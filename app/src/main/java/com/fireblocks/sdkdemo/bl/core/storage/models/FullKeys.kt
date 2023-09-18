package com.fireblocks.sdkdemo.bl.core.storage.models

import com.fireblocks.sdk.keys.FullKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks ltd. on 14/08/2023.
 */
data class FullKeys(@SerializedName("fullKeys") val fullKeys: Set<FullKey> = setOf())
