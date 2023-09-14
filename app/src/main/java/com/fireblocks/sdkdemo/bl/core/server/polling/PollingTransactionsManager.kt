package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
object PollingTransactionsManager : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private const val POLLING_FREQUENCY = 1000L
    private val jobs = hashMapOf<String, Job>()

    fun startPolling(context: Context, deviceId: String, getAllTransactions: Boolean = false) {
        val currentJob = launch {
            if (getAllTransactions) {
                getTransactions(context, deviceId)
            }
            val repository = DataRepository(context, deviceId)
            val poller = CoroutinePoller(repository = repository, dispatcher = Dispatchers.IO)
            val flow = poller.pollTransactions(POLLING_FREQUENCY)
            flow.cancellable().collect { transactionResponses ->
                handleTransactions(deviceId, transactionResponses)
            }
        }
        jobs[deviceId] = currentJob
    }

    private fun handleTransactions(deviceId: String,
                                   transactionResponses: ArrayList<TransactionResponse>?) {
//        Timber.i("$deviceId - Received ${transactionResponses?.count()} transactionResponses") //TODO fix endless calls here
        transactionResponses?.forEach { transactionResponse ->
            val transactionWrapper = TransactionWrapper(deviceId, transactionResponse)
            FireblocksManager.getInstance().fireTransaction(transactionWrapper)
        }
    }

    fun stopPolling(deviceId: String) {
        jobs[deviceId]?.cancel()
        job = Job()
        Timber.i("$deviceId - stopPolling")
    }

    private fun getTransactions(context: Context, deviceId: String) {
        val repository = DataRepository(context, deviceId)
        val transactionResponses = repository.getTransactions(0L, arrayListOf())
        handleTransactions(deviceId, transactionResponses)
    }

    fun getAllTransactionsFromServer(context: Context, deviceId: String){
        runBlocking {
            withContext(Dispatchers.IO) {
                getTransactions(context, deviceId)
            }
        }
    }

    fun cancelTransaction(context: Context, deviceId: String, txId: String): Boolean {
        var success: Boolean
        runBlocking {
            withContext(Dispatchers.IO) {
                val repository = DataRepository(context, deviceId)
                success = repository.cancelTransaction(txId)
            }
        }
        return success
    }

    fun createTransaction(context: Context, deviceId: String, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel): CreateTransactionResponse? {
        var response: CreateTransactionResponse?
        runBlocking {
            withContext(Dispatchers.IO) {
                val repository = DataRepository(context, deviceId)
                response = repository.createTransaction(assetId, destAddress, amount, feeLevel)
            }
        }
        return response
    }
}