package com.fireblocks.sdkdemo.log.filelogger

import android.content.Context
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class FileManager {

    companion object {
        const val BUFFER = 1024 * 1024
        const val ZIPPED_LOGS = "DemoZippedLogs.zip"
    }

    private fun zip(_files: List<String>, zipFileName: String) {
        val dest = FileOutputStream(zipFileName)
        val out = ZipOutputStream(BufferedOutputStream(dest))
        try {
            var origin: BufferedInputStream?
            val data = ByteArray(BUFFER)
            for (i in _files.indices) {
                Timber.d("Adding: ${_files[i]}")
                val fi = FileInputStream(_files[i])
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                fi.close()
                origin.close()
                out.closeEntry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not close stream")
        } finally {
            try {
                out.flush()
                out.close()
            } catch (e: Exception) {
                Timber.e(e, "Could not close stream")
            }
        }
    }

    fun zipLogs(context: Context, onZipped: (File?, String?) -> Unit) {
        val deleted = context.deleteFile(ZIPPED_LOGS)
        val listFiles = context.filesDir.listFiles()
        val files = listFiles?.map {
            it.absolutePath
        }?.filter {
            it.endsWith(".log")
        }

        files?.let {
            val zippedLogs = File(context.filesDir, ZIPPED_LOGS)
            zippedLogs.createNewFile()
            zip(files, zippedLogs.absolutePath)
            if (zippedLogs.length() > 0) {
                onZipped(zippedLogs, null)
            } else {
                onZipped(null, "Unable to create log file")
            }
        }
    }


}