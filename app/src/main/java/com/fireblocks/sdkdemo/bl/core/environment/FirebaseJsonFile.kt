package com.fireblocks.sdkdemo.bl.core.environment

import com.google.gson.annotations.SerializedName

data class FireblocksJsonFile(@SerializedName("host") val host: String,
                              @SerializedName("LOG_TAG") val logTag: String,
                              @SerializedName("envIndicator") val envIndicator: String,
                              @SerializedName("isDefault") val isDefault: Boolean = false)
