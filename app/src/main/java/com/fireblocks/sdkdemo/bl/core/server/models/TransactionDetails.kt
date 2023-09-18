package com.fireblocks.sdkdemo.bl.core.server.models

import com.fireblocks.sdkdemo.bl.core.storage.models.ExtraParameters
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Fireblocks ltd. on 19/04/2023.
 */
data class TransactionDetails(
    @SerializedName("id") val id: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("status") val status: SigningStatus? = null,
    @SerializedName("amountInfo") val amountInfo: AmountInfo? = AmountInfo(),
    @SerializedName("txHash") val txHash: String? = null,
    @SerializedName("assetId") var assetId: String? = null,
    @SerializedName("assetType") var assetType: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("operation") val operation: String? = null,
    @SerializedName("destinationAddress") val destinationAddress: String? = null,
    @SerializedName("sourceAddress") val sourceAddress: String? = null,
    @SerializedName("feeCurrency") val feeCurrency: String? = null,
    @SerializedName("networkFee") val networkFee: String? = null,
    @SerializedName("extraParameters") val extraParameters: ExtraParameters? = null,
    @SerializedName("source") val source: Source? = null,
    ): Serializable
