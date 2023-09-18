package com.fireblocks.sdkdemo.log

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.platform.Platform
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class HttpLoggingInterceptor @JvmOverloads constructor(private val prefix: String,
                                                       private val logger: Logger = Logger.DEFAULT) : Interceptor {

    interface Logger {
        fun logMessage(message: String)

        companion object {
            /** A [Logger] defaults output appropriate for the current platform. */
            @JvmField
            val DEFAULT: Logger = object : Logger {
                override fun logMessage(message: String) {
                    Platform.get().log(message, Platform.INFO, null)
                }
            }
        }
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val logBody = true
        val logHeaders = true

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (!logHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.logMessage("$prefix - $requestStartMessage")

        val requestHeaders = request.headers

        if (requestBody != null) {
            // Request body headers are only present when installed as a network interceptor. When not
            // already present, force them to be included (if available) so their values are known.
            requestBody.contentType()?.let {
                if (requestHeaders["Content-Type"] == null) {
                    logger.logMessage("$prefix - Content-Type: $it")
                }
            }
            if (requestBody.contentLength() != -1L) {
                if (requestHeaders["Content-Length"] == null) {
                    logger.logMessage("$prefix - Content-Length: ${requestBody.contentLength()}")
                }
            }
        }

        for (i in 0 until requestHeaders.size) {
            logHeader(requestHeaders, i)
        }

        if (!logBody || requestBody == null) {
            logger.logMessage("$prefix - --> END ${request.method}")
        } else if (bodyHasUnknownEncoding(request.headers)) {
            logger.logMessage("$prefix - --> END ${request.method} (encoded body omitted)")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            logger.logMessage("")
            if (buffer.isProbablyUtf8()) {
                logger.logMessage(buffer.readString(charset))
                logger.logMessage("$prefix - --> END ${request.method} (${requestBody.contentLength()}-byte body)")
            } else {
                logger.logMessage("$prefix - --> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)")
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.logMessage("$prefix - <-- HTTP FAILED: $e")
            if (e is SocketTimeoutException) {
                val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
                logger.logMessage("$prefix - Timeout: ${TimeUnit.MILLISECONDS.toSeconds(tookMs)} seconds")
            }
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.logMessage("$prefix - <-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms)")

        val responseHeaders = response.headers
        for (i in 0 until responseHeaders.size) {
            logHeader(responseHeaders, i)
        }

        if (bodyHasUnknownEncoding(response.headers)) {
            logger.logMessage("$prefix - <-- END HTTP (encoded body omitted)")
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            var gzippedLength: Long? = null
            if ("gzip".equals(responseHeaders["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            if (!buffer.isProbablyUtf8()) {
                logger.logMessage("")
                logger.logMessage("$prefix - <-- END HTTP (binary ${buffer.size}-byte body omitted)")
                return response
            }

            if (contentLength != 0L) {
                logger.logMessage("")
                logger.logMessage("$prefix - ${buffer.clone().readString(charset)}")
            }

            if (gzippedLength != null) {
                logger.logMessage("$prefix - <-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
            } else {
                logger.logMessage("$prefix - <-- END HTTP (${buffer.size}-byte body)")
            }
        }

        return response
    }

    private fun logHeader(headers: Headers, i: Int) {
        val name = headers.name(i)
        val value = headers.values(name)
        value.forEach {
            logger.logMessage("$prefix - $name :  $it")
        }
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) && !contentEncoding.equals("gzip",
                ignoreCase = true)
    }
}

internal fun Buffer.isProbablyUtf8(): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        for (i in 0 until 16) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (_: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}