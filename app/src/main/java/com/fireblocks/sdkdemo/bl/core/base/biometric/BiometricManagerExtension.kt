package com.fireblocks.sdkdemo.bl.core.base.biometric

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fireblocks.sdkdemo.biometric.BiometricCallback
import com.fireblocks.sdkdemo.bl.core.base.fingerprint.FingerPrintHandler
import com.fireblocks.sdkdemo.bl.core.base.getActivity
import timber.log.Timber
import java.security.Signature
import javax.crypto.Cipher

fun BiometricManager.isSupported(biometricCallback: FingerPrintHandler): Boolean {

    val canAuthenticateStatus = canAuthenticate(BIOMETRIC_STRONG)
    return when (canAuthenticateStatus) {
        BiometricManager.BIOMETRIC_SUCCESS -> true

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            // Biometric features hardware is missing
            Timber.w("Biometric features hardware is missing")
            biometricCallback.onBiometricAuthenticationNotSupported()
            false
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            // Biometric features are currently unavailable.
            Timber.w("Biometric features are currently unavailable")
            biometricCallback.onBiometricAuthenticationNotSupported()
            false
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            // The user didn't enroll in biometrics that your app accepts, prompt them to enroll in it
            Timber.w("User has not enrolled finger prints")
            biometricCallback.onBiometricAuthenticationNoFingerPrints()
            false
        }
        else -> {
            Timber.w("Biometric features are currently unavailable - canAuthenticateStatus:$canAuthenticateStatus")
            biometricCallback.onBiometricAuthenticationNotSupported()
            false
        }
    }
}

fun BiometricManager.authenticate(context: Context, biometricCallback: BiometricCallback,  signature: Signature) {
    val biometricPrompt = createBiometricPrompt(context, biometricCallback)
    Handler(Looper.getMainLooper()).post {
        biometricPrompt.authenticate(createPromptInfo(), CryptoObject(signature))
    }
}

fun BiometricManager.authenticate(context: Context, biometricCallback: BiometricCallback, cipher: Cipher) {
    val biometricPrompt = createBiometricPrompt(context, biometricCallback)
    Handler(Looper.getMainLooper()).post {
        biometricPrompt.authenticate(createPromptInfo(), CryptoObject(cipher))
    }
}

fun BiometricManager.authenticate(context: Context, biometricCallback: BiometricCallback) {
    val biometricPrompt = createBiometricPrompt(context, biometricCallback)
    Handler(Looper.getMainLooper()).post {
        biometricPrompt.authenticate(createPromptInfo())
    }
}



fun createBiometricPrompt(context: Context, biometricCallback: BiometricCallback): BiometricPrompt {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                // If you've added negative button to prompt dialog
            }
            biometricCallback.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // Failure with unknown reason
            biometricCallback.onAuthenticationFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // Authenticated successfully!
            biometricCallback.onAuthenticationSuccessful()
        }
    }

    val executor = ContextCompat.getMainExecutor(context)
    val activity = getActivity() as FragmentActivity
    return BiometricPrompt(activity, executor, callback)
}

fun createPromptInfo(): BiometricPrompt.PromptInfo =
    BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate with biometrics")
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .setConfirmationRequired(false)
        .setNegativeButtonText("Cancel")
        .build()
