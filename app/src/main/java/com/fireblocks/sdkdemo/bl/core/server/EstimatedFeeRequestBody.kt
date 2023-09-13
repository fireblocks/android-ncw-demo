package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 09/08/2023.
 */
data class EstimatedFeeRequestBody(@SerializedName("assetId") val assetId: String,
                                   @SerializedName("accountId") val accountId: String = "0",
                                   @SerializedName("destAddress") val destAddress: String,
                                   @SerializedName("amount") val amount: String,
                                   @SerializedName("feeLevel") val feeLevel: FeeLevel? = null,
                                   @SerializedName("estimateFee") val estimateFee: Boolean = true,)
