package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.NFTIcon
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentHorizontalView
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 16/02/2025.
 */
@Composable
fun NFTScreen(
    nft: TokenOwnershipResponse? = null,
    viewModel: NFTsViewModel = viewModel()
) {
    val context = LocalContext.current

    nft?.let {
        val nftName = nft.name ?: "" // sword
        val id = nft.id ?: "" // NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91
        val tokenId = nft.tokenId ?: ""
        val blockchain = nft.blockchainDescriptor?.name ?: "" //ETH_TEST5
        val standard = nft.standard ?: "" // ERC1155
        val contactAddress = nft.collection?.id ?: ""
        val collection = nft.collection?.name ?: "" // Weapons
        val purchaseDate = nft.ownershipStartTime?.let { TimeUnit.SECONDS.toMillis(it) }?.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", timeFormat = "hh:mm", useSpecificDays = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var cardBackgroundColor by remember { mutableStateOf(grey_1) }
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
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.padding_default)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NFTIcon(
                        modifier = Modifier,
                        context = context,
                        iconUrl = nft.media?.firstOrNull()?.url,
                        onDominantColorExtracted = { color ->
                            cardBackgroundColor = color
                        }
                    )
                }
            }
            Column(modifier = Modifier.fillMaxWidth()
                .padding(
                vertical = dimensionResource(id = R.dimen.padding_default),
                horizontal = dimensionResource(id = R.dimen.padding_large))
            ) {
                TitleContentHorizontalView(
                    titleText = nftName,
                    titleColor = white,
                    contentText = tokenId,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    topPadding = null,
                    contentDescriptionText = stringResource(id = R.string.nft_name_value_desc),
                )
                TitleContentHorizontalView(
                    titleText = stringResource(id = R.string.purchase_date),
                    titleColor = grey_4,
                    contentText = purchaseDate,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_date_value_desc),
                )
                TitleContentHorizontalView( //TODO add ... to teh context text
                    titleText = stringResource(id = R.string.collection),
                    titleColor = grey_4,
                    contentText = collection,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_collection_value_desc),
                )
                TitleContentHorizontalView(
                    titleText = stringResource(id = R.string.blockchain),
                    titleColor = grey_4,
                    contentText = blockchain,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_blockchain_value_desc),
                )
                TitleContentHorizontalView(
                    titleText = stringResource(id = R.string.standard),
                    titleColor = grey_4,
                    contentText = standard,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_standard_value_desc),
                )
                TitleContentView(
                    titleText = stringResource(id = R.string.contact_address),
                    titleColor = grey_4,
                    contentText = contactAddress,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    contentDrawableRes = R.drawable.ic_copy,
                    onContentButtonClick = { copyToClipboard(context, contactAddress) },
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_id_value_desc),
                )
                TitleContentView(
                    titleText = stringResource(id = R.string.id),
                    titleColor = grey_4,
                    contentText = id,
                    contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                    contentDrawableRes = R.drawable.ic_copy,
                    onContentButtonClick = { copyToClipboard(context, id) },
                    topPadding = R.dimen.padding_default,
                    contentDescriptionText = stringResource(id = R.string.nft_id_value_desc),
                )
            }
        }
    }
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
        Surface {
            NFTScreen(nft)
        }
    }
}