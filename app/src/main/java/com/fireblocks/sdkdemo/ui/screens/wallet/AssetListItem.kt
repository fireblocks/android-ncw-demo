package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIcon
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent

@Composable
fun AssetListItem(modifier: Modifier = Modifier,
                  supportedAsset: SupportedAsset,
                  showBlockchain: Boolean = true,
                  clickable: Boolean = true,
                  onClick: (SupportedAsset) -> Unit = {}) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .padding(end = dimensionResource(id = R.dimen.padding_default))
            .background(
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item)),
                color = transparent
            )
            .clickable(enabled = clickable) { onClick.invoke(supportedAsset) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = grey_2),
        ) {
            CryptoIcon(context, supportedAsset = supportedAsset, symbol = supportedAsset.symbol)
        }
        Column(modifier = Modifier.weight(1f)) {
            FireblocksText(
                text = supportedAsset.name,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
            )
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FireblocksText(
                    text = supportedAsset.symbol,
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = text_secondary
                )
                if (showBlockchain) {
                    Label(
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_small)),
                        text = supportedAsset.blockchain
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            FireblocksText(
                text = supportedAsset.balance?.roundToDecimalFormat(),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textAlign = TextAlign.End
            )
            if (supportedAsset.price.isNotNullAndNotEmpty()) {
                FireblocksText(
                    text = stringResource(id = R.string.usd_balance, supportedAsset.price),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_secondary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun AssetListItemPreview() {
    val supportedAsset = SupportedAsset(
        id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum",
        balance = "132.4",
        price = "2,825.04"
    )

    FireblocksNCWDemoTheme {
        Surface {
            AssetListItem(supportedAsset = supportedAsset)
        }
    }
}