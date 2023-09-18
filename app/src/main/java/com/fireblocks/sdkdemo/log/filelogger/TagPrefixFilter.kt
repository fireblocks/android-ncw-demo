package com.fireblocks.sdkdemo.log.filelogger

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class TagPrefixFilter(private val tagPrefix: String, private val include: Boolean) : Filter {
    override fun shouldLog(priority: Int, tag: String?, message: String, t: Throwable?): Boolean {
        return isLoggable(priority, tag)
    }

    override fun isLoggable(priority: Int, tag: String?): Boolean {
        tag?.let {
            if (include){
                return it.startsWith(this.tagPrefix)
            } else { // exclude
                return !it.startsWith(this.tagPrefix)
            }
        }
        return false
    }
}