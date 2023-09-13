package com.fireblocks.sdkdemo.log

import timber.log.Timber
import java.util.regex.Pattern

class LoggerInterfaceImpl : LoggerInterface {
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    override fun logDebug(message: String?, error: Throwable?, trace: Boolean) {
        Timber.tag(getTag()).d(error, message)
        if (trace) {
            Timber.tag(getPrevious()).d(error, message)
        }
    }

    private fun getTag(): String {
        val stackTrace = Throwable().stackTrace
        return createStackElementTag(stackTrace[4])
    }

    private fun getPrevious(): String {
        val stackTrace = Throwable().stackTrace
        return createStackElementTag(stackTrace[4])
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        return tag
    }
}