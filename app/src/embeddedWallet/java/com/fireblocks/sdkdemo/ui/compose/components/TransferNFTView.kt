package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.background

@Composable
fun TransferNFTView(nftWrapper: NFTWrapper) {
    NFTCardDetails(
        iconUrl = nftWrapper.iconUrl,
        blockchain = nftWrapper.blockchain,
        blockchainSymbol = nftWrapper.blockchainSymbol,
        nftName = nftWrapper.name ?: "",
        balance = "",
        standard = nftWrapper.standard ?: "",
    )
}

@Preview
@Composable
fun TransferNFTViewPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            TransferNFTView(
                nftWrapper = NFTWrapper(
                    id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91",
                    name = "nft name",
                    collectionName = "collection name",
                    iconUrl = "",
                    blockchain = "ETH",
                    standard = "ERC721",
                )
            )
        }
    }
}