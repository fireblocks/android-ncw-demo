package com.fireblocks.sdkdemo.bl.core.base.biometric

import androidx.annotation.Keep
import com.fireblocks.sdkdemo.biometric.BiometricCallback
import timber.log.Timber
@Keep
open class BiometricCallbackImpl : BiometricCallback {

    private fun log(text: String) {
        Timber.d(text)
    }

    override fun onSdkVersionNotSupported() {
        Timber.d("SDK not supported")
    }

    override fun onBiometricAuthenticationNotSupported() {
        log("BiometricAuthentication not supported")
    }

    override fun onBiometricAuthenticationNoFingerPrints() {
        log("onBiometricAuthenticationNoFingerPrints")
    }

    override fun onBiometricAuthenticationPermissionNotGranted() {
        log("onBiometricAuthenticationPermissionNotGranted")
    }

    override fun onBiometricAuthenticationInternalError(error: String?) {
        log("onBiometricAuthenticationInternalError: $error")
    }

    override fun onAuthenticationFailed() {
        log("onAuthenticationFailed")
    }

    override fun onAuthenticationCancelled() {
        log("onAuthenticationCancelled")
    }

    override fun onAuthenticationSuccessful() {
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        log("onAuthenticationHelp:$helpCode, $helpString")
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        log("onAuthenticationError:$errorCode, $errString")
    }
}