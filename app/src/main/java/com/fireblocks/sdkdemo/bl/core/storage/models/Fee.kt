package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks ltd. on 08/08/2023.
 */
data class Fee(@SerializedName("low") val low: FeeData? = FeeData(),
               @SerializedName("medium") val medium: FeeData? = FeeData(),
               @SerializedName("high") val high: FeeData? = FeeData())

