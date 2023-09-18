package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import java.io.Serializable


/**
 * Created by Fireblocks ltd. on 19/04/2023.
 */
data class TransactionWrapper(val deviceId: String, var transaction: TransactionResponse): Serializable {

    fun getPreviewTitle(context: Context): String? {
        return transaction.details?.operation
    }

    fun getCreatedAtTimestamp(context: Context): String {
        return context.getString(R.string.created_at, transaction.createdAt?.toFormattedTimestamp(context, R.string.date_timestamp) ?: "")
    }

    fun getUpdatedAtTimestamp(context: Context): String {
        return context.getString(R.string.updated_at, transaction.lastUpdated?.toFormattedTimestamp(context, R.string.date_timestamp) ?: "")
    }

    fun isOutgoingTransaction(context: Context, deviceId: String): Boolean {
        var isSentTransaction = false
        transaction.details?.let { transactionDetails ->
            val walletId = StorageManager.get(context, deviceId).walletId.value()
            isSentTransaction =  (transactionDetails.source?.type == "END_USER_WALLET" && transactionDetails.source.walletId == walletId)
        }
        return isSentTransaction
    }
}
