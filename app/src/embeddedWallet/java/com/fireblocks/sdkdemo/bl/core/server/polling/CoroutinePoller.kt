package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdk.ew.models.PaginatedResponse
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

/**
 * see [this](https://proandroiddev.com/polling-with-kotlin-channels-flows-1a69e94fdfe9)
 */
@OptIn(DelicateCoroutinesApi::class)
class CoroutinePoller(
    val context: Context,
    val repository: DataRepository,
    val dispatcher: CoroutineDispatcher
): Poller {

    private var cancelled = false

    override fun pollTransactions(delay: Long): Flow<PaginatedResponse<TransactionResponse>?> {
        return channelFlow {
            while (!isClosedForSend) {
                if (cancelled){
                    close()
                } else {
                    delay(delay)
                    var after = 0L
                    val transactions = FireblocksManager.getInstance().getTransactions(context)
                    if (transactions.isNotEmpty()) {
                        after = getLastUpdatedTimestamp(transactions)
                    }
                    // Make the calls in parallel
                    val sourceDataDeferred = async { repository.getTransactions(outgoing = true, after = after) }
                    val destinationDataDeferred = async { repository.getTransactions(incoming = true, after = after) }

                    // Await both results
                    val (sourceData, destinationData) = awaitAll(sourceDataDeferred, destinationDataDeferred)

                    val paginationData = sourceData?.data.orEmpty() + destinationData?.data.orEmpty()
                    val data = PaginatedResponse(
                        paginationData,
                    )
                    send(data)
                }
            }
        }.flowOn(dispatcher)
    }

    /**
     * Get the last updated timestamp of the transactions.
     * If there are non-final transactions, return the minimum timestamp of them.
     * Otherwise, return the maximum createdAt timestamp of all transactions.
     */
    private fun getLastUpdatedTimestamp(transactions: HashSet<TransactionWrapper>): Long {
        var minNonFinal: Long? = null
        var maxCreatedAt: Long = 0
        transactions.forEach { tx ->
            tx.createdAt?.let { createdAt ->
                if (!isFinalStatus(tx.getStatus())) {
                    minNonFinal = minNonFinal?.let { minOf(it, createdAt) } ?: createdAt
                }
                if (createdAt > maxCreatedAt) {
                    maxCreatedAt = createdAt
                }
            }
        }
        if (minNonFinal != null) {
            return minNonFinal!!
        }
        return maxCreatedAt + 1
    }

    private fun isFinalStatus(status: SigningStatus?): Boolean {
        return when(status) {
            SigningStatus.COMPLETED, SigningStatus.FAILED, SigningStatus.CANCELLED -> true
            else -> false
        }
    }

    override fun close() {
        dispatcher.cancel()
        cancelled = true
    }
}