package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIconCard
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.utils.AssetsUtils
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent

@Composable
fun AssetListItem(modifier: Modifier = Modifier,
                  supportedAsset: SupportedAsset,
                  clickable: Boolean = true,
                  onClick: (SupportedAsset) -> Unit = {}) {
    Row(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_default))
            .background(
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item)),
                color = transparent
            )
            .clickable(enabled = clickable) { onClick.invoke(supportedAsset) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CryptoIconCard(iconUrl = supportedAsset.iconUrl, symbol = supportedAsset.symbol, blockchain = supportedAsset.blockchain)

        Column(modifier = Modifier.weight(1f).padding(start = dimensionResource(id = R.dimen.padding_default), end = dimensionResource(R.dimen.padding_small))) {
            FireblocksText(
                text = AssetsUtils.getAssetTitleText(LocalContext.current, supportedAsset),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                maxLines = 1
            )
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FireblocksText(
                    text = supportedAsset.name,
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = text_secondary,
                    maxLines = 1
                )
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

@Preview
@Composable
fun AssetListItemPreview() {
    val supportedAsset = SupportedAsset(
        id = "ETH",
        symbol = "ETH",
        name = "Ether",
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