package com.fireblocks.sdkdemo.bl.core.server

import com.google.gson.annotations.SerializedName

data class MessageBody (@SerializedName("message") val message: String)