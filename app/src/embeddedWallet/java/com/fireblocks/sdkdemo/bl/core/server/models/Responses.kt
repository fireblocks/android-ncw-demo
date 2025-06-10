package com.fireblocks.sdkdemo.bl.core.server.models


/**
 * Created by Fireblocks Ltd. on 27/05/2025
 */
data class RegisterTokenResponseData(
    val id: String? = null,
    val userId: String? = null,
    val platform: String? = null,
    val walletId: String? = null,
    val deviceId: String? = null
)

data class RegisterTokenResponse(
    val success: Boolean? = null,
    val data: RegisterTokenResponseData? = null
)