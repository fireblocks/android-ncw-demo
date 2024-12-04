package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class WalletViewModel : BaseWalletViewModel() {

    override fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper, count: Int) {
        showProgress(false)
        onTransactionReceived(transactionWrapper)
        if (transactionWrapper.getStatus() == SigningStatus.PENDING_SIGNATURE) {
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
                feeLevel = selectedFeeData?.feeLevel ?: FeeLevel.MEDIUM
                selectedAsset?.run {
                    assetId = id
                }
            }
            val deviceId = getDeviceId(context)
            Timber.i("$deviceId - createTransaction with assetId:$assetId, destAddress:$destAddress, amount:$amount, feeLevel:$feeLevel started")
            FireblocksManager.getInstance().createTransaction(context, assetId, destAddress, amount, feeLevel) { createTransactionResponse ->
                Timber.i("$deviceId - createTransaction with txId ${createTransactionResponse?.id} assetId:$assetId, destAddress:$destAddress, amount:$amount, feeLevel:$feeLevel completed with status: ${createTransactionResponse?.status}")
                val allowedStatuses = arrayListOf(SigningStatus.SUBMITTED, SigningStatus.PENDING_AML_SCREENING, SigningStatus.PENDING_SIGNATURE)
                if (createTransactionResponse == null || createTransactionResponse.id.isNullOrEmpty() || !allowedStatuses.contains(createTransactionResponse.status)) {
                    Timber.e("Failed to create transaction, response: $createTransactionResponse")
                    onFailedToCreatedTransaction()
                } else {
                    onCreatedTransactionId(createTransactionResponse.id)
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to create transaction")
            onFailedToCreatedTransaction()
        }
    }

    override fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactionWrapper?.let {
            if (transactionSignature.transactionSignatureStatus.hasFailed()){
                showError()
                handleApprovedTransaction(context, deviceId, it, SigningStatus.FAILED)
            } else {
                it.justApproved = true
                launch {
                    runBlocking {
                        withContext(coroutineContext) {
                            var count = 0
                            var status = SigningStatus.BROADCASTING
                            while (count < GET_TRANSACTION_ITERATIONS) {
                                count++
                                delay(DELAY)
                                val transactionResponses = DataRepository(context, deviceId).getTransactions(System.currentTimeMillis())
                                val transactionResponse = transactionResponses?.body()?.firstOrNull { transactionResponse ->
                                    transactionResponse.id == transactionSignature.txId
                                }
                                status = transactionResponse?.status ?: SigningStatus.BROADCASTING
                                Timber.d("updateTransactionStatus status: $status")
                                if (transactionResponse?.status != SigningStatus.PENDING_SIGNATURE) {
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

    private fun handleApprovedTransaction(context: Context, deviceId: String, transactionWrapper: TransactionWrapper, status: SigningStatus) {
        val wrapper = transactionWrapper.setStatus(status)
        onTransactionSelected(wrapper)
        FireblocksManager.getInstance().updateTransaction(wrapper)
        FireblocksManager.getInstance().startPollingTransactions(context, deviceId)
    }

    override fun loadAssets(context: Context, state: UiState) {
        updateUserFlow(state)
        runCatching {
            FireblocksManager.getInstance().getAssetsSummary(context) { assets ->
                showProgress(false)

                onAssets(assets = assets)
                var balance = 0.0
                assets.forEach {
                    if (it.price.isNotNullAndNotEmpty()) {
                        balance += it.price.toDouble()
                    }
                }
                onBalance(balance.roundToDecimalFormat())
            }
        }.onFailure {
            showProgress(false)
        }
    }

    override fun getEstimatedFee(context: Context) {
        onFeeError(false)
        showProgress(true)
        runCatching {
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
            FireblocksManager.getInstance().getEstimatedFee(context, assetId, destAddress, amount) { estimatedFeeResponse ->
                val fee = estimatedFeeResponse?.fee
                onFeeError(fee == null)
                showProgress(false)
                onEstimatedFee(fee ?: Fee())
            }
        }.onFailure {
            Timber.e(it)
            onFeeError(true)
            showProgress(false)
            onEstimatedFee(Fee())
        }
    }
}