package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.bl.core.server.models.MessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
object PollingMessagesManager : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private const val POLLING_FREQUENCY = 1000L
    private val jobs = hashMapOf<String,Job>()
    private val pollers = hashMapOf<String,CoroutinePoller>()

    fun startPollingMessages(context: Context, deviceId: String) {
        val repository = DataRepository(context, deviceId)
        val poller = CoroutinePoller(repository = repository, dispatcher = Dispatchers.IO)
        val currentJob = launch {
            val flow = poller.pollMessages(POLLING_FREQUENCY)
            Timber.i("$deviceId - startPollingMessages")
            flow.cancellable().collect { messageResponses ->
                coroutineContext.ensureActive()
                handleMessages(messageResponses, deviceId, repository)
            }
        }
        jobs[deviceId] = currentJob
        pollers[deviceId] = poller
    }

    private fun handleMessages(messageResponses: ArrayList<MessageResponse>?,
                          deviceId: String,
                          repository: DataRepository) {
        messageResponses?.let { responses ->
            if (responses.isNotEmpty()) {
                Timber.d("$deviceId - Received ${responses.count()} messageResponses")
            }
            val iterator = responses.iterator()
            while(iterator.hasNext()) {
                val messageResponse = iterator.next()
                messageResponse.message?.let {
                    Fireblocks.getInstance(deviceId).handleIncomingMessage(it) { success ->
                        Timber.i("$deviceId - Handled message ${messageResponse.id}: $success")
                        repository.deleteMessage(messageResponse.id)
                    }
                }
            }
        }
    }

    fun stopPollingMessages(deviceId: String) {
        jobs[deviceId]?.cancel()
        pollers[deviceId]?.close()
        job = Job()
        Timber.i("$deviceId - stopPollingMessages")
    }
}