package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
object PollingMessagesManager : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private const val POLLING_FREQUENCY = 1000L
    private val jobs = hashMapOf<String,Job>()

    fun startPollingMessages(context: Context, deviceId: String) {
        val currentJob = launch {
            val repository = DataRepository(context, deviceId)
            val poller = CoroutinePoller(repository = repository, dispatcher = Dispatchers.IO)
            val flow = poller.pollMessages(POLLING_FREQUENCY)
            Timber.i("$deviceId - startPollingMessages")
            flow.cancellable().collect { messages ->
                Timber.d("$deviceId - Received ${messages?.count()} messages")
                messages?.forEach { messageResponse ->
                    messageResponse.message?.let {
                        Fireblocks.getInstance(deviceId).handleIncomingMessage(it) { success ->
                            Timber.i("$deviceId - Handled message ${messageResponse.id}: $success")
                            repository.deleteMessage(messageResponse.id)
                        }
                    }
                }
            }
        }
        jobs[deviceId] = currentJob
    }

    fun stopPollingMessages(deviceId: String) {
        jobs[deviceId]?.cancel()
        job = Job()
        Timber.i("$deviceId - stopPollingMessages")
    }
}