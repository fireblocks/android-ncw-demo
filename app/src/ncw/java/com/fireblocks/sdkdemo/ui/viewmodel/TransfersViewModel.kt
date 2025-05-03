package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 23/07/2023.
 */
class TransfersViewModel: BaseTransfersViewModel() {

    private fun handleApprovedTransaction(context: Context, deviceId: String, transactionWrapper: TransactionWrapper, status: SigningStatus) {
        val wrapper = transactionWrapper.setStatus(status)
        onTransactionSelected(wrapper)
        FireblocksManager.getInstance().updateTransaction(wrapper)
        FireblocksManager.getInstance().startPollingTransactions(context)
    }

    override fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactions.find { it.id == transactionSignature.txId }?.let {
            if (transactionSignature.transactionSignatureStatus.hasFailed()){
                showError()
                handleApprovedTransaction(context, deviceId, it, SigningStatus.FAILED)
            } else {
                it.justApproved = true
                launch {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            var count = 0
                            var status = SigningStatus.BROADCASTING
                            while (count < BaseWalletViewModel.GET_TRANSACTION_ITERATIONS) {
                                count++
                                delay(BaseWalletViewModel.DELAY)
                                val transactionResponses = DataRepository(context, deviceId).getTransactions(System.currentTimeMillis())
                                val transactionResponse = transactionResponses?.body()?.firstOrNull { transactionResponse ->
                                    transactionResponse.id == transactionSignature.txId
                                }
                                status = transactionResponse?.status ?: SigningStatus.BROADCASTING
                                Timber.d("updateTransactionStatus status: $status")
                                if (transactionResponse?.status == SigningStatus.PENDING_SIGNATURE) {
                                    break
                                }
                            }
                            handleApprovedTransaction(context, deviceId, it, status)
                        }
                    }
                }
            }
        }
    }

    override fun deny(context: Context, txId: String) {
        showProgress(true)
        runCatching {
            val deviceId = getDeviceId(context)
            val success = FireblocksManager.getInstance().cancelTransaction(context, deviceId, txId)
            onTransactionCanceled(success)
            showProgress(false)
        }.onFailure {
            showError()
        }
    }
}