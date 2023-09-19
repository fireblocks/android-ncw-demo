package com.fireblocks.sdkdemo.log

import android.util.Log
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
open class PrintlnTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE) {
            super.log(priority, tag, message, t)
        } else {
            Log.println(priority, tag, message)
        }
    }
}