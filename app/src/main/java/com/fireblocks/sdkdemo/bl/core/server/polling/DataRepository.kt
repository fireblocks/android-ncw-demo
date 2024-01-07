package com.fireblocks.sdkdemo.bl.core.server.polling

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.bl.core.server.Api
import com.fireblocks.sdkdemo.bl.core.server.CreateTransactionRequestBody
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.models.MessageResponse
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 09/03/2023.
 */
class DataRepository(val context: Context, val deviceId: String) {
    fun getMessages(): ArrayList<MessageResponse>? {
        Timber.d("calling getMessages API")
        return runCatching {
            val physicalDeviceId = Fireblocks.getPhysicalDeviceId()
            val response = Api.with(StorageManager.get(context, deviceId)).getMessages(deviceId, physicalDeviceId).execute()
            Timber.d("got response from getMessages rest API code:${response.code()}, isSuccessful:${response.isSuccessful}", response)
            response.body()
        }.onFailure {
            Timber.w(it, "Failed to call getMessages API")
        }.getOrNull()
    }

    fun deleteMessage(id: String?) {
        Timber.i("calling delete message API")
        runCatching {
            if (id.isNullOrEmpty()) {
                Timber.w("Failed to call deleteMessage API, missing message id")
            } else {
                val response = Api.with(StorageManager.get(context, deviceId)).deleteMessage(deviceId, id).execute()
                Timber.i("got response from deleteMessage rest API code:${response.code()}, isSuccessful:${response.isSuccessful}", response)
            }
        }.onFailure {
            Timber.w(it, "Failed to call getMessages API")
        }
    }

    fun getTransactions(startTimeInMillis: Long, statusList: List<String> = arrayListOf()): Response<ArrayList<TransactionResponse>>? {
        Timber.d("calling getTransactions API startTimeInMillis: $startTimeInMillis, statusList: $statusList")
        return runCatching {
            // = Instant.now().toEpochMilli()
            val response = Api.with(StorageManager.get(context, deviceId)).getTransactions(deviceId, startTimeInMillis, statusList).execute()
            Timber.d("got response from getTransactions rest API code:${response.code()}, isSuccessful:${response.isSuccessful}", response)
            response
        }.onFailure {
            Timber.w(it, "Failed to call getTransactions API")
        }.getOrNull()
    }

    fun cancelTransaction(txId: String): Boolean {
        var success = false
        runCatching {
            val response = Api.with(StorageManager.get(context, deviceId)).cancelTransaction(deviceId, txId).execute()
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
                    feeLevel = feeLevel)
            ).execute()
            Timber.d("got response from createTransaction rest API code:${response.code()}, isSuccessful:${response.isSuccessful} response.body(): ${response.body()}",
                response)
            response.body()
        }.onFailure {
            Timber.w(it, "Failed to call createTransaction API")
        }.getOrNull()
    }
}