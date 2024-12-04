package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import com.fireblocks.sdk.ew.models.Status
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdk.ew.models.TransferPeerPathType
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import java.io.Serializable

/**
 * Created by Fireblocks Ltd. on 24/11/2024.
 */
data class TransactionWrapper(val deviceId: String, var transaction: TransactionResponse? = null, var justApproved: Boolean = false) : Serializable {

    fun isOutgoingTransaction(context: Context, deviceId: String): Boolean {
        val isOutgoingTransaction: Boolean
        val walletId = StorageManager.get(context, deviceId).walletId.value()
        isOutgoingTransaction = (transaction?.source?.type == TransferPeerPathType.END_USER_WALLET && transaction?.source?.walletId == walletId)
        return isOutgoingTransaction
    }
    var supportedAsset: SupportedAsset? = null

    val txHash = transaction?.txHash
    val networkFee = transaction?.feeInfo?.networkFee
    val id = transaction?.id
    val assetId = supportedAsset?.id ?: transaction?.assetId
    val feeCurrency = transaction?.feeCurrency

    val amount = transaction?.amountInfo?.amount
    val amountUSD = transaction?.amountInfo?.amountUSD
    val createdAt = transaction?.createdAt
    val lastUpdated = transaction?.lastUpdated
    val destinationAddress = transaction?.destinationAddress
    val sourceAddress = transaction?.sourceAddress

    fun setStatus(status: Status): TransactionWrapper {
        val transactionResponse = transaction?.copy(status = status)
        return copy(transaction = transactionResponse)
    }

    fun getStatus(): SigningStatus {
        return SigningStatus.from(transaction?.status?.name)
    }

    fun setAsset(asset: SupportedAsset?) {
        this.supportedAsset = asset
    }
}

