package com.fireblocks.sdkdemo.ui.screens.wallet

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIconCard
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.theme.text_secondary

@Composable
fun AssetView(
    modifier: Modifier,
    context: Context,
    supportedAsset: SupportedAsset,
    name: String,
    symbol: String,
    blockchain : String,
    assetAmount: String,
    assetUsdAmount: String,
    assetAmountTextStyle : TextStyle = FireblocksNCWDemoTheme.typography.h1) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CryptoIconCard(iconUrl = supportedAsset.iconUrl, symbol = symbol, blockchain = blockchain)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.padding_small)),
        ) {
            FireblocksText(
                text = stringResource(id = R.string.asset_amount, assetAmount, name),
                textStyle = assetAmountTextStyle
            )
            FireblocksText(
                text = stringResource(id = R.string.usd_balance, assetUsdAmount),
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textColor = text_secondary,
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview
@Composable
fun AssetViewPreview() {
    FireblocksNCWDemoTheme {
        AssetView(
            modifier = Modifier,
            context = androidx.compose.ui.platform.LocalContext.current,
            supportedAsset = SupportedAsset(
                id = "BTC",
                type = "BTC",
                name = "Bitcoin",
                symbol = "BTC",
                blockchain = "Bitcoin",
                assetAddress = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                decimals = 8,
                iconUrl = "https://fireblocks-public.s3.amazonaws.com/images/coins/64x64/btc.png"
            ),
            name = "BTC",
            symbol = "BTC",
            blockchain = "Bitcoin",
            assetAmount = "0.0001",
            assetUsdAmount = "0.01"
        )
    }
}