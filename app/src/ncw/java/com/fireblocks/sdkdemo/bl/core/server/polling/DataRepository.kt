package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.bl.core.server.Api
import com.fireblocks.sdkdemo.bl.core.server.CreateTransactionRequestBody
import com.fireblocks.sdkdemo.bl.core.server.HeaderInterceptor.Companion.getHeaders
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
class DataRepository(val context: Context, val deviceId: String) {

    fun getTransactions(startTimeInMillis: Long, statusList: List<String> = arrayListOf()): Response<ArrayList<TransactionResponse>>? {
        if (isDebugLog()) {
            Timber.d("calling getTransactions API startTimeInMillis: $startTimeInMillis, statusList: $statusList")
        }
        return runCatching {
            val response = Api.with(StorageManager.get(context, deviceId)).getTransactions(deviceId, startTimeInMillis, statusList, headers = getHeaders(context, deviceId)).execute()
            if (isDebugLog()) {
                Timber.d("got response from getTransactions rest API code:${response.code()}, isSuccessful:${response.isSuccessful}", response)
            }
            response
        }.onFailure {
            Timber.w(it, "Failed to call getTransactions API")
        }.getOrNull()
    }

    fun cancelTransaction(txId: String): Boolean {
        var success = false
        runCatching {
            val response = Api.with(StorageManager.get(context, deviceId)).cancelTransaction(deviceId, txId, getHeaders(context, deviceId)).execute()
            Timber.d("got response from cancelTransaction rest API code:${response.code()}, isSuccessful:${response.isSuccessful} response.body(): ${response.body()}", response)
            success = response.isSuccessful
        }.onFailure {
            Timber.w(it, "Failed to call cancelTransaction API")
        }
        return success
    }

    fun createTransaction(assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel): CreateTransactionResponse? {
        return runCatching {
            val response = Api.with(StorageManager.get(context, deviceId)).createTransaction(
                deviceId = deviceId,
                CreateTransactionRequestBody(
                    assetId = assetId,
                    destAddress = destAddress,
                    amount = amount,
                    feeLevel = feeLevel),
                getHeaders(context, deviceId)
            ).execute()
            Timber.d("got response from createTransaction rest API code:${response.code()}, isSuccessful:${response.isSuccessful} response.body(): ${response.body()}",
                response)
            response.body()
        }.onFailure {
            Timber.w(it, "Failed to call createTransaction API")
        }.getOrNull()
    }
}