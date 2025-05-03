package com.fireblocks.sdkdemo.bl.core.server.models

import com.fireblocks.sdkdemo.bl.core.storage.models.AmountInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.ExtraParameters
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Fireblocks Ltd. on 19/04/2023.
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

    @SerializedName("asset") var asset: SupportedAsset? = null,
) : Serializable {
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (amountInfo?.hashCode() ?: 0)
        result = 31 * result + (txHash?.hashCode() ?: 0)
        result = 31 * result + (assetId?.hashCode() ?: 0)
        result = 31 * result + (assetType?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (createdBy?.hashCode() ?: 0)
        result = 31 * result + (operation?.hashCode() ?: 0)
        result = 31 * result + (destinationAddress?.hashCode() ?: 0)
        result = 31 * result + (sourceAddress?.hashCode() ?: 0)
        result = 31 * result + (feeCurrency?.hashCode() ?: 0)
        result = 31 * result + (networkFee?.hashCode() ?: 0)
        result = 31 * result + (extraParameters?.hashCode() ?: 0)
        result = 31 * result + (source?.hashCode() ?: 0)
        result = 31 * result + (asset?.hashCode() ?: 0)
        return result
    }
}
