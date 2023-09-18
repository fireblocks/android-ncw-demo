package com.fireblocks.sdkdemo.ui.signin

/**
 * Created by Fireblocks ltd. on 06/07/2023.
 */
data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
