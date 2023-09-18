package com.fireblocks.sdkdemo.bl.core.server.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks ltd. on 10/08/2023.
 */
data class Source(@SerializedName("id") val id: String? = null,
                  @SerializedName("name") val name: String? = null,
                  @SerializedName("type") val type: String? = null,
                  @SerializedName("subType") val subType: String? = null,
                  @SerializedName("walletId") val walletId: String? = null,
)
