package com.fireblocks.sdkdemo.bl.core.server

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 03/12/2023.
 */
data class JoinWalletBody(@SerializedName("walletId") val walletId: String)
