package com.fireblocks.sdkdemo.log.filelogger

import androidx.annotation.Keep
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Keep
class FileLoggerTree private constructor(private val filter: Filter,
                                         private val formatter: LogFormatter,
                                         private val logger: Logger,
                                         private val fileHandler: FileHandler?,
                                         private val path: String,
                                         private val maxFileCount: Int) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!(filter.isLoggable(priority, tag))) {
            return
        }
        logger.log(Filter.logLevel(priority), formatter.format(priority, tag, message))
        if (t != null) {
            logger.log(Filter.logLevel(priority), "", t)
        }
    }


    fun clear() {
        fileHandler?.close()
        for (i in 0 until maxFileCount) {
            val f = File(getFileName(i))
            if (f.exists() && f.isFile) {
                f.delete()
            }
        }
    }

    private fun getFileName(i: Int): String {
        return if (!this.path.contains("%g")) {
            this.path + "." + i
        } else {
            this.path.replace("%g", "" + i)
        }
    }

    fun getFiles(): Collection<File> {
        val col: MutableCollection<File> = ArrayList(maxFileCount)
        for (i in 0 until maxFileCount) {
            val f = File(getFileName(i))
            if (f.exists()) {
                col.add(f)
            }
        }
        return col
    }


    class Builder {
        private var fileName = "log"
        private var dir = ""
        private var filter: Filter = Filter.EMPTY
        private var sizeLimit = MAX_FILE_SIZE_BYTES
        private var fileLimit = MAX_FILE_COUNT
        private var appendToFile = true
        private var formatter: LogFormatter = LogFormatter.EMPTY

        fun filename(fn: String): Builder {
            fileName = fn
            return this
        }

        fun directory(dn: String): Builder {
            dir = dn
            return this
        }

        fun file(d: File): Builder {
            dir = d.absolutePath
            return this
        }

        fun sizeLimit(nbBytes: Int): Builder {
            sizeLimit = nbBytes
            return this
        }


        fun fileLimit(f: Int): Builder {
            fileLimit = f
            return this
        }

        fun appendToFile(b: Boolean): Builder {
            appendToFile = b
            return this
        }

        fun formatter(f: LogFormatter): Builder {
            formatter = f
            return this
        }

        fun filter(f: Filter) : Builder {
            filter = f
            return this
        }

        @Throws(IOException::class)
        fun build(): FileLoggerTree {
            val path = "$dir/$fileName"
            val fileHandler: FileHandler
            val logger: Logger = MyLogger.getLogger(TAG)
            logger.level = Level.ALL
            if (logger.handlers.isEmpty()) {
                fileHandler = FileHandler(path, sizeLimit, fileLimit, appendToFile)
                fileHandler.formatter = DefaultFormatter()
                logger.addHandler(fileHandler)
            } else {
                fileHandler = logger.handlers[0] as FileHandler
            }
            return FileLoggerTree(filter, formatter, logger, fileHandler, path, fileLimit)
        }

        companion object {
            private const val MAX_FILE_SIZE_BYTES = 1048576
            private const val MAX_FILE_COUNT = 3
        }
    }

    private class DefaultFormatter : Formatter() {
        override fun format(record: LogRecord): String {
            return record.message
        }
    }

    private class MyLogger internal constructor(name: String?) : Logger(name, null) {
        companion object {
            fun getLogger(name: String?): Logger {
                return MyLogger(name)
            }
        }
    }

    companion object {
        private const val TAG = "FileLoggerTree"
    }
}