package com.fireblocks.sdkdemo.bl.core.storage.models

import com.fireblocks.sdk.ew.models.Asset
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.KeyData

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
data class SupportedAsset(var id: String = "", // BTC_TEST
                          var symbol: String = "", // BTC_TEST
                          val name: String = "", // Bitcoin Test
                          val decimals: Int = 0, // 8
                          val testnet: Boolean = false,
                          val hasFee: Boolean = false,
                          val type: String, // BASE_ASSET
                          val deprecated: Boolean? = null, // false
                          val issuerAddress: String? = "", //
                          val blockchainSymbol: String? = "",
                          val coinType: Int? = null,
                          val blockchain: String = "",
                          val echContractAddress: String? = "", //
                          val ethNetwork: Long? = null, //
                          val networkProtocol: String = "", // BTC
                          val baseAsset: String = "", // BASE_ASSET
                          var algorithm: Algorithm? = null,

                          var rate: Double = 1.0, // rate of each asset e.g. 29000 for BTC
                          var iconUrl: String? = null,
                          var assetAddress: String? = null,
                          var accountId: String? = null,
                          var addressIndex: Int? = null,
                          var balance: String = "", // from the asset balance Api, the number of coins e.g. 1 BTC
                          var price: String = "", // balance * rate
                          var derivedAssetKey: KeyData? = null,
                          var wif: String? = null) {
    constructor(asset: Asset) : this(
        id = asset.id,
        symbol = asset.symbol,
        name = asset.name,
        decimals = asset.decimals,
        testnet = asset.testnet,
        hasFee = asset.hasFee,
        type = asset.type,
        deprecated = asset.deprecated,
        issuerAddress = asset.issuerAddress,
        blockchainSymbol = asset.blockchainSymbol,
        coinType = asset.coinType,
        blockchain = asset.blockchain,
        networkProtocol = asset.networkProtocol,
        baseAsset = asset.baseAsset,
        algorithm = asset.algorithm,
    )

    fun isBackgroundTransparent(): Boolean {
        return this.id.startsWith("ALGO")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SupportedAsset) return false

        if (id != other.id) return false
        if (symbol != other.symbol) return false
        if (name != other.name) return false
        if (decimals != other.decimals) return false
        if (testnet != other.testnet) return false
        if (hasFee != other.hasFee) return false
        if (type != other.type) return false
        if (deprecated != other.deprecated) return false
        if (issuerAddress != other.issuerAddress) return false
        if (blockchainSymbol != other.blockchainSymbol) return false
        if (coinType != other.coinType) return false
        if (blockchain != other.blockchain) return false
        if (echContractAddress != other.echContractAddress) return false
        if (ethNetwork != other.ethNetwork) return false
        if (networkProtocol != other.networkProtocol) return false
        if (baseAsset != other.baseAsset) return false
        if (algorithm != other.algorithm) return false
        if (rate != other.rate) return false
        if (iconUrl != other.iconUrl) return false
        if (assetAddress != other.assetAddress) return false
        if (accountId != other.accountId) return false
        if (addressIndex != other.addressIndex) return false
        if (balance != other.balance) return false
        if (price != other.price) return false
        if (derivedAssetKey != other.derivedAssetKey) return false
        if (wif != other.wif) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + decimals
        result = 31 * result + testnet.hashCode()
        result = 31 * result + hasFee.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (deprecated?.hashCode() ?: 0)
        result = 31 * result + (issuerAddress?.hashCode() ?: 0)
        result = 31 * result + (blockchainSymbol?.hashCode() ?: 0)
        result = 31 * result + (coinType ?: 0)
        result = 31 * result + blockchain.hashCode()
        result = 31 * result + (echContractAddress?.hashCode() ?: 0)
        result = 31 * result + (ethNetwork?.hashCode() ?: 0)
        result = 31 * result + networkProtocol.hashCode()
        result = 31 * result + baseAsset.hashCode()
        result = 31 * result + (algorithm?.hashCode() ?: 0)
        result = 31 * result + rate.hashCode()
        result = 31 * result + (iconUrl?.hashCode() ?: 0)
        result = 31 * result + (assetAddress?.hashCode() ?: 0)
        result = 31 * result + (accountId?.hashCode() ?: 0)
        result = 31 * result + (addressIndex ?: 0)
        result = 31 * result + balance.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + (derivedAssetKey?.hashCode() ?: 0)
        result = 31 * result + (wif?.hashCode() ?: 0)
        return result
    }
}

