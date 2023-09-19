package com.fireblocks.sdkdemo.log.filelogger

import android.util.Log
import java.util.logging.Level

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
interface Filter {
    fun shouldLog(priority: Int, tag: String?, message: String, t: Throwable?): Boolean

    fun isLoggable(priority: Int, tag: String?): Boolean

    companion object {
        val EMPTY = object : Filter {
            override fun shouldLog(priority: Int, tag: String?, message: String, t: Throwable?): Boolean {
                return true
            }

            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return true
            }
        }

        fun logLevel(priority: Int): Level {
            return when (priority) {
                Log.VERBOSE -> Level.FINER
                Log.DEBUG -> Level.FINE
                Log.INFO -> Level.INFO
                Log.WARN -> Level.WARNING
                Log.ERROR -> Level.SEVERE
                Log.ASSERT -> Level.SEVERE
                else -> Level.FINEST
            }
        }

        fun logLevelLetter(priority: Int): String {
            val logLevel = logLevel(priority)
            return logLevel.name.toString().toUpperCase()
        }
    }
}