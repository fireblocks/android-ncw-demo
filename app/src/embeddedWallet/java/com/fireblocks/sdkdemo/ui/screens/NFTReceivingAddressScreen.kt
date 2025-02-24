package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ReceivingAddressView
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun NFTReceivingAddressScreen(
    viewModel: NFTsViewModel,
    onNextScreen: (address: String) -> Unit = {},
) {
    val selectedNFT = viewModel.getSelectedNFT()
    selectedNFT?.let { nft ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
        ) {
            NFTListItem(nft = nft)

            ReceivingAddressView(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                onContinueClicked = { address ->
                    onContinueClick(onNextScreen, address)
                }
            )
        }
    }
}


private fun onContinueClick(
    onNextScreen: (address: String) -> Unit,
    address: String
) {
    onNextScreen(address)
}

@Preview
@Composable
fun NFTReceivingAddressScreenPreview() {
    val viewModel = NFTsViewModel()
    viewModel.onNFTSelected(
        TokenOwnershipResponse(
            id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91",
            tokenId = "1",
            standard = "ERC1155",
            blockchainDescriptor = BlockchainDescriptor.XTZ_TEST,
            description = "This is the NFT description",
            name = "sword",
            metadataURI = "ipfs://bafybeidstfcbqv2v2ursvchraw64hu2p4e3rx37dpoqzeguvketcfetlji/1",
            cachedMetadataURI = "https://stage-static.fireblocks.io/dev9/nft/24d1bea228cbcb5010db9a91376c65ea/metadata.json",
            media = listOf(
                MediaEntityResponse(
                    url = "https://stage-static.fireblocks.io/dev9/nft/media/aXBmczovL2JhZnliZWloamNuYXFrd3lucG9kaW5taW54dXdiZ3VucWNxYnNlMmxwb2kzazJibnIyempneXhyaHV1LzE",
                    contentType = MediaEntityResponse.ContentType.IMAGE
                )
            ),
            collection = TokenCollectionResponse(
                id = "0xe0e2C83BdE2893f93012b9FE7cc0bfC2893b344B",
                name = "my collection",
                symbol = "A"
            ),
            spam = SpamOwnershipResponse(
                true,
                source = SpamOwnershipResponse.SpamOwnershipResponseSource.SYSTEM
            ),
            balance = "1",
            ownershipStartTime = 123,
            ownershipLastUpdateTime = 123,
            status = TokensStatus.LISTED,
            vaultAccountId = "1",
            ncwId = "1",
            ncwAccountId = "1",
        )
    )

    FireblocksNCWDemoTheme {
        Surface(color = background) {
            NFTReceivingAddressScreen(viewModel = viewModel)
        }
    }
}

