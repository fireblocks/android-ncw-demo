package com.fireblocks.sdkdemo.bl.core.base.fingerprint

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.fireblocks.sdkdemo.biometric.BiometricCallbackImpl
import com.fireblocks.sdkdemo.biometric.BiometricManager
import com.fireblocks.sdkdemo.bl.core.base.fingerprint.FingerPrintPreference.Companion.NO_ERROR
import com.fireblocks.sdkdemo.prefs.base.CryptoPreference
import com.fireblocks.sdkdemo.prefs.base.PasswordError
import com.fireblocks.sdkdemo.prefs.base.PasswordPreferenceResult
import com.fireblocks.sdkdemo.prefs.base.toHexString
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.security.*
import javax.crypto.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
interface FingerPrintPreferenceResult {
    fun value(result: ByteArray?, cancelled: Boolean, errorCode: Int, errorString: String)

    companion object {
        val EMPTY = FingerPrintPreferenceResultImpl { _, _, _ -> }
    }
}

typealias CompletionListener = (Int, String?) -> Unit

interface FingerPrintCompletionListener {
    fun onCompleted(errorCode: Int = NO_ERROR, errorString: String? = null)

    companion object {
        val EMPTY = FingerPrintCompletionListenerImpl { _, _ -> }
    }
}

class FingerPrintCompletionListenerImpl(private val completion: CompletionListener) : FingerPrintCompletionListener {
    override fun onCompleted(errorCode: Int, errorString: String?) {
        completion(errorCode, errorString)
    }
}

typealias ResultListener = (ByteArray?, Boolean, Int) -> Unit

class FingerPrintPreferenceResultImpl(private val resultListener: ResultListener) : FingerPrintPreferenceResult {
    override fun value(result: ByteArray?, cancelled: Boolean, errorCode: Int, errorString: String) {
        resultListener(result, cancelled, errorCode)
    }
}


@RequiresApi(Build.VERSION_CODES.N)
open class FingerPrintPreference(context: Context,
                                 group: String,
                                 alias: String, //this allows multiple values to be encrypted with the same alias keystore key.
                                 override val key: String = alias,
                                 val errorHandler: FingerPrintErrorHandler = FingerPrintErrorHandler.EMPTY,
                                 keyStoreProvider: String = "AndroidKeyStore") :
        CryptoPreference(context = context, group = group, alias = alias, keyStoreProvider = keyStoreProvider) {

    companion object {
        @VisibleForTesting
        var containsFingerPrint = true
        internal val NO_ERROR = FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NO_ERROR.errorCode
    }

    override val autoGenerateKey = false
    private val biometricManager = BiometricManager(context)

    private val fingerPrintHandler = FingerPrintHandler(context, errorHandler)

    override fun generateKey(keyName: String) {
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreProvider)
        val builder =
            KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC) //
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7) //
                    .setUserAuthenticationRequired(containsFingerPrint) //
                    .setInvalidatedByBiometricEnrollment(containsFingerPrint) //

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        }

        val keyGenParameterSpec = builder.build()
        try {
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: InvalidAlgorithmParameterException) {
            Timber.w(e, "Could not generate key: InvalidAlgorithmParameterException")
            fingerPrintHandler.onBiometricAuthenticationNoFingerPrints()
        } catch (e: Exception) {
            if (e is UserNotAuthenticatedException) {
                if (!biometricManager.isSupported(fingerPrintHandler)) {
                    Timber.w("Finger print is not supported") //probably better handling because this is quick for demo
                    return
                }
                biometricManager.authenticate(object : BiometricCallbackImpl() {
                    override fun onAuthenticationSuccessful() {
                        keyGenerator.generateKey()
                    }
                })
            }
        }
    }

    override fun set(value: ByteArray) { //this is async action
        encrypt(value, FingerPrintCompletionListener.EMPTY)
    }

    fun set(value: ByteArray, completion: FingerPrintCompletionListener) { //this is async action
        encrypt(value, completion)
    }

    private fun encrypt(value: ByteArray, completion: FingerPrintCompletionListener) {
        if (!biometricManager.isSupported(fingerPrintHandler)) {
            completion.onCompleted(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE.errorCode)
            return
        }

        initKeyIfNeeded()
        if(!keyStore.containsAlias(groupAlias)) {
            Timber.w("groupAlias does not exist:$groupAlias")
            return
        }

        val secretKey = getKey().key
        if (secretKey == null) {
            Timber.w("Secret key is null:$groupAlias")
            errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED)
            return
        }

        fun initCipher() {
            encrypter.init(Cipher.ENCRYPT_MODE, secretKey)
        }

        fun encrypt() {
            try {
                val result = encrypter.doFinal(value)
                Timber.i("encryption finished")
                getSharedPreferences().edit().putString(key, result.toHexString()).apply()
                completion.onCompleted()
            } catch (e: BadPaddingException) {
                handleUnknownException(e, completion, "Bad padding")
            } catch (e: IllegalBlockSizeException) {
                handleUnknownException(e, completion, "Illegal block size", FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_COMPATIBLE)
//                crashlyticsLog {"IllegalBlockSizeException occurred inside encrypt"}
//                crashlyticsLog(e)
            }
        }

        fun authenticateWithoutDuration() {
            initCipher()
            biometricManager.authenticate(object : BiometricCallbackImpl() {
                override fun onAuthenticationSuccessful() {
                    super.onAuthenticationSuccessful()
                    encrypt()
                    updateIV()
                }

                override fun onAuthenticationCancelled() {
                    fingerPrintHandler.onAuthenticationCancelled()
                    completion.onCompleted(PasswordError.UserCancelled.code, PasswordError.UserCancelled.errorString)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    fingerPrintHandler.onAuthenticationError(errorCode, errString)
                    completion.onCompleted(errorCode, errString.toString())
                }
            }, encrypter)
        }
        try {
            authenticateWithoutDuration()
        } catch (e: KeyPermanentlyInvalidatedException) {
            Timber.w(e, "Key permanently invalidated - fingerprint enrollment changed after creating key")
            errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED)
            completion.onCompleted(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED.errorCode,
                    "Key permanently invalidated - fingerprint enrollment changed after creating key") //Fingerprint enrollment has been changed
        } catch (e: UnrecoverableKeyException) {
            Timber.w(e, "Key permanently invalidated - fingerprint enrollment changed after creating key")
            errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED)
            completion.onCompleted(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED.errorCode,
                    "Key permanently invalidated - fingerprint enrollment changed after creating key") //Fingerprint enrollment has been changed
        } catch (e: InvalidKeyException) {
            authenticateWithoutDuration()
        } catch (e: NoSuchAlgorithmException) {
            handleUnknownException(e, completion, "No such algorithm:${encrypter.algorithm}")
        } catch (e: NoSuchPaddingException) {
            handleUnknownException(e, completion, "No such pading")
        } catch (e: NoSuchProviderException) {
            handleUnknownException(e, completion, "No such provider:${encrypter.provider}")
        } catch (e: InvalidAlgorithmParameterException) {
            handleUnknownException(e, completion, "Invalid algorithm parameters")
        } catch (e: KeyStoreException) {
            handleUnknownException(e, completion, "KeyStore exception")
        } catch (e: BadPaddingException) {
            handleUnknownException(e, completion, "Bad padding")
        } catch (e: IllegalBlockSizeException) {
            handleUnknownException(e, completion, "Illegal block size")
        }
    }

    private fun handleUnknownException(e: Exception, resultListener: FingerPrintPreferenceResult, errorString: String) {
        Timber.w(e, "Unknown fingerprint exception")
        errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE)
        resultListener.value(null,
                false,
                FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE.errorCode,
                errorString)
    }

    private fun handleUnknownException(e: Exception,
                                       resultListener: FingerPrintCompletionListener,
                                       errorString: String,
                                       fingerPrintError: FingerPrintErrorHandler.FingerPrintError = FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE) {
        Timber.w(e, "Unknown fingerprint exception")
        errorHandler.onError(fingerPrintError)
        resultListener.onCompleted(fingerPrintError.errorCode,
                errorString)
    }

    override fun encrypt(value: ByteArray): ByteArray {
        encrypt(value, FingerPrintCompletionListener.EMPTY)
        return defaultValue
    }

    fun value(resultListener: FingerPrintPreferenceResult) {
        if (!biometricManager.isSupported(fingerPrintHandler)) {
            resultListener.value(null, false, 1, " Biometric not supported")
            return
        }

        val secretKey = getKey().key

        if (secretKey == null) {
            resultListener.value(null,
                    false,
                    PasswordError.CantRetrievePasswordEncryptedPart.code,
                    PasswordError.CantRetrievePasswordEncryptedPart.errorString)
            return
        }

        fun initCipher() {
            decryptor.init(Cipher.DECRYPT_MODE, secretKey, getIv())
        }

        fun decrypt() {
            try {
                val result = decryptor.doFinal(encryptedValue())
                Timber.i("decryption finished")
                resultListener.value(result, false, -1, "")
            } catch (e: Exception) {
                Timber.w("unable to decrypt")
                resultListener.value(null, false, -300, "Unable to decrypt")
            }
        }


        fun authenticateWithoutDuration() {
            initCipher()
            biometricManager.authenticate(object : BiometricCallbackImpl() {
                override fun onAuthenticationSuccessful() {
                    super.onAuthenticationSuccessful()
                    decrypt()
                }

                override fun onAuthenticationCancelled() {
                    fingerPrintHandler.onAuthenticationCancelled()
                    resultListener.value(null, true, 10, "Authentication Cancelled")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    fingerPrintHandler.onAuthenticationError(errorCode, errString)
                    resultListener.value(null, errorCode == 10, errorCode, errString.toString())
                }
            }, decryptor)
        }
        try {
            authenticateWithoutDuration()
        } catch (e: KeyPermanentlyInvalidatedException) { //Finger prints enrollment has been changed
            Timber.w(e, "Key permanently invalidated - fingerprint enrollment changed after creating key")
            val error = FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED
            errorHandler.onError(error)
            resultListener.value(null,
                    false,
                    error.errorCode,
                    "Key permanently invalidated - fingerprint enrollment changed after creating key")

        } catch (e: UnrecoverableKeyException) { //Finger prints enrollment has been changed
            Timber.w(e, "Key permanently invalidated - fingerprint enrollment changed after creating key")
            val error = FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED
            errorHandler.onError(error)
            resultListener.value(null,
                    false,
                    error.errorCode,
                    "Key permanently invalidated - fingerprint enrollment changed after creating key")
        } catch (e: InvalidKeyException) {
            authenticateWithoutDuration()
        } catch (e: NoSuchAlgorithmException) {
            handleUnknownException(e, resultListener, "No such algorithm :${decryptor.algorithm}")
        } catch (e: NoSuchPaddingException) {
            handleUnknownException(e, resultListener, "No such padding")
        } catch (e: NoSuchProviderException) {
            handleUnknownException(e, resultListener, "No such provider:${decryptor.provider}")
        } catch (e: InvalidAlgorithmParameterException) {
            handleUnknownException(e, resultListener, "Invalid algorithm parameters")
        } catch (e: KeyStoreException) {
            handleUnknownException(e, resultListener, "KeyStore exception")
        } catch (e: BadPaddingException) {
            handleUnknownException(e, resultListener, "Bad padding")
        } catch (e: IllegalBlockSizeException) {
            handleUnknownException(e, resultListener, "Illegal block size")
        }

    }

    override suspend fun valueSync(): ByteArray? = suspendCancellableCoroutine { cont ->
        value(FingerPrintPreferenceResultImpl { result, cancelled, errorCode ->
            if (cont.isActive) {
                if (cancelled) {
                    fingerPrintHandler.onAuthenticationCancelled()
                } else {
                    if (errorCode != NO_ERROR && errorCode != 10) {
                        fingerPrintHandler.onAuthenticationError(errorCode, "error")
                    }
                }
                if (errorCode == NO_ERROR) {
                    cont.resume(result)
                } else {
                    cont.resumeWithException(FingerPrintException(errorCode, cancelled))
                }
            }
        })
    }

    override suspend fun setSync(value: ByteArray): PasswordPreferenceResult<Boolean> = suspendCoroutine { cont ->
        set(value, FingerPrintCompletionListenerImpl { errorCode, errorString ->
            val isSuccess = errorCode == NO_ERROR
            val result = PasswordPreferenceResult(isSuccess, PasswordError.from(errorCode))
            cont.resume(result)
        })
    }
}