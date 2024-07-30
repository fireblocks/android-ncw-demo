package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import android.content.res.Resources
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdkdemo.R
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
data class SupportedAsset(@KeyId @SerializedName("id") var id: String = "", // BTC_TEST
                          @SerializedName("symbol") var symbol: String = "", // BTC_TEST
                          @SerializedName("name") val name: String = "", // Bitcoin Test
                          @SerializedName("decimals") private val decimals: Int = 0, // 8
                          @SerializedName("testnet") val testnet: Boolean = false,
                          @SerializedName("hasFee") val hasFee: Boolean = false,
                          @SerializedName("type")  val type: String, // BASE_ASSET
                          @SerializedName("deprecated")  val deprecated: Boolean = false, // false
                          @SerializedName("issuerAddress")  val issuerAddress: String? = "", //
                          @SerializedName("blockchainSymbol") val blockchainSymbol: String? = "",
                          @SerializedName("coinType") val coinType: Int? = null,
                          @SerializedName("blockchain") val blockchain: String = "",
                          @SerializedName("ethContractAddress") val echContractAddress: String? = "", //
                          @SerializedName("ethNetwork") val ethNetwork: Long? = null, //
                          @SerializedName("networkProtocol") private val networkProtocol: String = "", // BTC
                          @SerializedName("baseAsset")  val baseAsset: String = "", // BASE_ASSET
                          @SerializedName("rate") var rate: Double = 1.0, // rate of each asset e.g. 29000 for BTC
                          @SerializedName("fee") var fee: Fee? = null,
                          @SerializedName("iconUrl") var iconUrl: String? = null,
                          @SerializedName("assetAddress") var assetAddress: AssetAddress? = null,
                          @SerializedName("algorithm") var algorithm: Algorithm? = null,

                          var balance: String = "", // from the asset balance Api, the number of coins e.g. 1 BTC
                          var price: String = "", // balance * rate
                          var derivedAssetKey: KeyData? = null,
                          var wif: String? = null) {

    fun getIcon(context: Context): Int {
        return when (this.id) {
            "BTC", "BTC_TEST" -> R.drawable.ic_btc
            "ADA", "ADA_TEST" -> R.drawable.ic_ada
            "AVAX", "AVAX_TEST", "AVAXTEST" -> R.drawable.ic_avax
            "MATIC", "MATIC_TEST", "MATIC_POLYGON_MUMBAI" -> R.drawable.ic_matic
            "USDT", "USDT_TEST" -> R.drawable.ic_usdt
            "USDC", "USDC_TEST" -> R.drawable.ic_usdc
            "DAI", "DAI_TEST" -> R.drawable.ic_dai
            "SHIB", "SHIB_TEST" -> R.drawable.ic_shib
            "UNI", "UNI_TEST", "UNI_ETH_TEST3_EB3S" -> R.drawable.ic_uni
            "ETH", "ETH_TEST", "ETH_TEST3", "ETH_TEST5", "WETH", "WETH_TEST" -> R.drawable.ic_eth
            "XRP", "XRP_TEST" -> R.drawable.ic_xrp
            "DOT", "DOT_TEST" -> R.drawable.ic_dot
            "SOL", "SOL_TEST", "AMAZING_SOL_TOKEN" -> R.drawable.ic_sol
            "CELO_ALF", "CELO_BAK" -> R.drawable.ic_celo
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

    fun isBackgroundTransparent(): Boolean {
        return this.id.startsWith("ALGO")
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + decimals
        result = 31 * result + testnet.hashCode()
        result = 31 * result + hasFee.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + deprecated.hashCode()
        result = 31 * result + (issuerAddress?.hashCode() ?: 0)
        result = 31 * result + (blockchainSymbol?.hashCode() ?: 0)
        result = 31 * result + (coinType?.hashCode() ?: 0)
        result = 31 * result + blockchain.hashCode()
        result = 31 * result + (echContractAddress?.hashCode() ?: 0)
        result = 31 * result + (ethNetwork?.hashCode() ?: 0)
        result = 31 * result + networkProtocol.hashCode()
        result = 31 * result + baseAsset.hashCode()
        result = 31 * result + rate.hashCode()
        result = 31 * result + (fee?.hashCode() ?: 0)
        result = 31 * result + (iconUrl?.hashCode() ?: 0)
        result = 31 * result + (assetAddress?.hashCode() ?: 0)
        result = 31 * result + (algorithm?.hashCode() ?: 0)
        result = 31 * result + balance.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + (derivedAssetKey?.hashCode() ?: 0)
        result = 31 * result + (wif?.hashCode() ?: 0)
        return result
    }
}

