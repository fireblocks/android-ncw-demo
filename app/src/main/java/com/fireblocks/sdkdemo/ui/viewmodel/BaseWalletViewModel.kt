package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 26/11/2024.
 */
abstract class BaseWalletViewModel : TransactionListener, BaseViewModel(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    override fun clean(){
        super.clean()
        _uiState.update { WalletUiState() }
    }

    fun cleanBeforeNewFlow(){
        _uiState.update { currentState ->
            currentState.copy(
                selectedAsset = null,
                createdTransaction = false,
                transactionWrapper = null,
                transactionSignature = null,
                transactionCanceled = false,
                transactionCancelFailed = false,
                showFeeError = false,
                showPendingSignatureError = false,
                createdTransactionId = null,
                createdTransactionStatus = null,
            )
        }
    }

    fun onFeeError(showError: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showFeeError = showError,
            )
        }
    }

    fun onEstimatedFee(estimatedFee: Fee?) {
        _uiState.update { currentState ->
            currentState.copy(
                estimatedFee = estimatedFee
            )
        }
    }


    fun onPendingSignatureError(showPendingSignatureError: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showPendingSignatureError = showPendingSignatureError,
            )
        }
    }

    fun onAssets(assets: List<SupportedAsset>) {
        val sortedAssets = assets.sortedBy { it.id }
        _uiState.update { currentState ->
            currentState.copy(
                assets = sortedAssets,
            )
        }
    }

    fun onBalance(balance: String) {
        _uiState.update { currentState ->
            currentState.copy(
                balance = balance,
            )
        }
    }

    fun onSelectedAsset(asset: SupportedAsset) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedAsset = asset,
            )
        }
    }

    fun onSelectedNFT(nftWrapper: NFTWrapper) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedNFT = nftWrapper,
            )
        }
    }

    fun onAssetAmount(amount: String) {
        _uiState.update { currentState ->
            currentState.copy(
                assetAmount = amount,
            )
        }
    }

    fun onAssetUsdAmount(assetUsdAmount: String) {
        _uiState.update { currentState ->
            currentState.copy(
                assetUsdAmount = assetUsdAmount,
            )
        }
    }

    fun onSendDestinationAddress(address: String) {
        _uiState.update { currentState ->
            currentState.copy(
                sendDestinationAddress = address,
            )
        }
    }

    fun onTransactionSignature(transactionSignature: TransactionSignature) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionSignature = transactionSignature,
            )
        }
    }

    fun onTransactionSelected(transactionWrapper: TransactionWrapper) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionWrapper = transactionWrapper
            )
        }
    }

    fun onSendFlow(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                sendFlow = value
            )
        }
    }

    fun onCloseWarningClicked(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                closeWarningClicked = value
            )
        }
    }

    abstract fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature)

    abstract fun discardTransaction(context: Context, txId: String)

    fun onTransactionCanceled() {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCanceled = true,
            )
        }
    }

    fun onTransactionCancelFailed(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCancelFailed = value,
            )
        }
    }

    fun onCreatedTransactionId(txId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                createdTransactionId = txId,
            )
        }
    }

    fun onFailedToCreatedTransaction(throwable: Throwable? = null) {
        _uiState.update { currentState ->
            currentState.copy(
                createdTransaction = false,
            )
        }
        FireblocksManager.getInstance().removeTransactionListener(this)
        showError(throwable = throwable)
    }

    fun getAsset(assetId: String): SupportedAsset? {
        return _uiState.value.assets.firstOrNull { it.id == assetId }
    }

    fun onSelectedFee(feeData: FeeData) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedFeeData = feeData,
            )
        }
    }

    fun approve(context: Context, deviceId: String, txId: String) {
        onPendingSignatureError(false)
        showProgress(true)
        runCatching {
            val transactions = FireblocksManager.getInstance().getTransactions(context)
            // check if the transaction with txId is in the list of transactions and has status of pending signature
            val transaction = transactions.firstOrNull { it.id == txId && it.getStatus() == SigningStatus.PENDING_SIGNATURE }
            if (transaction == null) {
                showProgress(false)
                onPendingSignatureError(true)
                return
            }
            FireblocksManager.getInstance().stopPollingTransactions()
            Timber.i("$deviceId - signTransaction with txId:$txId started")
            val start = System.currentTimeMillis()
            Fireblocks.getInstance(deviceId).signTransaction(txId) {
                Timber.w("Demo The operation Fireblocks.signTransaction took ${System.currentTimeMillis() - start} ms")
                Timber.i("$deviceId - signTransaction with txId:$txId completed")
                showProgress(false)
                onTransactionSignature(it)
                updateTransactionStatus(context, deviceId, it)
            }
        }.onFailure {
            showError()
        }
    }

    companion object {
        internal const val GET_TRANSACTION_ITERATIONS = 6
        internal const val DELAY: Long = 1000
    }

    abstract fun onTransactionReceived(transactionWrapper: TransactionWrapper)
    abstract fun getEstimatedFee(context: Context)
    abstract fun loadAssets(context: Context, state: UiState = UiState.Loading)
    abstract fun createTransaction(context: Context)
}