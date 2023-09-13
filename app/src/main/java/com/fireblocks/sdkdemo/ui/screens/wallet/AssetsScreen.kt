package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdkdemo.R
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
import com.fireblocks.sdkdemo.ui.theme.text_grey
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks Ltd. on 11/07/2023.
 */
@Composable
fun AssetsScreen(
    uiState: WalletViewModel.WalletUiState,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel,
    onSendClicked: () -> Unit = {},
    onReceiveClicked: () -> Unit = {},
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
            .alpha(0.5f)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(modifier = mainModifier
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
                .padding(top = dimensionResource(id = R.dimen.padding_default)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)))
            {
                ColoredButton(
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.send,
                    imageResourceId = R.drawable.ic_send,
                    onClick = { onSendClicked() })
                DefaultButton(
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.receive,
                    imageResourceId = R.drawable.ic_receive,
                    onClick = onReceiveClicked,
                )
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_extra_large_1),
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default)))
            {
                FireblocksText(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.assets),
                    textStyle = FireblocksNCWDemoTheme.typography.h3
                )
                Image(
                    modifier = Modifier.clickable { viewModel.loadAssets(context) },
                    painter = painterResource(id = R.drawable.ic_reload),
                    contentDescription = ""
                )
            }
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.padding_default), horizontal = dimensionResource(id = R.dimen.padding_small)),
                verticalArrangement = Arrangement.spacedBy(10.dp),) {
                val assets = uiState.assets
                assets.forEach {
                    item {
                        AssetListItem(supportedAsset = it, clickable = false)
                    }
                }
            }
        }
        if (showProgress) {
            ProgressBar()
        }
    }

    OnLifecycleEvent { owner, event ->
        when (event){
            Lifecycle.Event.ON_CREATE -> { viewModel.loadAssets(context) }
            else -> {}
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
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small))
            .clickable(enabled = clickable) { onClick.invoke(supportedAsset) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = grey_1),
        ) {
            Image(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small_1)),
                painter = painterResource(id = supportedAsset.getIcon(context)),
                contentDescription = ""
            )
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
                    textColor = text_grey
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
            FireblocksText(
                text = stringResource(id = R.string.usd_balance, supportedAsset.price),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = text_grey,
                textAlign = TextAlign.End
            )
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
        price = "0.29"))
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
            AssetsScreen(
                uiState = WalletViewModel.WalletUiState(assets = assets),
                viewModel = viewModel,
                onSendClicked = {},
            )
        }
    }
}