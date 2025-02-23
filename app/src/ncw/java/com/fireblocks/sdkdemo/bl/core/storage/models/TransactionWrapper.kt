package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import java.io.Serializable


/**
 * Created by Fireblocks Ltd. on 19/04/2023.
 */
data class TransactionWrapper(val deviceId: String, var transaction: TransactionResponse? = null, var justApproved: Boolean = false): Serializable {

    fun isOutgoingTransaction(context: Context, deviceId: String): Boolean {
        var isSentTransaction = false
        transaction?.details?.let { transactionDetails ->
            val walletId = StorageManager.get(context, deviceId).walletId.value()
            isSentTransaction =  (transactionDetails.source?.type == "END_USER_WALLET" && transactionDetails.source.walletId == walletId)
        }
        return isSentTransaction
    }

    val txHash = transaction?.details?.txHash
    val networkFee = transaction?.details?.networkFee
    val id = transaction?.id
    val assetId = transaction?.details?.assetId
    val feeCurrency = transaction?.details?.feeCurrency
    val amount = transaction?.details?.amountInfo?.amount
    val amountUSD = transaction?.details?.amountInfo?.amountUSD
    val createdAt = transaction?.createdAt
    val lastUpdated = transaction?.lastUpdated
    val asset = transaction?.details?.asset
    val destinationAddress = transaction?.details?.destinationAddress
    val sourceAddress = transaction?.details?.sourceAddress

    fun setStatus(status: SigningStatus): TransactionWrapper {
        val transactionResponse = transaction?.copy(status = status)
        val wrapper = copy(transaction = transactionResponse)
        return wrapper
    }

    fun getStatus() = transaction?.status

    fun setAsset(asset: SupportedAsset?) {
        transaction?.details?.asset = asset
    }

    val assetName = assetId?.let { if (it.startsWith("NFT")) "NFT" else it } ?: ""
    val blockchainSymbol = assetId?.let { if (it.startsWith("NFT")) feeCurrency else it } ?: ""
}
