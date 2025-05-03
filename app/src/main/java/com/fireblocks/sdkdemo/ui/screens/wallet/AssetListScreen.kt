@file:OptIn(ExperimentalMaterialApi::class)

package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.AddButton
import com.fireblocks.sdkdemo.ui.compose.components.ExpandableAssetListItem
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 11/07/2023.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssetListScreen(
    modifier: Modifier = Modifier,
    uiState: WalletUiState,
    viewModel: WalletViewModel,
    onSendClicked: (SupportedAsset) -> Unit = {},
    onReceiveClicked: (SupportedAsset) -> Unit = {},
    onAddAssetClicked: () -> Unit = {},
) {
    val context = LocalContext.current
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    val refreshing = userFlow is UiState.Refreshing
    fun refresh() = viewModel.loadAssets(context, state = UiState.Refreshing)
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    val hasAssetsState = remember { mutableStateOf(true) }
    val assets = uiState.assets
    val hasAssets = assets.isNotEmpty()
    hasAssetsState.value = hasAssets
    Timber.d("hasAssets: $hasAssets, hasAssetsState: ${hasAssetsState.value}")

    val listState = rememberLazyListState()

    Box(
        modifier = mainModifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_default)),) {
            item {
                Header(Modifier, uiState, onAddAssetClicked, hasAssets)
            }
            assets.forEach {
                item(key = it.id) {
                    ExpandableAssetListItem(
                        supportedAsset = it,
                        onSendClicked = { supportedAsset ->
                            onSendClicked(supportedAsset)
                        },
                        onReceiveClicked = { supportedAsset ->
                            onReceiveClicked(supportedAsset)
                        },
                        listState = listState
                    )
                }
            }
            if (!hasAssetsState.value) {
                item {
                    AddAssetListItem(onAddAssetClicked)
                }
            }
        }

        PullRefreshIndicator(
            refreshing,
            pullRefreshState,
            modifier.align(Alignment.TopCenter),
            contentColor = white,
            backgroundColor = transparent)

        if (showProgress) {
            ProgressBar()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event){
            Lifecycle.Event.ON_CREATE -> {
                val noAssets = (uiState.assets.isEmpty())
                val state = if (noAssets) UiState.Loading else UiState.Idle
                viewModel.loadAssets(context, state)
            }
            else -> {}
        }
    }
}
@Composable
fun Header(modifier: Modifier,
           uiState: WalletUiState,
           onShowSupportedAssetsClicked: () -> Unit = {},
           hasAssets: Boolean = true,
) {
    Column(modifier = modifier
    ) {
        val balanceContentDesc = stringResource(id = R.string.balance_value_desc)
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_large_1)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FireblocksText(
                text = stringResource(id = R.string.balance),
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textColor = text_secondary
            )
            FireblocksText(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_large),
                        start = dimensionResource(id = R.dimen.padding_small),
                        end = dimensionResource(id = R.dimen.padding_small))
                    .semantics { contentDescription = balanceContentDesc },
                text = stringResource(id = R.string.usd_balance, uiState.balance),
                textStyle = FireblocksNCWDemoTheme.typography.h1
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_extra_large),
                start = dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default)))
        {
            FireblocksText(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.assets),
                textStyle = FireblocksNCWDemoTheme.typography.b1
            )
            if (hasAssets) {
                AddButton(onShowSupportedAssetsClicked)
            }
        }
    }
}

@Composable
fun AddAssetListItem(onAddAssetClicked: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = transparent)
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small), horizontal = dimensionResource(id = R.dimen.padding_small))
            .clickable { onAddAssetClicked.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = grey_1),
        ) {
            val imageSize = dimensionResource(id = R.dimen.image_size)
            Image(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small_1))
                    .height(imageSize)
                    .width(imageSize),
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = stringResource(id = R.string.add_asset)
            )
        }
        FireblocksText(
            text = stringResource(id = R.string.add_asset),
            textStyle = FireblocksNCWDemoTheme.typography.b1,
        )
    }
}

@Preview
@Composable
fun AddAssetListItemPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            AddAssetListItem()
        }
    }
}

@Preview
@Composable
fun AssetsScreenPreview() {
    val viewModel = WalletViewModel()
    val assets = arrayListOf<SupportedAsset>()
    assets.add(SupportedAsset(
        id = "BTC_TEST",
        symbol = "BTC_TEST",
        name = "Bitcoin Test",
        type = "BASE_ASSET",
        blockchain = "BTC_TEST",
        balance = "0.00001",
        price = "0.29"
    ))
    assets.add(SupportedAsset(
        id = "ETH_TEST3",
        symbol = "ETH_TEST3",
        name = "Ethereum Test (Goerli)",
        decimals = 18,
        type = "BASE_ASSET",
        blockchain = "ETH_TEST3",
        balance = "132.4",
        price = "2,825.04"))
    assets.add(SupportedAsset(
        id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum",
        balance = "132.4",
        price = "2,825.04"))
    assets.add(SupportedAsset(id = "SOL",
        symbol = "SOL",
        name = "Solana",
        type = "BASE_ASSET",
        blockchain = "Solana",
        balance = "217",
        price = "1,336.72"))
    assets.add(SupportedAsset(id = "XRP_TEST",
        symbol = "XRP_TEST",
        name = "Xrp Test",
        type = "BASE_ASSET",
        blockchain = "XRP_TEST",
        balance = "2.48",
        price = "41,044.93"))
    viewModel.onAssets(assets)

    viewModel.onBalance("45,873.03")

    FireblocksNCWDemoTheme {
        Surface(color = background) {
            AssetListScreen(
                uiState = WalletUiState(assets = assets),
                viewModel = viewModel,
                onSendClicked = {},
            )
        }
    }
}