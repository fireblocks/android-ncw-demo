package com.fireblocks.sdkdemo.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.getBlockchainDisplayName
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.DetailsListItem
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.NFTIcon
import com.fireblocks.sdkdemo.ui.compose.components.SymbolListItem
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 16/02/2025.
 */
@Composable
fun NFTDetailsScreen(
    nft: TokenOwnershipResponse? = null,
    onTransferNFTClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    nft?.let {
        val nftName = nft.name ?: "" // sword
        val id = nft.id ?: "" // NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91
        val tokenId = nft.tokenId?.let { "#$it" } ?: ""
        val blockchain = nft.getBlockchainDisplayName(context)
        val blockchainSymbol = nft.blockchainDescriptor?.name ?: ""
        val standard = nft.standard ?: "" // ERC1155
        val balance = nft.balance ?: ""
        val contactAddress = nft.collection?.id ?: ""
        val collection = nft.collection?.name ?: "" // Weapons
        val purchaseDate = nft.ownershipStartTime?.let { TimeUnit.SECONDS.toMillis(it) }?.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", timeFormat = "hh:mm", useSpecificDays = false)
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default))) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = dimensionResource(R.dimen.padding_extra_large_3))
                        .background(
                            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                            color = grey_1
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn {
                        val itemsList = mutableListOf<@Composable () -> Unit>()
                        itemsList.add {
                            NFTDetailsHeader(context, nft, nftName)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.amount, contentText = balance)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.purchase_date, contentText = purchaseDate)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.collection, contentText = collection)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.token_id, contentText = tokenId)
                        }
                        itemsList.add {
                            SymbolListItem(titleResId = R.string.blockchain, blockchain = blockchain, blockchainSymbol = blockchainSymbol)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.standard, contentText = standard)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.contact_address, contentText = contactAddress, showCopyButton = true)
                        }
                        itemsList.add {
                            DetailsListItem(titleResId = R.string.fireblocks_nft_id, contentText = id, showCopyButton = true)
                        }

                        itemsIndexed(itemsList) { index, item ->
                            item()
                            if (index < itemsList.size - 1) {
                                HorizontalDivider(color = grey_2, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                TransferNFTButton(onTransferNFTClicked)
            }
        }
    }
}

@Composable
private fun ColumnScope.NFTDetailsHeader(
    context: Context,
    nft: TokenOwnershipResponse,
    nftName: String
) {
    var cardBackgroundColor by remember { mutableStateOf(grey_2) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.nft_image_height_details))
            .align(Alignment.CenterHorizontally),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.round_corners_default),
            topEnd = dimensionResource(id = R.dimen.round_corners_default)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            NFTIcon(
                modifier = Modifier.fillMaxHeight(),
                context = context,
                iconUrl = nft.media?.firstOrNull()?.url,
                onDominantColorExtracted = { color ->
                    cardBackgroundColor = color
                }
            )
        }
    }

    FireblocksText(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = background)
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_large),
                vertical = dimensionResource(id = R.dimen.padding_large)
            ),
        text = nftName,
        textStyle = FireblocksNCWDemoTheme.typography.h3,
    )
}

@Composable
private fun TransferNFTButton(onTransferNFTClicked: () -> Unit) {
    val continueEnabledState = remember { mutableStateOf(true) }
    ContinueButton(
        enabledState = continueEnabledState,
        onClick = onTransferNFTClicked,
        labelResourceId = R.string.transfer_nft
    )
}

@Preview
@Composable
fun NFTScreenPreview() {
    MultiDeviceManager.initialize(LocalContext.current)
    val nft = TokenOwnershipResponse(
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
        ownershipStartTime = 1738855593,
        ownershipLastUpdateTime = 1738855593,
        status = TokensStatus.LISTED,
        vaultAccountId = "1",
        ncwId = "1",
        ncwAccountId = "1",
    )
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            NFTDetailsScreen(nft)
        }
    }
}