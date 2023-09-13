package com.fireblocks.sdkdemo.bl.core.storage.models

import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */
data class FeeData(@SerializedName("networkFee") val networkFee: String? = "0",
                   var feeLevel: FeeLevel = FeeLevel.MEDIUM)
