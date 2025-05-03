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

    // TODO  - use this in Fee and anywhere else
    fun removeTestSuffix(title: String): String {
        var title1 = title
        if (title1.contains("_TEST")) {
            title1 = title1.replace(Regex("_TEST\\d*$"), "")
        }
        return title1
    }

}