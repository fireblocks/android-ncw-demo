package com.fireblocks.sdkdemo.ui.signin


/**
 * Created by Fireblocks ltd. on 06/07/2023.
 */
data class SignInResult(
    val data: UserData? = null,
    val errorMessage: String? = null
)

data class UserData(
    val email: String?,
    val userName: String?,
    val profilePictureUrl: String?,
    val idToken: String?
)


