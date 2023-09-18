package com.fireblocks.sdkdemo.log.filelogger

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
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