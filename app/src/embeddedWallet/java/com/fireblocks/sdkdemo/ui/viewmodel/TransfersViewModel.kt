package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.ew.models.Status
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.viewmodel.BaseWalletViewModel.Companion.DELAY
import com.fireblocks.sdkdemo.ui.viewmodel.BaseWalletViewModel.Companion.GET_TRANSACTION_ITERATIONS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 27/11/2024.
 */
class TransfersViewModel: BaseTransfersViewModel() {

    private fun handleApprovedTransaction(context: Context, transactionWrapper: TransactionWrapper, status: Status) {
        val wrapper = transactionWrapper.setStatus(status)
        onTransactionSelected(wrapper)
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.updateTransaction(wrapper)
        fireblocksManager.startPollingTransactions(context)
    }

    override fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactions.find { it.id == transactionSignature.txId }?.let {
            if (transactionSignature.transactionSignatureStatus.hasFailed()) {
                showError()
                handleApprovedTransaction(context, it, Status.FAILED)
            } else {
                it.justApproved = true
                launch {
                    withContext(coroutineContext) {
                        var count = 0
                        var status = Status.BROADCASTING
                        while (count < GET_TRANSACTION_ITERATIONS) {
                            count++
                            delay(DELAY)
                            val result = FireblocksManager.getInstance().getTransactionById(viewModel = this@TransfersViewModel, transactionId = transactionSignature.txId)
                            if (result.isSuccess) {
                                val transactionResponse = result.getOrNull()
                                status = transactionResponse?.status ?: Status.BROADCASTING
                                Timber.d("updateTransactionStatus status: $status")
                                if (transactionResponse?.status != Status.PENDING_SIGNATURE) {
                                    break
                                }
                            }
                        }
                        handleApprovedTransaction(context, it, status)
                    }
                }
            }
        }
    }

    override fun deny(context: Context, txId: String) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                val result = FireblocksManager.getInstance().cancelTransaction(viewModel = this@TransfersViewModel, txId = txId)
                val success = result.isSuccess
                showProgress(false)
                if (result.isFailure) {
                    showError(result.exceptionOrNull())
                }
                onTransactionCanceled(success)
            }
        }
    }
}