package com.fireblocks.sdkdemo.log.filelogger

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class CommaFormatter private constructor() : LogFormatter {
    companion object {
        val instance = CommaFormatter()
    }

    override fun format(priority: Int, tag: String?, message: String?): String {
        return "${getDate()} ${Filter.logLevelLetter(priority)} $tag -->: $message\n"
    }

    private fun getDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return formatter.format(Date())
    }
}