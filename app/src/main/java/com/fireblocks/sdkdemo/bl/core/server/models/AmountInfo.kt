package com.fireblocks.sdkdemo.bl.core.server.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 10/08/2023.
 */
data class AmountInfo(@SerializedName("amount") val amount: String? = null,
                      @SerializedName("amountUSD") val amountUSD: String? = null,
)
