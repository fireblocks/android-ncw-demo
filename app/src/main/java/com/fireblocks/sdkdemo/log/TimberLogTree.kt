package com.fireblocks.sdkdemo.log

import timber.log.Timber

private const val MAX_LOG_LENGTH = 4000

open class TimberLogTree : PrintlnTree(), HttpLoggingInterceptor.Logger {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        super.log(priority, "Dev-${Thread.currentThread().id}-${tag}", message, t)
        largeLog(priority, "Dev-${Thread.currentThread().id}-${tag}", message, t)
    }

    override fun logMessage(message: String) {
        Timber.tag("HTTP").d(message)
    }

    private fun largeLog(priority: Int, tag: String?, message: String, t: Throwable?) { //TODO SEE code in Timber.kt log message that uses MAX_LOG_LENGTH
        if (message.length > MAX_LOG_LENGTH) {
            super.log(priority, tag, message.substring(0, MAX_LOG_LENGTH), t)
            largeLog(priority, tag, message.substring(MAX_LOG_LENGTH), t)
        } else {
            super.log(priority, tag, message, t)
        }
    }
}