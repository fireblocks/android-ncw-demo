package com.fireblocks.sdkdemo.bl.useraction

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
interface UserAction {


    @get:SerializedName("command")
    val command: String

    @get:SerializedName("data")
    val data: Any?

    fun execute()
}