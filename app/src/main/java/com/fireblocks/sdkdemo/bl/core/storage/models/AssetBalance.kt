package com.fireblocks.sdkdemo.bl.core.storage.models

import com.fireblocks.sdk.bl.core.storage.models.KeyId
import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */
data class AssetBalance(
    @KeyId @SerializedName("id") var id: String = "", // BTC_TEST
    @SerializedName("total") val total: String = "0",
    @SerializedName("available") val available: String = "0", //TODO use it in the MAX button
)
