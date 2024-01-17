package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import kotlinx.coroutines.flow.Flow

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
interface Poller {
    fun pollTransactions(context: Context, delay: Long): Flow<ArrayList<TransactionResponse>?>
    fun close()
}