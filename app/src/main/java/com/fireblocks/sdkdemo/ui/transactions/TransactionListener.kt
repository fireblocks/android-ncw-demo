package com.fireblocks.sdkdemo.ui.transactions

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper

/**
 * Created by Fireblocks Ltd. on 02/04/2023.
 */
interface TransactionListener {
    fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper, count: Int) {}
    fun clearTransactionsCount() {}
}