package com.fireblocks.sdkdemo.bl.core.extensions

import android.util.Log
import com.fireblocks.sdk.logger.Level

/**
 * Created by Fireblocks Ltd. on 25/01/2024.
 */

//fun Timber.Forest.d(message: () -> String, vararg args: Any?) {
//    if (isDebugLog()) {
//        d(message(), args)
//    }
//}

fun getLogLevel(): Int {
    return Log.DEBUG
}

fun getNCWLogLevel(): Level {
    return Level.DEBUG
}

fun isDebugLog(): Boolean {
    return Log.DEBUG == getLogLevel()
}