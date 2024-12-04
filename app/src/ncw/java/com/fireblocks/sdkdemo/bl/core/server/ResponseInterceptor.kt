package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.bl.useraction.ResponseReceived
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source?.buffer
        val contentType = responseBody?.contentType()
        val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

        val responseString = buffer?.clone()?.readString(charset)

        val headersMap = hashMapOf<String, String>()
        response.headers.names().forEach {
            headersMap[it] = response.header(it) ?: ""
        }

        val requestBuffer = Buffer()
        response.request.body?.writeTo(requestBuffer)

        val requestContentType = response.request.body?.contentType()
        val requestCharset: Charset = requestContentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        val requestBodyString =  requestBuffer.readString(requestCharset)

        responseReceived(response.request.url.pathSegments.toString().replace("[", "") //
                .replace("]", ""), //
                response.request.url.query, //
                requestBodyString,
                responseString ?: "", //
                response.code, //
                headersMap)

        return response
    }

    private val gson = Gson()

    fun responseReceived(url: String,
                         query: String?,
                         requestData: String,
                         data: String,
                         statusCode: Int,
                         headers: Map<String, Any>) {

        val finalUrl = when (query) {
            null -> url
            else -> "$url?$query"
        }
        val response = hashMapOf("url" to finalUrl,
            "requestData" to requestData,
            "body" to data,
            "statusCode" to statusCode,
            "headers" to headers)
        ResponseReceived(gson.toJson(response)).execute()
    }

}