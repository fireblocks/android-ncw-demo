package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks ltd. on 08/08/2023.
 */
data class AssetAddress(@SerializedName("address") val address: String = "")
