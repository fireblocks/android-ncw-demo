package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 27/09/2023.
 */
data class AssetsSummary(
    @SerializedName("asset") val asset: SupportedAsset? = null,
    @SerializedName("address") val address: AssetAddress? = null,
    @SerializedName("balance") val balance: AssetBalance? = null,
)
