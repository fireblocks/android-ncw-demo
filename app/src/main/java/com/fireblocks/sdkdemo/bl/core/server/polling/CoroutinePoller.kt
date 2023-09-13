package com.fireblocks.sdkdemo.bl.core.server.polling

import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.server.models.MessageResponse
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

/**
 * see [this](https://proandroiddev.com/polling-with-kotlin-channels-flows-1a69e94fdfe9)
 */
class CoroutinePoller(
    val repository: DataRepository,
    val dispatcher: CoroutineDispatcher
): Poller {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun pollMessages(delay: Long): Flow<ArrayList<MessageResponse>?> {
        return channelFlow {
            while (!isClosedForSend) {
//                delay(delay)
                val data = repository.getMessages()
                send(data)
            }
        }.flowOn(dispatcher)
    }

    override fun pollTransactions(delay: Long): Flow<ArrayList<TransactionResponse>?> {
        return channelFlow {
            while (!isClosedForSend) {
//                delay(delay)
                var lastUpdated = 0L
                val transactions = FireblocksManager.getInstance().getTransactions()
                if (transactions.isNotEmpty()) {
                    lastUpdated = transactions.maxByOrNull { it.transaction.lastUpdated ?: 0L }?.transaction?.lastUpdated ?: 0L
                }
                val data = repository.getTransactions(startTimeInMillis = lastUpdated)
                send(data)
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
    }
}