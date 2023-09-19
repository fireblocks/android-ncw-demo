package com.fireblocks.sdkdemo.bl.useraction

/**
 * Created by Fireblocks Ltd. on 06/08/2020
 */
class ApplicationResumed : UserActionImpl() {
    override val command: String = "applicationResumed"
    override val data: Any? = ""
}

class ApplicationPaused : UserActionImpl() {
    override val command: String = "applicationPaused"
    override val data: Any? = ""
}