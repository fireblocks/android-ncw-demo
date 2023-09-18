package com.fireblocks.sdkdemo.ui.screens.wallet

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.AddressTextField
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.theme.text_grey
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber


/**
 * Created by Fireblocks ltd. on 18/07/2023.
 */
@Composable
fun ReceivingAddressScreen(
    uiState: WalletViewModel.WalletUiState,
    viewModel: WalletViewModel = viewModel(),
    onNextScreen: (address: String) -> Unit = {},
) {
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

            Column(modifier = Modifier.weight(1f)) {
                AssetView(Modifier
                    .fillMaxWidth(),
                    supportedAsset, context, assetAmount, assetUsdAmount)

                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                    text = stringResource(id = R.string.address),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Start
                )
                Row(verticalAlignment = Alignment.CenterVertically){
                    AddressTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                        readOnly = false,
                        text = addressTextState,
                    ) {
                        onContinueClick(viewModel, onNextScreen, addressTextState)
                    }

                    val scannedQRCode = remember { mutableStateOf("")}
                    if (scannedQRCode.value.isNotEmpty()){
                        addressTextState.value = scannedQRCode.value
                    }

                    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
                        if (result.contents == null) {
                            val originalIntent: Intent = result.originalIntent
                            if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                                Timber.d("Cancelled scan due to missing camera permission")
                                Toast.makeText(context, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Timber.d("Scanned")
                            scannedQRCode.value = result.contents
                        }
                    }

                    Image(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                            .clickable {
                                barcodeLauncher.launch(
                                    ScanOptions()
                                        .setOrientationLocked(true)
                                        .setPrompt("QR Scan")
                                )
                            },
                        painter = painterResource(id = R.drawable.ic_scan_qr),
                        contentDescription = ""
                    )
                }

            }
            ContinueButton(continueEnabledState,
                onClick = {
                    onContinueClick(viewModel, onNextScreen, addressTextState)
                })
        }
    }
}

@Composable
fun AssetView(
    modifier: Modifier,
    supportedAsset: SupportedAsset,
    context: Context,
    assetAmount: String,
    assetUsdAmount: String,
    assetAmountTextStyle : TextStyle = FireblocksNCWDemoTheme.typography.h1) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_extra_small)),
            painter = painterResource(id = supportedAsset.getIcon(context)),
            contentDescription = ""
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.padding_small)),
        ) {
            FireblocksText(
                text = stringResource(id = R.string.asset_amount, assetAmount, supportedAsset.symbol),
                textStyle = assetAmountTextStyle
            )
            FireblocksText(
                text = stringResource(id = R.string.usd_balance, assetUsdAmount),
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textColor = text_grey,
                textAlign = TextAlign.End
            )
        }
    }
}

private fun onContinueClick(viewModel: WalletViewModel,
                            onNextScreen: (address: String) -> Unit,
                            addressTextState: MutableState<String>) {
//    viewModel.loadFee()//TODO implement progress
    onNextScreen(addressTextState.value)
}

@Preview
@Composable
fun ReceivingAddressScreenPreview() {
    val uiState = WalletViewModel.WalletUiState(
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

