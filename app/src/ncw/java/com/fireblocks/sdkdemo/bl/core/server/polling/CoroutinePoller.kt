package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 * see [this](https://proandroiddev.com/polling-with-kotlin-channels-flows-1a69e94fdfe9)
 */
@OptIn(DelicateCoroutinesApi::class)
class CoroutinePoller(
    val context: Context,
    private val repository: DataRepository,
    private val dispatcher: CoroutineDispatcher
): Poller {

    private var cancelled = false

    override fun pollTransactions(delay: Long): Flow<ArrayList<TransactionResponse>?> {
        return channelFlow {
            while (!isClosedForSend) {
                if (cancelled){
                    close()
                } else {
                    var lastUpdated = 0L
                    val transactions = FireblocksManager.getInstance().getTransactions(context)
                    if (transactions.isNotEmpty()) {
                        lastUpdated = transactions.maxByOrNull { it.lastUpdated ?: 0L }?.transaction?.lastUpdated ?: 0L
                    }
                    val response = repository.getTransactions(startTimeInMillis = lastUpdated)
                    val data = response?.body()
                    if (response?.isSuccessful == false) {
                        delay(delay)
                    }
                    send(data)
                }
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
        cancelled = true
    }
}