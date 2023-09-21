package com.fireblocks.sdkdemo.bl.core.environment

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
data class FireblocksJsonFile(@SerializedName("host") val host: String,
                              @SerializedName("LOG_TAG") val logTag: String,
                              @SerializedName("envIndicator") val envIndicator: String,
                              @SerializedName("isDefault") val isDefault: Boolean = false)
