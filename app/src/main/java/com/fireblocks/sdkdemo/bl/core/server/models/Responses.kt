package com.fireblocks.sdkdemo.bl.core.server.models

import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.google.gson.annotations.SerializedName


/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
data class MessageResponse(@SerializedName("id") val id: String? = null,
                           @SerializedName("message") val message: String? = null)

data class TransactionResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: SigningStatus? = null,
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("lastUpdated") val lastUpdated: Long? = null,
    @SerializedName("details") val details: TransactionDetails? = null,
) : java.io.Serializable

data class CreateTransactionResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: SigningStatus? = null)

data class GetDevicesResponse(@SerializedName("devices") val devices: ArrayList<FireblocksDevice>? = null,)
data class FireblocksDevice(@SerializedName("walletId") val walletId: String? = null,
                            @SerializedName("deviceId") val deviceId: String? = null,
                            @SerializedName("createdAt") val createdAt: Long? = null,)

data class AssignResponse(@SerializedName("walletId") val walletId: String? = null)

