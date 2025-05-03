package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.blockchain.BlockchainProvider
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIconCard
import com.fireblocks.sdkdemo.ui.compose.components.DetailsListItem
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksHorizontalDivider
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.rememberQrBitmap
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CryptoIconCard(
                    imageSizeResId = R.dimen.receive_icon_size,
                    blockchainImageSizeResId = R.dimen.receive_blockchain_icon_size,
                    iconUrl = supportedAsset.iconUrl,
                    symbol = supportedAsset.symbol,
                    blockchain = supportedAsset.blockchain
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                    text = supportedAsset.name,
                    textStyle = FireblocksNCWDemoTheme.typography.h3,
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_small)),
                    text = BlockchainProvider.getBlockchainDisplayName(context = LocalContext.current, supportedAsset.blockchain),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_secondary
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen.padding_extra_large))
                    .width(358.dp)
                    .height(358.dp),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.round_corners_list_item),
                    topEnd = dimensionResource(id = R.dimen.round_corners_list_item)
                ),
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
            Card(
                modifier = Modifier,
                shape = RoundedCornerShape(
                    bottomStart = dimensionResource(id = R.dimen.round_corners_list_item),
                    bottomEnd = dimensionResource(id = R.dimen.round_corners_list_item)
                ),
                colors = CardDefaults.cardColors(containerColor = grey_1),
            ) {
                FireblocksHorizontalDivider()
                DetailsListItem(titleResId = R.string.wallet_address, contentText = receiveAddress, showCopyButton = true)
            }
        }
    }
}


@Preview
@Composable
fun ReceiveScreenPreview() {
    val uiState = WalletUiState(
        selectedAsset = SupportedAsset(
            id = "BTC",
            symbol = "BTC",
            name = "Bitcoin",
            type = "BASE_ASSET",
            blockchain = "Bitcoin",
            balance = "2.48",
            price = "41,044.93",
            assetAddress = "bc1q9sc3gyfe7mp3ndpec5gdrnfh6aplf3re0xufgh"
        )
    )
    FireblocksNCWDemoTheme {
        Surface {
            ReceiveScreen(uiState)
        }
    }
}