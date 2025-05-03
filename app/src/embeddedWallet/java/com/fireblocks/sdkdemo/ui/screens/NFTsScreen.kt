package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.fireblocks.sdkdemo.bl.core.extensions.getBlockchainDisplayName
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BalanceView
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIcon
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.NFTCard
import com.fireblocks.sdkdemo.ui.compose.components.NFTIcon
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NFTsScreen(modifier: Modifier = Modifier,
               viewModel: NFTsViewModel = viewModel(),
               onNextScreen: (nft: TokenOwnershipResponse) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val isListView: MutableState<Boolean> = remember { mutableStateOf(true) }
    val isDescending: MutableState<Boolean> = remember { mutableStateOf(true) }

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
        Column {
            Row(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small),
                vertical = dimensionResource(id = R.dimen.padding_default)),) {
                val viewTypeText = when(isListView.value) {
                    true -> stringResource(id = R.string.list_view)
                    false -> stringResource(id = R.string.gallery_view)
                }
                val annotatedString = buildAnnotatedString {
                    append(stringResource(id = R.string.view_as))
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(" $viewTypeText")
                    }
                }
                FireblocksText(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                isListView.value = !isListView.value
                            }
                        ) ,
                    annotatedString = annotatedString,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Start,
                    textColor = text_secondary
                )

                Spacer(modifier = Modifier.weight(1f))
                val sortTypeArrowText = when (isDescending.value) {
                    true -> " ↓ "
                    false -> " ↑ "
                }

                val sortAnnotatedString = buildAnnotatedString {
                    append(stringResource(id = R.string.sort_by))
                    append(" ")
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(stringResource(id = R.string.date))
                        append(sortTypeArrowText)
                    }
                }
                FireblocksText(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                isDescending.value = !isDescending.value
                            }
                        ) ,
                    annotatedString = sortAnnotatedString,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Start,
                    textColor = text_secondary
                )
            }

            val nfts = uiState.nfts
            val sortedItems = if (isDescending.value) {
                nfts.sortedByDescending { it.ownershipStartTime }
            } else {
                nfts.sortedBy { it.ownershipStartTime }
            }

            if (isListView.value) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sortedItems, key = { nft -> nft.id!!}) { nft ->
                        NFTListItem(
                            nft = nft,
                            onClick = {
                                onNextScreen(it)
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sortedItems, key = { nft -> nft.id!!}) { nft ->
                        NFTGalleryItem(
                            nft = nft,
                            onClick = {
                                onNextScreen(it)
                            }
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing,
            pullRefreshState,
            modifier.align(Alignment.TopCenter),
            contentColor = white,
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
    onClick: (TokenOwnershipResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val nftName = nft.name ?: "" // sword
    val blockchain = nft.getBlockchainDisplayName(context)
    val blockchainSymbol = nft.blockchainDescriptor?.name
    val standard = nft.standard ?: "" // ERC1155
    val balance = getBalance(nft)
    val iconUrl = nft.media?.firstOrNull()?.url

    NFTCard(
        iconUrl = iconUrl,
        blockchain = blockchain,
        blockchainSymbol = blockchainSymbol,
        nftName = nftName,
        balance = balance ?: "",
        standard = standard,
        onClick = { onClick.invoke(nft) }
    )
}

private fun getBalance(nft: TokenOwnershipResponse): String? {
    return nft.balance?.toIntOrNull()?.let { if (it < 100) it.toString() else "99+" }
}

@Composable
fun NFTGalleryItem(
    nft: TokenOwnershipResponse,
    clickable: Boolean = true,
    onClick: (TokenOwnershipResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val nftName = nft.name ?: ""
    val blockchain = nft.getBlockchainDisplayName(context)
    val blockchainSymbol = nft.blockchainDescriptor?.name ?: ""
    val standard = nft.standard ?: ""
    val balance = getBalance(nft)

    val cardBackgroundColor: MutableState<Color> = remember { mutableStateOf(grey_1) }

    Column(
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                color = grey_1
            )
            .clickable(enabled = clickable) { onClick.invoke(nft) },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.nft_image_height_gallery)),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor.value),
            shape = RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.round_corners_default),
                topEnd = dimensionResource(id = R.dimen.round_corners_default))
            ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                NFTIcon(
                    modifier = Modifier,
                    context = context,
                    iconUrl = nft.media?.firstOrNull()?.url,
                    onDominantColorExtracted = { color ->
                        cardBackgroundColor.value = color
                    }
                )
            }
        }
        Column(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small_2), end = dimensionResource(id = R.dimen.padding_small_2), top = dimensionResource(id = R.dimen.padding_default)),
            horizontalAlignment = Alignment.Start) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                ) {
                FireblocksText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(R.dimen.padding_extra_small)),
                    text = nftName,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    maxLines = 1
                )
                BalanceView(balance)
            }

            val annotatedString = buildAnnotatedString {
                append(blockchain)
                append(" ")
                appendInlineContent(id = "imageId")
                append(" ")
                append(standard)
            }
            val inlineContentMap = mapOf(
                "imageId" to InlineTextContent(
                    Placeholder(4.sp, 4.sp, PlaceholderVerticalAlign.TextCenter)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_dot),
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = ""
                    )
                }
            )
            Row(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(id = R.dimen.padding_small_2)),
                verticalAlignment = Alignment.CenterVertically) {
                CryptoIcon(symbol = blockchainSymbol, imageSizeResId = R.dimen.blockchain_image_size_nft_galley, paddingResId = R.dimen.padding_none)
                FireblocksText(
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_extra_small_1)),
                    annotatedString = annotatedString,
                    inlineContent = inlineContentMap,
                    textStyle = FireblocksNCWDemoTheme.typography.b4,
                    textColor = text_secondary

                )
            }
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
            balance = "100",
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
        Surface(color = background) {
            NFTsScreen(viewModel = viewModel)
        }
    }
}