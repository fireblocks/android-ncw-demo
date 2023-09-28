package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */
data class AssetAddress(
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountId") val accountId: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("addressType") val addressType: String = ""
)

