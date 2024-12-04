package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIcon
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun SendingScreen(uiState: WalletUiState,
                  onNextScreen: () -> Unit = {}) {
    val assetAmount = uiState.assetAmount
    val assetUsdAmount = uiState.assetUsdAmount
    uiState.selectedAsset?.let { supportedAsset ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
        ) {
            val context = LocalContext.current
            val addressTextState = remember { mutableStateOf("") }
            val continueEnabledState = remember { mutableStateOf(false) }
            continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_sending),
                    contentDescription = null,
                    modifier = Modifier.width(300.dp)
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    text = stringResource(id = R.string.sending_symbol, supportedAsset.symbol),
                    textStyle = FireblocksNCWDemoTheme.typography.h3
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    text = stringResource(id = R.string.sending_description),
                )
                Divider(
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_extra_large)),
                    color = grey_2,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CryptoIcon(context, supportedAsset, paddingResId = R.dimen.padding_extra_small)
                    Column(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        FireblocksText(
                            text = stringResource(id = R.string.asset_amount, assetAmount, supportedAsset.symbol),
                            textStyle = FireblocksNCWDemoTheme.typography.h1
                        )
                        FireblocksText(
                            text = stringResource(id = R.string.usd_balance, assetUsdAmount),
                            textStyle = FireblocksNCWDemoTheme.typography.b2,
                            textColor = grey_4,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {

                    FireblocksText(
                        modifier = Modifier
                            .padding(top = dimensionResource(R.dimen.padding_large)),
                        text = stringResource(id = R.string.receiving_address),
                        textAlign = TextAlign.Start
                    )
                    Label(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large)),
                        text = uiState.sendDestinationAddress,
                        textColor = white
                    )
                }
            }
            ColoredButton(
                modifier = Modifier.fillMaxWidth(),
                labelResourceId = R.string.show_transaction,
                onClick = { onNextScreen() },
            )
        }
    }
}

@Preview
@Composable
fun SendingScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            val fee = Fee(
                FeeData("0.00008", feeLevel = FeeLevel.LOW),
                FeeData("0.0001", feeLevel = FeeLevel.MEDIUM),
                FeeData("0.0002", feeLevel = FeeLevel.HIGH))
            val asset = SupportedAsset(id = "ETH",
                symbol = "ETH",
                name = "Ethereum",
                type = "BASE_ASSET",
                blockchain = "Ethereum",
                balance = "132.4",
                price = "2,825.04",
            )

            val uiState = WalletUiState(
                selectedAsset = asset,
                assetAmount = "1",
                assetUsdAmount = "1,000",
                sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
                selectedFeeData = fee.low)
            SendingScreen(uiState = uiState)
        }
    }
}