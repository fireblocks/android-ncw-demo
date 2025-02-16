package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.NFTIcon
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.primary_blue
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NFTsScreen(modifier: Modifier = Modifier,
               viewModel: NFTsViewModel = viewModel(),
               onNextScreen: (nft: TokenOwnershipResponse) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    var mainModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        mainModifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
            .alpha(floatResource(R.dimen.progress_alpha))
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    val refreshing = userFlow is UiState.Refreshing
    fun refresh() = viewModel.loadNFTs(state = UiState.Refreshing)
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(
        modifier = mainModifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize(),
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val nfts = uiState.nfts
            val sortedItems = nfts.sortedByDescending { it.ownershipLastUpdateTime }
            sortedItems.forEach {
                item {
                    NFTListItem(
                        nft = it,
                        onClick = {
                            onNextScreen(it)
                        }
                    )
                }
            }
        }

        PullRefreshIndicator(refreshing,
            pullRefreshState,
            modifier.align(Alignment.TopCenter),
            contentColor = primary_blue,
            backgroundColor = transparent
        )

        if (showProgress) {
            ProgressBar()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event){
            Lifecycle.Event.ON_CREATE -> {
                val noAssets = (uiState.nfts.isEmpty())
                val state = if (noAssets) UiState.Loading else UiState.Idle
                viewModel.loadNFTs(state)

            }
            else -> {}
        }
    }
}

@Composable
fun NFTListItem(
    nft: TokenOwnershipResponse,
    clickable: Boolean = true,
    onClick: (TokenOwnershipResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val nftName = nft.name ?: "" // sword
    val blockchain = nft.blockchainDescriptor?.name ?: "" //ETH_TEST5
    val standard = nft.standard ?: "" // ERC1155
    val tokenId = nft.tokenId ?: ""
    Row(
        modifier = Modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small), horizontal = dimensionResource(id = R.dimen.padding_default))
            .clickable(enabled = clickable) { onClick.invoke(nft) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = grey_1),
        ) {
            NFTIcon(context = context, iconUrl = nft.media?.firstOrNull()?.url)
        }
        Column(modifier = Modifier.weight(1f)) {
            FireblocksText(
                modifier = Modifier.padding(start = 2.dp),
                text = nftName,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
            )
            Label(
                modifier = Modifier.padding(top = 2.dp),
                text = blockchain
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            FireblocksText(
                text = tokenId,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textAlign = TextAlign.End
            )
            FireblocksText(
                text = standard,
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textColor = grey_4
            )
        }
    }
}

@Preview
@Composable
fun PreviewNFTsScreen() {
    val viewModel = NFTsViewModel()
    val nfts = listOf(
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
        ),
        TokenOwnershipResponse(
            id = "NFT-00000e5a0288cae2f191a9e3628790fb08e6a7ac91",
            tokenId = "2",
            standard = "ERC1155",
            blockchainDescriptor = BlockchainDescriptor.XTZ_TEST,
            description = "This is the NFT description",
            name = "sword#1",
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
    viewModel.onNFTs(nfts)
    FireblocksNCWDemoTheme {
        Surface {
            NFTsScreen(viewModel = viewModel)
        }
    }
}