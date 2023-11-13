@file:OptIn(ExperimentalMaterialApi::class)

package com.fireblocks.sdkdemo.ui.screens.wallet

import CryptoIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.primary_blue
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 11/07/2023.
 */
@Composable
fun AssetListScreen(
    uiState: WalletViewModel.WalletUiState,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel,
    onSendClicked: () -> Unit = {},
    onReceiveClicked: () -> Unit = {},
    onAddAssetClicked: () -> Unit = {},
) {
    val context = LocalContext.current

    var mainModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
    val userFlow by viewModel.userFlow.collectAsState()
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
    fun refresh() = viewModel.loadAssets(context, state = UiState.Refreshing)
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    val hasAssetsState = remember { mutableStateOf(true) }
    val assets = uiState.assets
    val hasAssets = assets.isNotEmpty()
    hasAssetsState.value = hasAssets
    Timber.d("hasAssets: $hasAssets, hasAssetsState: ${hasAssetsState.value}")
    Box(
        modifier = modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize(),
    ) {
        LazyColumn(modifier = mainModifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),) {
            item {
                Header(Modifier, uiState, onSendClicked, onReceiveClicked, onAddAssetClicked, hasAssets, hasAssetsState)
            }
            assets.forEach {
                item {
                    AssetListItem(supportedAsset = it, clickable = false)
                }
            }
            if (!hasAssetsState.value) {
                item {
                    AddAssetListItem(onAddAssetClicked)
                }
            }
        }

        PullRefreshIndicator(refreshing,
            pullRefreshState,
            modifier.align(Alignment.TopCenter),
            contentColor = primary_blue,
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
           uiState: WalletViewModel.WalletUiState,
           onSendClicked: () -> Unit,
           onReceiveClicked: () -> Unit,
           onShowSupportedAssetsClicked: () -> Unit = {},
           hasAssets: Boolean = true,
           buttonsEnabledState: MutableState<Boolean> = remember { mutableStateOf(true) }
) {
    Column(modifier = modifier
    ) {
        FireblocksText(
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default), start = dimensionResource(id = R.dimen.padding_small), end = dimensionResource(id = R.dimen.padding_small)),
            text = stringResource(id = R.string.balance),
            textStyle = FireblocksNCWDemoTheme.typography.b1
        )
        FireblocksText(
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small), start = dimensionResource(id = R.dimen.padding_small), end = dimensionResource(id = R.dimen.padding_small)),
            text = stringResource(id = R.string.usd_balance, uiState.balance),
            textStyle = FireblocksNCWDemoTheme.typography.h1
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_extra_large_1)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)))
        {
            ColoredButton(
                modifier = Modifier.weight(1f),
                labelResourceId = R.string.send,
                imageResourceId = R.drawable.ic_send,
                onClick = { onSendClicked() },
                enabledState = buttonsEnabledState)
            DefaultButton(
                modifier = Modifier.weight(1f),
                labelResourceId = R.string.receive,
                imageResourceId = R.drawable.ic_receive,
                onClick = onReceiveClicked,
                enabledState = buttonsEnabledState)
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_extra_large_1),
                start = dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default)))
        {
            FireblocksText(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.assets),
                textStyle = FireblocksNCWDemoTheme.typography.h3
            )
            if (hasAssets) {
                Image(
                    modifier = Modifier.clickable { onShowSupportedAssetsClicked() },
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(id = R.string.add_asset)
                )
            }
        }
    }
}

@Composable
fun AddAssetListItem(onAddAssetClicked: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.padding_default)), color = transparent)
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

@Composable
fun AssetListItem(modifier: Modifier = Modifier,
                  supportedAsset: SupportedAsset,
                  showBlockchain: Boolean = true,
                  clickable: Boolean = true,
                  onClick: (SupportedAsset) -> Unit = {}) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.padding_default)), color = transparent)
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small), horizontal = dimensionResource(id = R.dimen.padding_small))
            .clickable(enabled = clickable) { onClick.invoke(supportedAsset) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = grey_1),
        ) {
            CryptoIcon(context, supportedAsset)
        }
        Column(modifier = Modifier.weight(1f)) {
            FireblocksText(
                text = supportedAsset.name,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
            )
            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                FireblocksText(
                    text = supportedAsset.symbol,
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = grey_4
                )
                if (showBlockchain) {
                    Label(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_small)), text = supportedAsset.blockchain)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            FireblocksText(
                text = supportedAsset.balance,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textAlign = TextAlign.End
            )
            if (supportedAsset.price.isNotNullAndNotEmpty()) {
                FireblocksText(
                    text = stringResource(id = R.string.usd_balance, supportedAsset.price),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = grey_4,
                    textAlign = TextAlign.End
                )
            }
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
    assets.add(SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93"))
    viewModel.onAssets(assets)

    viewModel.onBalance("45,873.03")

    FireblocksNCWDemoTheme {
        Surface {
            AssetListScreen(
                uiState = WalletViewModel.WalletUiState(assets = assets),
                viewModel = viewModel,
                onSendClicked = {},
            )
        }
    }
}