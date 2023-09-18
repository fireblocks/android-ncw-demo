package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdk.transactions.TransactionSignatureStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 23/07/2023.
 */
class TransfersViewModel: TransactionListener, BaseViewModel(){

    private val _uiState = MutableStateFlow(TransfersUiState())
    val uiState: StateFlow<TransfersUiState> = _uiState.asStateFlow()

    init {
        FireblocksManager.getInstance().addTransactionListener(this)
    }

    data class TransfersUiState(
        val transactions: HashSet<TransactionWrapper> = hashSetOf(),
        val transactionCanceled: Boolean = false,
        val transactionSignature: TransactionSignature? = null,
    )

    fun onTransactions(value: HashSet<TransactionWrapper>){
        _uiState.update { currentState ->
            currentState.copy(
                transactions = value,
            )
        }
    }

    fun onTransactionSelected(transactionWrapper: TransactionWrapper) {
        _uiState.update { currentState ->
            val transactions = currentState.transactions
            transactions.remove(transactionWrapper)
            transactions.add(transactionWrapper)
            currentState.copy(
                transactions = transactions
            )
        }
    }

    fun loadTransactions() {
        val transactions = FireblocksManager.getInstance().getTransactions()
        onTransactions(transactions)
    }

    override fun fireTransaction(transactionWrapper: TransactionWrapper, count: Int) {
        loadTransactions()
    }

    override fun clearTransactionsCount() {
    }

    override fun onCreatedTransaction(createTransactionResponse: CreateTransactionResponse) {
    }

    fun onTransactionSignature(transactionSignature: TransactionSignature) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionSignature = transactionSignature,
            )
        }
    }

    private fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature) {
        _uiState.value.transactions.find { it.transaction.id == transactionSignature.txId }?.let {
            val status = when (transactionSignature.transactionSignatureStatus) {
                TransactionSignatureStatus.COMPLETED -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            val transactionResponses = DataRepository(context, deviceId).getTransactions(System.currentTimeMillis())
                            val transactionResponse = transactionResponses?.find { transactionResponse ->
                                transactionResponse.id == transactionSignature.txId
                            }
                            transactionResponse?.status ?: SigningStatus.CONFIRMING
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
        }
    }

    fun approve(context: Context, txId: String) {
        showProgress(true)
        runCatching {
            val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
            Fireblocks.getInstance(deviceId).signTransaction(txId) {
                onTransactionSignature(it)
                updateTransactionStatus(context, deviceId, it)
                showProgress(false)
            }
        }.onFailure {
            onError(true)
        }
    }

    fun deny(context: Context, txId: String) {
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
}