package com.fireblocks.sdkdemo.bl.core.server.polling

import com.fireblocks.sdkdemo.bl.core.server.models.MessageResponse
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
interface Poller {
    fun pollMessages(delay: Long): Flow<ArrayList<MessageResponse>?>
    fun pollTransactions(delay: Long): Flow<ArrayList<TransactionResponse>?>
    fun close()
}