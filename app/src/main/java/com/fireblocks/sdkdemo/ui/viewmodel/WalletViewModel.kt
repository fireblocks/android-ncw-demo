package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdk.transactions.TransactionSignatureStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class WalletViewModel : TransactionListener, BaseViewModel() {

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
        val createdTransaction: Boolean = false,
        val transactionWrapper: TransactionWrapper? = null,
        val transactionSignature: TransactionSignature? = null,
        val sendFlow: Boolean = false,
        val closeWarningClicked: Boolean = false,
        val transactionCanceled: Boolean = false,
        val estimatedFee : Fee? = null,
        val showFeeError: Boolean = false,
        )

    fun clean(){
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
                showFeeError = false
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

    private fun onCreatedTransaction(createdTransaction: Boolean, transactionWrapper: TransactionWrapper? = null) {
        _uiState.update { currentState ->
            currentState.copy(
                createdTransaction = createdTransaction,
                transactionWrapper = transactionWrapper
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

    private fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactionWrapper?.let {
            val status = when (transactionSignature.transactionSignatureStatus) {
                TransactionSignatureStatus.COMPLETED -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            var count = 0
                            var status = SigningStatus.BROADCASTING
                            while(count < 3){
                                count++
                                delay(2000)
                                val transactionResponses = DataRepository(context, deviceId).getTransactions(System.currentTimeMillis())
                                val transactionResponse = transactionResponses?.find { transactionResponse ->
                                    transactionResponse.id == transactionSignature.txId
                                }
                                status = transactionResponse?.status ?: SigningStatus.BROADCASTING
                                Timber.d("updateTransactionStatus status: $status")
                                if (transactionResponse?.status != SigningStatus.PENDING_SIGNATURE){
                                    break
                                }
                            }
                            status
                        }
                    }
                }
                else -> {
                    onError(true)
                    SigningStatus.FAILED
                }
            }
            val transactionResponse = it.transaction.copy(status = status)
            val transactionWrapper = it.copy(transaction = transactionResponse)
            onTransactionSelected(transactionWrapper)
            FireblocksManager.getInstance().updateTransaction(transactionWrapper)
        }
    }

    fun loadAssets(context: Context) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().getAssets(context) { assets ->
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
            FireblocksManager.getInstance().createTransaction(context, assetId, destAddress, amount, feeLevel) { createTransactionResponse ->
                if (createTransactionResponse == null || createTransactionResponse.id.isNullOrEmpty() || createTransactionResponse.status != SigningStatus.SUBMITTED){
                    onError(true)
                    onCreatedTransaction(false)
                }
            }
        }.onFailure {
            Timber.e(it)
            onError(true)
            onCreatedTransaction(false)
        }
    }

    fun approve(context: Context, deviceId: String, txId: String) {
        showProgress(true)
        runCatching {
            Fireblocks.getInstance(deviceId).signTransaction(txId) {
                updateTransactionStatus(context, deviceId, it)
                showProgress(false)
                onTransactionSignature(it)
            }
        }.onFailure {
            showProgress(false)
        }
    }

    fun discardTransaction(context: Context, txId: String) {
        showProgress(true)
        runCatching {
            val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
            val success = FireblocksManager.getInstance().cancelTransaction(context, deviceId, txId)
            onTransactionCanceled(success)
            showProgress(false)

        }.onFailure {
            onError(true)
        }
    }

    private fun onTransactionCanceled(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCanceled = value,
            )
        }
    }

    override fun fireTransaction(transactionWrapper: TransactionWrapper, count: Int) {
        Timber.v("Got transaction $transactionWrapper")
        showProgress(false)
        onCreatedTransaction(true, transactionWrapper)
        FireblocksManager.getInstance().removeTransactionListener(this)
    }

    override fun clearTransactionsCount() {
    }

    override fun onCreatedTransaction(createTransactionResponse: CreateTransactionResponse) {
    }
}