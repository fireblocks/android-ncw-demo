package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.google.gson.annotations.SerializedName

data class CreateTransactionRequestBody (@SerializedName("assetId") val assetId: String,
                                         @SerializedName("accountId") val accountId: String = "0",
                                         @SerializedName("destAddress") val destAddress: String,
                                         @SerializedName("amount") val amount: String,
                                         @SerializedName("feeLevel") val feeLevel: FeeLevel)