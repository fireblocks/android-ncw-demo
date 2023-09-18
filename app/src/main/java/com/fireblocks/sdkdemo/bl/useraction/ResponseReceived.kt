package com.fireblocks.sdkdemo.bl.useraction

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class ResponseReceived(responseJson: String) : UserActionImpl() {
    override val command: String = "mockResponse"
    override val data: Any? = responseJson

    @Transient
    override val logForCrash: Boolean = false
}