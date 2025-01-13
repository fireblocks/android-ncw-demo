package com.fireblocks.sdkdemo.bl.core.server.models

import com.google.gson.Gson
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 13/01/2025.
 */
object ResponseErrorUtil {
    private val gson = Gson()

    fun parseErrorMessage(errorBody: String?): String? {
        try {
            val serverResponseError = gson.fromJson(errorBody, ServerResponseError::class.java)
            Timber.d("Server response error: $serverResponseError")
            return serverResponseError.message
        } catch (e: Exception) {
            Timber.e(e)
            return null
        }
    }
}