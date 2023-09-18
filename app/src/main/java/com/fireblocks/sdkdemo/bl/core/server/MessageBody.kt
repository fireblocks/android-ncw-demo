package com.fireblocks.sdkdemo.bl.core.server

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
data class MessageBody (@SerializedName("message") val message: String)