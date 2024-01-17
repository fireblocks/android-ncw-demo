package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
object PollingTransactionsManager : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private const val POLLING_FREQUENCY = 5000L
    private val jobs = hashMapOf<String, Job>()
    private val pollers = hashMapOf<String, CoroutinePoller>()

    fun startPollingTransactions(context: Context, deviceId: String, getAllTransactions: Boolean = false) {
        val repository = DataRepository(context, deviceId)
        val poller = CoroutinePoller(repository = repository, dispatcher = Dispatchers.IO)
        val currentJob = launch {
            if (getAllTransactions) {
                getTransactions(context, deviceId)
            }
            Timber.i("$deviceId - startPollingTransactions")

            val flow = poller.pollTransactions(context, POLLING_FREQUENCY)
            flow.cancellable().collect { transactionResponses ->
                coroutineContext.ensureActive()
                handleTransactions(context, deviceId, transactionResponses)
            }
        }
        jobs[deviceId] = currentJob
        pollers[deviceId] = poller
    }

    private fun handleTransactions(context: Context, deviceId: String,
                                   transactionResponses: ArrayList<TransactionResponse>?) {
        transactionResponses?.let { responses ->
            if (responses.isNotEmpty()) {
                if (FireblocksManager.getInstance().isDebugLog()) {
                    Timber.d("$deviceId - Received ${responses.count()} transactionResponses")
                }
            }
            val iterator = responses.iterator()
            while (iterator.hasNext()) {
                val transactionResponse = iterator.next()
                val transactionWrapper = TransactionWrapper(deviceId, transactionResponse)
                FireblocksManager.getInstance().fireTransaction(context, transactionWrapper)
            }
        }
    }

    private fun getTransactions(context: Context, deviceId: String) {
        val repository = DataRepository(context, deviceId)
        val transactionResponses = repository.getTransactions(0L, arrayListOf())
        handleTransactions(context, deviceId, transactionResponses?.body())
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

    fun stopPollingTransactions(deviceId: String) {
        jobs[deviceId]?.cancel()
        pollers[deviceId]?.close()
        job = Job()
        Timber.i("$deviceId - stopPollingTransactions")
    }
}