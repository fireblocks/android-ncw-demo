package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ReceivingAddressView
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun ReceivingAddressScreen(
    uiState: WalletUiState,
    onNextScreen: (address: String) -> Unit = {},
) {
    val assetAmount = uiState.assetAmount
    val assetUsdAmount = uiState.assetUsdAmount
    uiState.selectedAsset?.let { supportedAsset ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
        ) {
            TransferAssetView(
                supportedAsset = supportedAsset,
                assetAmount = assetAmount,
                assetUsdAmount = assetUsdAmount)

            ReceivingAddressView(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                onContinueClicked = { address ->
                    onNextScreen(address)
                }
            )
        }
    }
}

@Preview
@Composable
fun ReceivingAddressScreenPreview() {
    val uiState = WalletUiState(
        selectedAsset = SupportedAsset(id = "BTC",
            symbol = "BTC",
            name = "Bitcoin",
            type = "BASE_ASSET",
            blockchain = "Bitcoin",
            balance = "2.48",
            price = "41,044.93"),
        assetAmount = "0.01",
        assetUsdAmount = "2,472.92"
    )

    FireblocksNCWDemoTheme {
        Surface {
            ReceivingAddressScreen(uiState = uiState)
        }
    }
}

