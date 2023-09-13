package com.fireblocks.sdkdemo.bl.core.base.fingerprint

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import com.fireblocks.sdkdemo.biometric.BiometricCallbackImpl
import timber.log.Timber

@RequiresApi(M)
class FingerPrintHandler(private val context: Context, private val errorHandler: FingerPrintErrorHandler) :
        BiometricCallbackImpl() {

    override fun onSdkVersionNotSupported() {
        super.onSdkVersionNotSupported()
        errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE)
    }

    override fun onBiometricAuthenticationNoFingerPrints() {
        super.onBiometricAuthenticationNoFingerPrints()
        if (isWorkProfile(context)) {
            Timber.i("No fingerprints work profile")
            errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_ENROLLED_WORK_PROFILE)
        } else {
            Timber.i("No fingerprints personal profile")
            errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_ENROLLED)
        }
    }

    override fun onBiometricAuthenticationPermissionNotGranted() {
        super.onBiometricAuthenticationPermissionNotGranted()
        errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        when (errorCode) {
            BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT, //
            BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT -> errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_TOO_MANY_ATTEMPTS)
            BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> onBiometricAuthenticationNoFingerPrints()
            BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT -> onBiometricAuthenticationNotSupported()
            BiometricPrompt.BIOMETRIC_ERROR_TIMEOUT -> {
                Timber.i("Biometric timeout")
            }
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED.errorCode -> {
                Timber.i("Fingerprint permanently invalidated")
            }
            else -> {
                Timber.i("unknown error code: $errorCode, $errString")
            }
        }
    }

    override fun onAuthenticationCancelled() {
        super.onAuthenticationCancelled()
        //errorHandler.onError(FingerPrintErrorHandler.FingerPrintError.USER_CANCELLED)
    }

    private fun isWorkProfile(context: Context): Boolean {
        val devicePolicyManager: DevicePolicyManager? =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?
        val activeAdmins = devicePolicyManager?.activeAdmins
        if (activeAdmins != null) {
            for (admin in activeAdmins) {
                val packageName = admin.packageName
                if (devicePolicyManager.isProfileOwnerApp(packageName)) {
                    return true
                }
            }
        }

        return false
    }
}