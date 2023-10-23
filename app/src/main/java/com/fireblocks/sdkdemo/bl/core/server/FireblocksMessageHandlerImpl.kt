package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdk.messages.FireblocksMessageHandler
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 * An implementation example for the FireblocksMessageHandler.
 * Override the handleOutgoingMessage method and call pass the payload to Fireblocks BE using your BE implementation.
 * Make sure you to invoke the responseCallback with the response body or the errorCallback in case of an error
 */
class FireblocksMessageHandlerImpl(private val deviceId: String, private val storageManager: StorageManager) : FireblocksMessageHandler, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun handleOutgoingMessage(payload: String,
                                       responseCallback: (responseBody: String?) -> Unit,
                                       errorCallback: (errorMessage: String?) -> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                // do rest API call
                Timber.i("$deviceId - calling invoke rest API")
                runCatching {
                    val response = Api.with(storageManager).invoke(deviceId, MessageBody(payload)).execute()
                    Timber.i("$deviceId - got response from invoke rest API, code:${response.code()}, isSuccessful:${response.isSuccessful}")
                    val body = response.body()
                    responseCallback(body)
                }.onFailure {
                    Timber.e(it, "$deviceId - Failed to call invoke Api")
                    errorCallback("$deviceId - Failed to call invoke Api")
                }
            }
        }
    }
}