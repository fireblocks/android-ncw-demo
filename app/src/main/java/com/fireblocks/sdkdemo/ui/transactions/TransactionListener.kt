package com.fireblocks.sdkdemo.ui.transactions

import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper

/**
 * Created by Fireblocks ltd. on 02/04/2023.
 */
interface TransactionListener {
    fun fireTransaction(transactionWrapper: TransactionWrapper, count: Int)
    fun clearTransactionsCount()
    fun onCreatedTransaction(createTransactionResponse: CreateTransactionResponse)
}