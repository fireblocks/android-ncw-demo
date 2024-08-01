package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class WalletViewModel : TransactionListener, BaseViewModel(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    data class WalletUiState(
        val assets: List<SupportedAsset> = arrayListOf(),
        val balance: String = "0",
        val selectedAsset:  SupportedAsset? = null,
        val assetAmount: String = "0",
        val assetUsdAmount: String = "0",
        val sendDestinationAddress: String = "",
        val selectedFeeData: FeeData? = null,
        val createdTransactionId: String? = null,
        val createdTransaction: Boolean = false,
        val transactionWrapper: TransactionWrapper? = null,
        val transactionSignature: TransactionSignature? = null,
        val sendFlow: Boolean = false,
        val closeWarningClicked: Boolean = false,
        val transactionCanceled: Boolean = false,
        val transactionCancelFailed: Boolean = false,
        val estimatedFee : Fee? = null,
        val showFeeError: Boolean = false,
        val showPendingSignatureError: Boolean = false,
        val createdTransactionStatus: SigningStatus? = null
        )

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

    private fun onFeeError(showError: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showFeeError = showError,
            )
        }
    }

    private fun onPendingSignatureError(showPendingSignatureError: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showPendingSignatureError = showPendingSignatureError,
            )
        }
    }

    fun onAssets(assets: List<SupportedAsset>) {
        _uiState.update { currentState ->
            currentState.copy(
                assets = assets,
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

    fun onSelectedFee(feeData: FeeData) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedFeeData = feeData,
            )
        }
    }

    private fun onTransactionSignature(transactionSignature: TransactionSignature) {
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

    private fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactionWrapper?.let {
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
        val transactionResponse = transactionWrapper.transaction.copy(status = status)
        val wrapper = transactionWrapper.copy(transaction = transactionResponse)
        onTransactionSelected(wrapper)
        FireblocksManager.getInstance().updateTransaction(wrapper)
        FireblocksManager.getInstance().startPollingTransactions(context, deviceId)
    }

    fun loadAssets(context: Context, state: UiState = UiState.Loading) {
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

    fun getEstimatedFee(context: Context) {
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

    private fun onEstimatedFee(estimatedFee: Fee?) {
        _uiState.update { currentState ->
            currentState.copy(
                estimatedFee = estimatedFee
            )
        }
    }

    fun createTransaction(context: Context) {
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

    fun approve(context: Context, deviceId: String, txId: String) {
        onPendingSignatureError(false)
        showProgress(true)
        runCatching {
            val transactions = FireblocksManager.getInstance().getTransactions(context)
            // check if the transaction with txId is in the list of transactions and has status of pending signature
            val transaction = transactions.firstOrNull { it.transaction.id == txId && it.transaction.status == SigningStatus.PENDING_SIGNATURE }
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

    fun discardTransaction(context: Context, txId: String) {
        showProgress(true)
        FireblocksManager.getInstance().startPollingTransactions(context)
        runCatching {
            val deviceId = getDeviceId(context)
            val success = FireblocksManager.getInstance().cancelTransaction(context, deviceId, txId)
            onTransactionCanceled()
            onTransactionCancelFailed(!success)
            showProgress(false)
        }.onFailure {
            onError(true)
        }
    }

    private fun onTransactionCanceled() {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCanceled = true,
            )
        }
    }

    private fun onTransactionCancelFailed(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCancelFailed = value,
            )
        }
    }

    override fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper, count: Int) {
        showProgress(false)
        onTransactionReceived(transactionWrapper)
        if (transactionWrapper.transaction.status == SigningStatus.PENDING_SIGNATURE) {
            FireblocksManager.getInstance().removeTransactionListener(this)
        }
    }

    private fun onCreatedTransactionId(txId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                createdTransactionId = txId,
            )
        }
    }

    private fun onFailedToCreatedTransaction() {
        _uiState.update { currentState ->
            currentState.copy(
                createdTransaction = false,
            )
        }
        FireblocksManager.getInstance().removeTransactionListener(this)
        onError(true)
    }

    private fun onTransactionReceived(transactionWrapper: TransactionWrapper) {
        if (_uiState.value.createdTransactionId == transactionWrapper.transaction.id) {
            _uiState.update { currentState ->
                currentState.copy(
                    transactionWrapper = transactionWrapper,
                    createdTransactionStatus = transactionWrapper.transaction.status,
                    createdTransaction = true
                )
            }
        }
    }

    fun getAsset(assetId: String): SupportedAsset? {
        return _uiState.value.assets.firstOrNull { it.id == assetId }
    }

    companion object {
        internal const val GET_TRANSACTION_ITERATIONS = 6
        internal const val DELAY: Long = 1000
    }
}