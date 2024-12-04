package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.ew.models.FeeLevel
import com.fireblocks.sdk.ew.models.Status
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.convertToFeeLevel
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.extensions.toFee
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class WalletViewModel : BaseWalletViewModel() {

    override fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper, count: Int) {
        showProgress(false)
        onTransactionReceived(transactionWrapper)
        if (transactionWrapper.transaction?.status == Status.PENDING_SIGNATURE) {
            FireblocksManager.getInstance().removeTransactionListener(this)
        }
    }

    override fun onTransactionReceived(transactionWrapper: TransactionWrapper) {
        if (_uiState.value.createdTransactionId == transactionWrapper.id) {
            _uiState.update { currentState ->
                currentState.copy(
                    transactionWrapper = transactionWrapper,
                    createdTransactionStatus = transactionWrapper.getStatus(),
                    createdTransaction = true
                )
            }
        }
    }

    override fun getEstimatedFee(context: Context) {
        onFeeError(false)
        showProgress(true)
        var assetId = ""
        var destAddress: String
        var amount: String
        _uiState.value.run {
            destAddress = sendDestinationAddress
            amount = assetAmount
            selectedAsset?.run {
                assetId = id
            }
        }
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().estimateTransactionFee(assetId, destAddress, amount, viewModel = this@WalletViewModel).onSuccess { estimatedTransactionFeeResponse ->
                    showProgress(false)
                    val fee = estimatedTransactionFeeResponse.toFee()
                    onEstimatedFee(fee)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    override fun createTransaction(context: Context) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().addTransactionListener(this)
            var assetId = ""
            var destAddress: String
            var amount: String
            var feeLevel: FeeLevel
            _uiState.value.run {
                destAddress = sendDestinationAddress
                amount = assetAmount
                feeLevel = convertToFeeLevel(selectedFeeData?.feeLevel?.name)
                selectedAsset?.run {
                    assetId = id
                }
            }
            val deviceId = getDeviceId(context)
            Timber.i("$deviceId - createTransaction with assetId:$assetId, destAddress:$destAddress, amount:$amount, feeLevel:$feeLevel started")
            launch {
                withContext(coroutineContext) {
                    FireblocksManager.getInstance().createOneTimeAddressTransaction(assetId, destAddress, amount, feeLevel, viewModel = this@WalletViewModel).onSuccess { createTransactionResponse ->
                        Timber.i("$deviceId - createTransaction with txId ${createTransactionResponse?.id} assetId:$assetId, destAddress:$destAddress, amount:$amount, feeLevel:$feeLevel completed with status: ${createTransactionResponse?.status}")
                        val allowedStatuses = arrayListOf(Status.SUBMITTED, Status.PENDING_AML_SCREENING, Status.PENDING_SIGNATURE)
                        if (createTransactionResponse.id.isNullOrEmpty() || !allowedStatuses.contains(createTransactionResponse.status)) {
                            Timber.e("Failed to create transaction, response: $createTransactionResponse")
                            onFailedToCreatedTransaction()
                        } else {
                            onCreatedTransactionId(createTransactionResponse.id!!)
                        }
                    }.onFailure {
                        Timber.e(it, "Failed to create transaction")
                        onFailedToCreatedTransaction(it)
                    }

                }
            }
        }.onFailure {
            Timber.e(it, "Failed to create transaction")
            onFailedToCreatedTransaction(it)
        }
    }

    override fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactionWrapper?.let {
            if (transactionSignature.transactionSignatureStatus.hasFailed()) {
                showError()
                handleApprovedTransaction(context, deviceId, it, Status.FAILED)
            } else {
                it.justApproved = true
                launch {
                    withContext(coroutineContext) {
                        var count = 0
                        var status = Status.BROADCASTING
                        while (count < GET_TRANSACTION_ITERATIONS) {
                            count++
                            delay(DELAY)
                            val result = FireblocksManager.getInstance().getTransactionById(viewModel = this@WalletViewModel, transactionId = transactionSignature.txId)
                            if (result.isSuccess) {
                                val transactionResponse = result.getOrNull()
                                status = transactionResponse?.status ?: Status.BROADCASTING
                                Timber.d("updateTransactionStatus status: $status")
                                if (transactionResponse?.status == Status.PENDING_SIGNATURE) {
                                    break
                                }
                            }
                        }
                        handleApprovedTransaction(context, deviceId, it, status)
                    }
                }
            }
        }
    }

    private fun handleApprovedTransaction(context: Context, deviceId: String, transactionWrapper: TransactionWrapper, status: Status) {
        val wrapper = transactionWrapper.setStatus(status)
        onTransactionSelected(wrapper)
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.updateTransaction(wrapper)
        fireblocksManager.startPollingTransactions(context, deviceId)
    }

    override fun loadAssets(context: Context, state: UiState) {
        updateUserFlow(state)
        launch {
            withContext(coroutineContext) {
                runCatching {
                    val fireblocksManager = FireblocksManager.getInstance()
                    val supportedAssets = fireblocksManager.getAssetsSummary(context = context, viewModel = this@WalletViewModel)
                    showProgress(false)
                    onAssets(assets = supportedAssets)

                    var balance = 0.0
                    supportedAssets.forEach {
                        if (it.price.isNotNullAndNotEmpty()) {
                            balance += it.price.toDouble()
                        }
                    }
                    onBalance(balance.roundToDecimalFormat())
                }.onFailure {
                    showProgress(false)
                }
            }
        }
    }
}