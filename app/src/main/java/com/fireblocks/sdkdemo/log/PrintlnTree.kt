package com.fireblocks.sdkdemo.log

import android.util.Log
import timber.log.Timber

open class PrintlnTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE) {
            super.log(priority, tag, message, t)
        } else {
            Log.println(priority, tag, message)
        }
    }
}