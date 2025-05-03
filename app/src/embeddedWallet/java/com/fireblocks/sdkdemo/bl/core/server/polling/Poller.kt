package com.fireblocks.sdkdemo.bl.core.server.polling

import com.fireblocks.sdk.ew.models.PaginatedResponse
import com.fireblocks.sdk.ew.models.TransactionResponse
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
interface Poller {
    fun pollTransactions(delay: Long): Flow<PaginatedResponse<TransactionResponse>?>
    fun close()
}