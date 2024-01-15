package com.fireblocks.sdkdemo.log.filelogger

class PriorityFilter(private val minPriority: Int) : Filter {
    override fun shouldLog(priority: Int, tag: String?, message: String, t: Throwable?): Boolean {
        return priority >= minPriority
    }

    override fun isLoggable(priority: Int, tag: String?): Boolean {
        return priority >= minPriority
    }
}