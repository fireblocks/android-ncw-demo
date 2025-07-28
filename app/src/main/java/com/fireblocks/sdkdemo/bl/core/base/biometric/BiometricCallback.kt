package com.fireblocks.sdkdemo.bl.core.base.biometric

interface BiometricCallback {

    fun onSdkVersionNotSupported()

    fun onBiometricAuthenticationNotSupported()

    fun onBiometricAuthenticationNoFingerPrints()

    fun onBiometricAuthenticationPermissionNotGranted()

    fun onBiometricAuthenticationInternalError(error: String?)


    fun onAuthenticationFailed()

    fun onAuthenticationCancelled()

    fun onAuthenticationSuccessful()

    fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?)

    fun onAuthenticationError(errorCode: Int, errString: CharSequence?)
}
