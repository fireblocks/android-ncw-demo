package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.fireblocks.sdk.bl.core.storage.models.KeyId
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.google.gson.annotations.SerializedName
import java.util.*

data class SupportedAsset(@KeyId @SerializedName("id") var id: String = "", // BTC_TEST
                          @SerializedName("symbol") var symbol: String = "", // BTC_TEST
                          @SerializedName("name") val name: String = "", // Bitcoin Test
                          @SerializedName("decimals") private val decimals: Int = 0, // 8
                          @SerializedName("testnet") val testnet: Boolean = false,
                          @SerializedName("hasFee") val hasFee: Boolean = false,
                          @SerializedName("type")  val type: String, // BASE_ASSET
                          @SerializedName("issuerAddress")  val issuerAddress: String = "", //
                          @SerializedName("blockchainSymbol") val blockchainSymbol: String = "",
                          @SerializedName("coinType") val coinType: Int? = null,
                          @SerializedName("blockchain") val blockchain: String = "",
                          @SerializedName("ethContractAddress") val echContractAddress: String = "", //
                          @SerializedName("ethNetwork") val ethNetwork: Long? = null, //
                          @SerializedName("networkProtocol") private val networkProtocol: String = "", // BTC
                          @SerializedName("baseAsset")  val baseAsset: String = "", // BASE_ASSET
                          @SerializedName("rate") var rate: Double = 1.0, // rate of each asset e.g. 29000 for BTC
                          @SerializedName("fee") var fee: Fee? = null,

                          var balance: String = "", // from the asset balance Api, the number of coins e.g. 1 BTC
                          var price: String = "", // balance * rate
                          var address: String = "",
                          ) {

    fun getIcon(context: Context): Int {
        return when (this.id) {
            "BTC", "BTC_TEST" -> R.drawable.ic_btc
            "ADA", "ADA_TEST" -> R.drawable.ic_ada
            "AVAX", "AVAX_TEST" -> R.drawable.ic_avax
            "MATIC", "MATIC_TEST" -> R.drawable.ic_matic
            "USDT", "USDT_TEST" -> R.drawable.ic_usdt
            "USDC", "USDC_TEST" -> R.drawable.ic_usdc
            "DAI", "DAI_TEST" -> R.drawable.ic_dai
            "SHIB", "SHIB_TEST" -> R.drawable.ic_shib
            "UNI", "UNI_TEST" -> R.drawable.ic_uni
            "ETH", "ETH_TEST", "ETH_TEST3" -> R.drawable.ic_eth
            "WETH", "WETH_TEST" -> R.drawable.ic_eth
            "XRP", "XRP_TEST" -> R.drawable.ic_xrp
            "DOT", "DOT_TEST" -> R.drawable.ic_dot
            "SOL", "SOL_TEST" -> R.drawable.ic_sol
            else -> run {
                val resources: Resources = context.resources
                val resourceId: Int = resources.getIdentifier(name, "ic_${this.id.lowercase(Locale.ENGLISH)}", context.packageName)
                if (resourceId > 0) {
                    return resourceId
                }
                return R.drawable.ic_default_asset
            }
        }
    }

    fun getSymbol(context: Context): String {
        return when {
            type == "FIAT" && id in arrayOf("SIGNET", "SIGNET_TEST", "SILVERGATE", "SILVERGATE_TEST") -> context.getString(
                    R.string.USD)
            symbol.isNotNullAndNotEmpty() -> symbol.toString()
            else -> id
        }
    }

    companion object {

        fun getAssetFor(assetId: String): SupportedAsset {
            return SupportedAsset(id = assetId, name = assetId, symbol = assetId, type = "unknown")
        }

        fun getTagType(context: Context, asset: String): String {
            return context.getString(getTagTypeId(asset))
        }

        @StringRes
        fun getTagTypeId(asset: String): Int {
            if (asset.toUpperCase() in arrayOf("EOS", "EOS_TEST", "EOS_T", "XLM", "XLM_TEST", "HBAR", "HBAR_TEST")) {
                return R.string.address_memo
            }
            return R.string.tag
        }

        fun getTagText(context: Context, asset: String, tag: String?): String? {
            if (tag != null && tag.isNotEmpty()) {
                return context.getString(getTagTypeId(asset), tag)
            }
            return null
        }

        fun getTagTextWithTypeId(context: Context, asset: String, tag: String?): String? {
            if (tag != null && tag.isNotEmpty()) {
                return context.getString(R.string.address_tag, context.getString(getTagTypeId(asset)), tag)
            }
            return null
        }

        fun isAbsoluteFeeAsset(feeAsset: String?): Boolean {
            return feeAsset?.startsWith("ETH") == false
        }
    }
}

