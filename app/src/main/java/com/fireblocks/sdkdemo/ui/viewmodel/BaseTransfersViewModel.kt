package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
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
 * Created by Fireblocks Ltd. on 23/07/2023.
 */
abstract class BaseTransfersViewModel: TransactionListener, BaseViewModel(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    val _uiState = MutableStateFlow(TransfersUiState())
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

    fun loadTransactions(context: Context) {
        val transactions = FireblocksManager.getInstance().getTransactions(context)
        onTransactions(transactions)
    }

    override fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper, count: Int) {
        loadTransactions(context)
    }

    private fun onTransactionSignature(transactionSignature: TransactionSignature) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionSignature = transactionSignature,
            )
        }
    }

    abstract fun updateTransactionStatus(context: Context, deviceId: String, transactionSignature: TransactionSignature)

    fun approve(context: Context, txId: String) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().stopPollingTransactions()
            val start = System.currentTimeMillis()
            val deviceId = getDeviceId(context)
            Fireblocks.getInstance(deviceId).signTransaction(txId) {
                Timber.w("Demo The operation Fireblocks.signTransaction took ${System.currentTimeMillis() - start} ms")
                showProgress(false)
                onTransactionSignature(it)
                updateTransactionStatus(context, deviceId, it)
            }
        }.onFailure {
            showError()
        }
    }

    fun deny(context: Context, txId: String) {
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

    private fun onTransactionCanceled(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                transactionCanceled = value,
            )
        }
    }
}