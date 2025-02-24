package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIcon
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.rememberQrBitmap
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun ReceiveScreen(uiState: WalletUiState) {
    uiState.selectedAsset?.let { supportedAsset ->
        val receiveAddress = supportedAsset.assetAddress
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
        ) {
            val context = LocalContext.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_default)),
                    colors = CardDefaults.cardColors(containerColor = transparent),
                ) {
                    CryptoIcon(context, supportedAsset.symbol, paddingResId = R.dimen.padding_extra_small)
                }
                FireblocksText(
                    text = supportedAsset.name,
                    textStyle = FireblocksNCWDemoTheme.typography.h3,
                    maxLines = 1
                )
                Label(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)), text = supportedAsset.blockchain)
            }
            FireblocksText(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small), end = dimensionResource(id = R.dimen.padding_small)),
                text = stringResource(id = R.string.receive_description, supportedAsset.symbol))
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_large), bottom = dimensionResource(id = R.dimen.padding_default))
                .width(342.dp)
                .height(342.dp),
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                colors = CardDefaults.cardColors(containerColor = grey_1),
            ) {
                if (receiveAddress != null && receiveAddress.isNotNullAndNotEmpty()) {
                    Image(
                        bitmap = rememberQrBitmap(receiveAddress),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(id = R.dimen.qr_code_padding))
                    )
                }
            }
            Card(modifier = Modifier,
                shape = RoundedCornerShape(size = 16.dp),
                colors = CardDefaults.cardColors(containerColor = grey_1),
            ) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default))){
                    TitleContentView(
                        titleText = stringResource(id = R.string.address_with_asset, supportedAsset.symbol),
                        titleColor = white,
                        contentText = receiveAddress,
                        contentColor = grey_4,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, receiveAddress) },
                        topPadding = null,
                        contentDescriptionText = stringResource(id = R.string.address_value_desc),
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun ReceiveScreenPreview() {
    val uiState = WalletUiState(selectedAsset = SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93",
        assetAddress = "bc1q9sc3gyfe7mp3ndpec5gdrnfh6aplf3re0xufgh"
    ))
    FireblocksNCWDemoTheme {
        Surface {
            ReceiveScreen(uiState)
        }
    }
}