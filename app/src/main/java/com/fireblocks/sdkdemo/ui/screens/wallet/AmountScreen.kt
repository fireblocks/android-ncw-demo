package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun AmountScreen(
    uiState: WalletViewModel.WalletUiState,
    onNextScreen: (amount: String, amountUsd: String) -> Unit = { _: String, _: String -> },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default)),
    ) {
        uiState.selectedAsset?.let { supportedAsset ->
            val amountTextState = remember { mutableStateOf("0") }
            val usdAmountTextState = remember { mutableStateOf("0.0") }
//            val continueEnabledState = remember { mutableStateOf(false) } //TODO uncomment when QA is done
            val continueEnabledState = remember { mutableStateOf(true) } //TODO delete this when QA is done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssetListItem( //TODO add background
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(id = R.dimen.padding_default)),
                    supportedAsset = supportedAsset,
                    showBlockchain = false)
                DefaultButton(
                    modifier = Modifier.height(dimensionResource(id = R.dimen.max_button_height)),
                    labelResourceId = R.string.max,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = {
                        amountTextState.value = supportedAsset.balance
                        updateUsdAmount(usdAmountTextState, amountTextState, supportedAsset)
                        updateContinueEnabledState(continueEnabledState, amountTextState, supportedAsset)
                    })
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                val amountValueDescriptor = stringResource(id = R.string.amount_value_desc)
                FireblocksText(
                    modifier = Modifier.semantics { contentDescription = amountValueDescriptor },
                    text = stringResource(id = R.string.asset_amount, amountTextState.value, supportedAsset.symbol),
                    textStyle = FireblocksNCWDemoTheme.typography.bigText
                )
                val amountUsdValueDescriptor = stringResource(id = R.string.amount_usd_value_desc)
                FireblocksText(
                    modifier = Modifier.semantics { contentDescription = amountUsdValueDescriptor },
                    text = stringResource(id = R.string.usd_balance, usdAmountTextState.value),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = grey_4,
                    textAlign = TextAlign.End
                )
                if ((supportedAsset.balance.toDouble() == 0.0) || (amountTextState.value > supportedAsset.balance)) {
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
                        text = stringResource(id = R.string.usd_balance_error, supportedAsset.balance, supportedAsset.symbol),
                        textStyle = FireblocksNCWDemoTheme.typography.b2,
                        textColor = error,
                        textAlign = TextAlign.End
                    )
                }
            }
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val keyPads = arrayListOf<KeyPad>()
                    for (i in 1..9) {
                        keyPads.add(
                            KeyPad(
                                value = i.toString(),
                                onClick = {
                                    updateAmount(amountTextState, i, usdAmountTextState, continueEnabledState, supportedAsset)
                                }
                            )
                        )
                    }
                    keyPads.add(KeyPad(value = ".", onClick = {
                        amountTextState.value += "."
                    }))
                    keyPads.add(KeyPad(value = "0", onClick = { updateAmount(amountTextState, 0, usdAmountTextState, continueEnabledState, supportedAsset) }))
                    keyPads.add(KeyPad(imageResourceId = R.drawable.ic_left_arrow,
                        onClick = {
                            val updatedValue = amountTextState.value.dropLast(1)
                            when (updatedValue.length) {
                                0 -> amountTextState.value = "0"
                                else -> amountTextState.value = updatedValue
                            }
                            updateUsdAmount(usdAmountTextState, amountTextState, supportedAsset)
                            updateContinueEnabledState(continueEnabledState, amountTextState, supportedAsset)
                        }
                    ))
                    keyPads.forEach {
                        item {
                            DefaultButton(
                                modifier = Modifier.height(72.dp),
                                labelText = it.value,
                                imageResourceId = it.imageResourceId,
                                textStyle = FireblocksNCWDemoTheme.typography.h2,
                                colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                                onClick = it.onClick,
                                contentDescription = it.value
                            )
                        }
                    }
                }

                ContinueButton(continueEnabledState,
                    onClick = {
                    onNextScreen(amountTextState.value, usdAmountTextState.value)
                })
            }
        }
    }
}

private fun updateAmount(amountTextState: MutableState<String>,
                         value: Int,
                         usdAmountTextState: MutableState<String>,
                         continueEnabledState: MutableState<Boolean>,
                         asset: SupportedAsset) {
    if (amountTextState.value == "0") {
        amountTextState.value = value.toString()
    } else {
        amountTextState.value += value
    }
    updateUsdAmount(usdAmountTextState, amountTextState, asset)
    updateContinueEnabledState(continueEnabledState, amountTextState, asset)
}

private fun updateContinueEnabledState(continueEnabledState: MutableState<Boolean>,
                                       amountTextState: MutableState<String>,
                                       asset: SupportedAsset) {
    //TODO uncomment when QA is done
    //continueEnabledState.value = (asset.balance.toDouble() > 0) && (amountTextState.value.toDouble() > 0) && (amountTextState.value <= asset.balance)
}

private fun updateUsdAmount(usdAmountText: MutableState<String>,
                            amountText: MutableState<String>,
                            asset: SupportedAsset
) {
    usdAmountText.value = (amountText.value.toDouble() * asset.rate).roundToDecimalFormat()
}

data class KeyPad(val value: String = "", val imageResourceId: Int? = null, val onClick: () -> Unit)

@Preview
@Composable
fun AmountScreenPreview() {
    val uiState = WalletViewModel.WalletUiState(selectedAsset = SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93"))

    FireblocksNCWDemoTheme {
        Surface {
            AmountScreen(uiState = uiState)
        }
    }
}

