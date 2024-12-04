package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 09/08/2023.
 */
data class EstimatedFeeResponse(@SerializedName("fee") var fee: Fee? = null)
