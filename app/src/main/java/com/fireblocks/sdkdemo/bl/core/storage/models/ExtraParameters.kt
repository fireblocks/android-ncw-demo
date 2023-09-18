package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.Serializable

/**
 * Created by Fireblocks ltd. on 11/25/20
 */
data class ExtraParameters(@SerializedName("contractCallData") val contractCallData: String? = null,
                           @SerializedName("rawMessageData") val rawMessageData: RawMessageData? = null) : Serializable
{
    fun getPayloadString(): String? {
        if (!contractCallData.isNullOrBlank()) {
            return contractCallData
        }
        return rawMessageData?.messages?.firstOrNull()?.content?.toString()
    }

    fun getContentMessageType(context: Context): String? {
        return runCatching {
            val type = rawMessageData?.messages?.firstOrNull()?.type
            if (type != null) {
                val messageType = TypedMessageType.ofType(type)
                messageType?.getDisplayName(context)
            } else {
                Timber.i("unable to display content message type:$type")
                null
            }
        }.onFailure {
            Timber.i("unable to display content message type:${it.cause}")
        }.getOrNull()
    }

    fun getMessageContent(): String? {
        return runCatching {
            val content = rawMessageData?.messages?.firstOrNull()?.content
            content?.getAttribute("message")
        }.onFailure {
            Timber.i("unable to display message content:${it.cause}")
        }.getOrNull()
    }

}

data class RawMessageData(@SerializedName("messages") val messages: ArrayList<RawMessage> = arrayListOf()) : Serializable
data class RawMessage(@SerializedName("content") val content: StringOrObject?,
                      @SerializedName("type") val type: String? = null): Serializable
