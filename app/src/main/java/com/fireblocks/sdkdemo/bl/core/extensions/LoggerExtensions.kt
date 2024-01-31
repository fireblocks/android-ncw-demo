package com.fireblocks.sdkdemo.bl.core.extensions

import com.fireblocks.sdk.logger.Level
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 25/01/2024.
 */

//fun Timber.Forest.d(message: () -> String, vararg args: Any?) {
//    if (isDebugLog()) {
//        d(message(), args)
//    }
//}

fun getLogLevel(): Level {
    return Level.INFO
}

fun isDebugLog(): Boolean {
    return Level.DEBUG == getLogLevel()
}