package com.fireblocks.sdkdemo.bl.useraction

import com.google.gson.annotations.SerializedName

interface UserAction {


    @get:SerializedName("command")
    val command: String

    @get:SerializedName("data")
    val data: Any?

    fun execute()
}