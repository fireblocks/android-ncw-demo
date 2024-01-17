package com.fireblocks.sdkdemo.bl.core.server

import android.content.Context
import com.fireblocks.sdk.messages.FireblocksMessageHandler
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 * An implementation example for the FireblocksMessageHandler.
 * Override the handleOutgoingMessage method and call pass the payload to Fireblocks BE using your BE implementation.
 * Make sure you to invoke the responseCallback with the response body or the errorCallback in case of an error
 */
class FireblocksMessageHandlerImpl(private val context: Context, private val deviceId: String) : FireblocksMessageHandler, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun handleOutgoingMessage(payload: String,
                                       responseCallback: (responseBody: String?) -> Unit,
                                       errorCallback: (errorMessage: String?) -> Unit) {
        // do rest API call
        Timber.i("calling invoke rest API")
        runCatching {
            Api.with(StorageManager.get(context, deviceId)).invoke(deviceId, MessageBody(payload)).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Timber.i("got response from invoke rest API, code:${response.code()}, isSuccessful:${response.isSuccessful}")
                    val body = response.body()
                    responseCallback(body)
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.e(t, "Failed to call invoke Api")
                    errorCallback("Failed to call invoke Api")
                }
            })
        }.onFailure {
            Timber.e(it, "Failed to call invoke Api")
            errorCallback("Failed to call invoke Api")
        }
    }
}