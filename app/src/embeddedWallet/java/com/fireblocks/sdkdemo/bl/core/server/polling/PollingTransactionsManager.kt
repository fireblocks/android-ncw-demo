package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdk.ew.EmbeddedWallet
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Ofir Barzilay on 09/03/2023.
 */
object PollingTransactionsManager : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private const val POLLING_FREQUENCY = 5000L
    private val jobs = hashMapOf<String, Job>()
    private val pollers = hashMapOf<String, CoroutinePoller>()

    fun startPollingTransactions(context: Context, deviceId: String, accountId: Int, embeddedWallet: EmbeddedWallet) {
        Timber.i("$deviceId - startPollingTransactions")
        val repository = DataRepository(accountId = accountId, embeddedWallet)
        val poller = CoroutinePoller(context, repository, Dispatchers.IO)
        val currentJob = launch {
            val flow = poller.pollTransactions(POLLING_FREQUENCY)
            flow.cancellable().collect { transactionResponses ->
                coroutineContext.ensureActive()
                handleTransactions(context, deviceId, transactionResponses?.data)
            }
        }
        jobs[deviceId] = currentJob
        pollers[deviceId] = poller
    }

    fun fetchTransactions(context: Context, deviceId: String, accountId: Int, embeddedWallet: EmbeddedWallet) {
        Timber.i("$deviceId - getAllTransactions")
        val repository = DataRepository(accountId = accountId, embeddedWallet)
        val poller = CoroutinePoller(context, repository, Dispatchers.IO)

        launch {
            withContext(coroutineContext) {
                try {
                    // Get transactions from repository directly
                    val transactions = poller.getAllTransactions(coroutineContext)

                    // If we got transactions, handle them
                    if (transactions != null) {
                        handleTransactions(context, deviceId, transactions.data)
                    } else {
                        Timber.w("$deviceId - No transactions retrieved")
                    }

                    Timber.i("$deviceId - fetchTransactionsOnce completed")
                } catch (e: Exception) {
                    Timber.e(e, "$deviceId - Error fetching transactions once")
                }
            }
        }
    }

    private fun handleTransactions(context: Context, deviceId: String, transactionResponses: List<TransactionResponse>?) {
        if (isDebugLog()) {
            Timber.d("$deviceId - Received ${transactionResponses?.count()} transactionResponses")
        }
        val fireblocksManager = FireblocksManager.getInstance()
        transactionResponses?.forEach { transactionResponse ->
            val transactionWrapper = TransactionWrapper(deviceId, transactionResponse)
            fireblocksManager.fireTransaction(context, transactionWrapper)
        }
    }

    fun stopPollingTransactions(deviceId: String) {
        jobs[deviceId]?.cancel()
        pollers[deviceId]?.close()
        job = Job()
        Timber.w("$deviceId - stopPolling")
    }

}