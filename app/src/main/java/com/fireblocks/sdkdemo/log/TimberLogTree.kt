package com.fireblocks.sdkdemo.log

import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.log.filelogger.Filter
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
private const val MAX_LOG_LENGTH = 4000
private const val TOO_LARGE_LOG_LENGTH = 1000000

open class TimberLogTree(private val filter: Filter = Filter.EMPTY) : PrintlnTree(), HttpLoggingInterceptor.Logger {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!(filter.isLoggable(priority, tag))) {
            return
        }
        largeLog(priority, "Dev-${Thread.currentThread().id}-${tag}", message, t)
    }

    override fun log(message: String) {
        if (isDebugLog()) {
            Timber.tag("HTTP").d(message)
        }
    }

    private fun largeLog(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (message.length < TOO_LARGE_LOG_LENGTH) {
            if (message.length > MAX_LOG_LENGTH) {
                super.log(priority, tag, message.substring(0, MAX_LOG_LENGTH), t)
                largeLog(priority, tag, message.substring(MAX_LOG_LENGTH), t)
            } else {
                super.log(priority, tag, message, t)
            }
        }
    }
}