package com.fireblocks.sdkdemo.bl.useraction


class ResponseReceived(responseJson: String) : UserActionImpl() {
    override val command: String = "mockResponse"
    override val data: Any? = responseJson

    @Transient
    override val logForCrash: Boolean = false
}