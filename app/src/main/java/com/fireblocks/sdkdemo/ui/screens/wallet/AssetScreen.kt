package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks ltd. on 18/07/2023.
 */
@Composable
fun AssetScreen(
    uiState: WalletViewModel.WalletUiState,
    onNextScreen: (SupportedAsset) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default)),
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),) {
            val assets = uiState.assets
            assets.forEach {
                item {
                    AssetListItem(
                        supportedAsset = it,
                        showBlockchain = false,
                        onClick = {
                            onNextScreen(it)
                        })
                }
            }
        }
    }
}

@Preview
@Composable
fun AssetScreenPreview() {
    val assets = arrayListOf<SupportedAsset>()
    // TODO DELETE MOCK
    assets.add(SupportedAsset(id = "ETH",
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
    assets.add(SupportedAsset(id = "ADA", symbol = "ADA", name = "Cardano", type = "BASE_ASSET", blockchain = "Cardano"))
    assets.add(SupportedAsset(id = "AVAX", symbol = "AVAX", name = "Avalanche", type = "BASE_ASSET", blockchain = "Avalanche"))
    assets.add(SupportedAsset(id = "MATIC", symbol = "MATIC", name = "Polygon", type = "BASE_ASSET", blockchain = "Polygon"))
    assets.add(SupportedAsset(id = "USDT", symbol = "USDT", name = "Tether", type = "BASE_ASSET", blockchain = "Ethereum"))

    FireblocksNCWDemoTheme {
        Surface {
            AssetScreen(uiState = WalletViewModel.WalletUiState(assets = assets))
        }
    }
}