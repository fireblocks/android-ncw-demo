package com.fireblocks.sdkdemo.log.filelogger

interface LogFormatter {
    fun format(priority: Int, tag: String?, message: String?): String

    companion object {
        val EMPTY: LogFormatter
            get() = object : LogFormatter {
                override fun format(priority: Int, tag: String?, message: String?): String {
                    return message + ""
                }
            }
    }
}