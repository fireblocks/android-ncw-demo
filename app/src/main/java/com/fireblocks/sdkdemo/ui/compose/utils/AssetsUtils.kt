package com.fireblocks.sdkdemo.ui.compose.utils

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.blockchain.BlockchainProvider
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset

object AssetsUtils {

    /**
     * Returns the title text for the asset, including its symbol and blockchain display name.
     * If the symbol contains "_TEST", it is removed.
     * Symbol + (blockchain)
     * e.g. ETH (Ethereum Testnet Sepolia)
     */
    fun getAssetTitleText(context: Context, supportedAsset: SupportedAsset): String {
        var title = supportedAsset.symbol
        title = removeTestSuffix(title)

        val blockchain = BlockchainProvider.getBlockchain(context, supportedAsset.blockchain)
        if (blockchain?.displayName.isNotNullAndNotEmpty()) {
            title += " (${blockchain?.displayName})"
        }
        return title
    }

    fun removeTestSuffix(symbol: String): String {
        var title = symbol
        if (title.contains("_TEST")) {
            title = title.replace(Regex("_TEST\\d*$"), "")
        }
        return title
    }
}