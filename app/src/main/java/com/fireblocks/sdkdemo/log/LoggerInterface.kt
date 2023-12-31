package com.fireblocks.sdkdemo.log

import com.fireblocks.sdkdemo.log.LoggerInterface.Companion.instance

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
interface LoggerInterface {

    fun logDebug(message: String? = "", error: Throwable? = null, trace: Boolean = false) {
        //do nothing
    }

    companion object {
        internal val instance = LoggerInterfaceImpl()
    }
}


fun logDebug(message: String? = "", error: Throwable? = null) {
    instance.logDebug(message, error)
}
