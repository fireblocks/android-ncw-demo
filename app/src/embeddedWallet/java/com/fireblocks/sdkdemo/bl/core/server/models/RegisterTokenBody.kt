package com.fireblocks.sdkdemo.bl.core.server.models

/**
 * Created by Fireblocks Ltd. on 27/05/2025.
 */
data class RegisterTokenBody(
    val token: String,
    val platform: String = "android",// android, ios
    val walletId: String,
    val deviceId: String
)
