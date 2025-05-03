package com.fireblocks.sdkdemo.log.filelogger

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class CommaFormatter private constructor() : LogFormatter {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    companion object {
        val instance = CommaFormatter()
    }

    override fun format(priority: Int, tag: String?, message: String?): String {
        return "${getDate()} ${Filter.logLevelLetter(priority)} $tag -->: $message\n"
    }

    private fun getDate(): String {
        return dateTimeFormatter.format(LocalDateTime.now())
    }
}