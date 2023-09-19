package com.fireblocks.sdkdemo.ui.screens.wallet

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.transactions.TransactionSignatureStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.EXTENDED_PATTERN
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.SignOutBottomSheet
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_grey
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by Fireblocks ltd. on 19/07/2023.
 */
@Composable
fun PreviewScreen(
    uiState: WalletViewModel.WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel(),
    onDiscard: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    DiscardBottomSheet(
        bottomSheetScaffoldState,
        coroutineScope,
        uiState,
        onNextScreen,
        viewModel,
        onDiscard)

    LaunchedEffect(key1 = uiState.closeWarningClicked) {
        if (uiState.closeWarningClicked) {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.expand()
                viewModel.onCloseWarningClicked(false)
            }
        }
    }

    LaunchedEffect(key1 = uiState.transactionCanceled) {
        if (uiState.transactionCanceled) {
            Toast.makeText(context, context.getString(R.string.discarded_transaction), Toast.LENGTH_LONG).show()
            viewModel.clean()
            onDiscard()
        }
    }
}

@Preview
@Composable
fun PreviewScreenPreview() {
    FireblocksNCWDemoTheme {
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
            fee = fee)

        val uiState = WalletViewModel.WalletUiState(
            selectedAsset = asset,
            assetAmount = "1",
            assetUsdAmount = "1,000",
            sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
            selectedFeeData = fee.low)
        Surface {
            PreviewScreen(uiState = uiState)
        }
    }
}


@Composable
fun PreviewMainContent(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    uiState: WalletViewModel.WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel()
) {
    val assetAmount = uiState.assetAmount
    val assetUsdAmount = uiState.assetUsdAmount
    val userFlow by viewModel.userFlow.collectAsState()
    uiState.selectedAsset?.let { supportedAsset ->

        val context = LocalContext.current
        var mainModifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default))
        val showProgress = userFlow is UiState.Loading
        if (showProgress) {
            mainModifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default))
                .alpha(0.5f)
                .clickable(
                    indication = null, // disable ripple effect
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { }
                )
        }

        Column(
            modifier = mainModifier,
        ) {
            val addressTextState = remember { mutableStateOf("") }
            val approveEnabledState = remember { mutableStateOf(true) }
            approveEnabledState.value = userFlow !is UiState.Loading
            val feeAmountAsDouble = uiState.selectedFeeData?.networkFee?.toDouble() ?: 0.0
            val totalPlusFee = (uiState.assetAmount.toDouble() + feeAmountAsDouble)
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val totalPlusFeeUsd = df.format(totalPlusFee * uiState.selectedAsset.rate).toDouble()

            Column(modifier = Modifier.weight(1f)) {
                FireblocksText(
                    text = stringResource(id = R.string.your_are_sending),
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.padding_default)),
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
                            .weight(1f),
                    ) {
                        FireblocksText(
                            text = stringResource(id = R.string.asset_amount, assetAmount, supportedAsset.symbol),
                            textStyle = FireblocksNCWDemoTheme.typography.h1
                        )
                        FireblocksText(
                            text = stringResource(id = R.string.usd_balance, assetUsdAmount),
                            textStyle = FireblocksNCWDemoTheme.typography.b2,
                            textColor = text_grey,
                            textAlign = TextAlign.End
                        )
                    }
                }
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                    text = stringResource(id = R.string.receiving_address),
                    textAlign = TextAlign.Start
                )
                Label(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large)),
                    text = uiState.sendDestinationAddress,
                    textColor = white,
                    shape = RoundedCornerShape(size = 4.dp)
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.padding_large)),
                    color = grey_2,
                )
                //Fee
                Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_large))) {
                    FireblocksText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.fee),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                    )
                    FireblocksText(
                        text = stringResource(id = R.string.asset_amount, uiState.selectedFeeData?.networkFee?.roundToDecimalFormat() ?: "0", supportedAsset.symbol),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                    )
                }
                //Total
                Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_large))) {
                    FireblocksText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.total),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        FireblocksText(
                            text = stringResource(id = R.string.asset_amount, totalPlusFee.roundToDecimalFormat(EXTENDED_PATTERN), supportedAsset.symbol),
                            textAlign = TextAlign.End
                        )
                        FireblocksText(
                            text = stringResource(id = R.string.usd_balance, totalPlusFeeUsd),
                            textColor = text_grey,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
            if (userFlow is UiState.Error) {
                ErrorView(message = stringResource(id = R.string.approve_tx_error))
            }
            ContinueButton(
                approveEnabledState,
                labelResourceId = R.string.approve,
                imageResourceId = R.drawable.ic_approve,
                onClick = {
                    val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
                    uiState.transactionWrapper?.transaction?.id?.let { txId -> viewModel.approve(context, deviceId, txId) }
                })
        }
        if (showProgress) {
            ProgressBar(R.string.progress_message)
        }
        LaunchedEffect(key1 = uiState.transactionSignature) {
            if (uiState.transactionSignature?.transactionSignatureStatus?.isCompleted() == true) {
                onNextScreen()
            }
        }
    }
}

private fun TransactionSignatureStatus.isCompleted(): Boolean {
    return when (this) {
        TransactionSignatureStatus.COMPLETED -> true
        else -> false
    }
}

@Preview
@Composable
fun PreviewMainContentPreview() {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    FireblocksNCWDemoTheme {
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
            fee = fee)

        val uiState = WalletViewModel.WalletUiState(
            selectedAsset = asset,
            assetAmount = "1",
            assetUsdAmount = "1,000",
            sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
            selectedFeeData = fee.medium)
        Surface {
            PreviewMainContent(
                bottomSheetScaffoldState = bottomSheetScaffoldState,
                coroutineScope = coroutineScope,
                uiState = uiState)
        }
    }
}

@Composable
fun DiscardBottomSheet (
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    uiState: WalletViewModel.WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel(),
    onDiscard: () -> Unit = {},
) {
    val context = LocalContext.current
    BottomSheetScaffold(
        modifier = Modifier.pointerInput(Unit){
            detectTapGestures(onTap = {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }
                }
            })
        },
        sheetContainerColor = grey_2,
        containerColor = black,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Column(modifier = Modifier
                .fillMaxHeight(fraction = 0.5f)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_default)
                    )
                ) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = painterResource(id = R.drawable.ic_discard),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = 13.dp),
                        text = stringResource(id = R.string.discard_warning),
                        textStyle = FireblocksNCWDemoTheme.typography.h3,
                        textAlign = TextAlign.Center
                    )
                    ColoredButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.discard,
                        onClick = {
                            coroutineScope.launch {
                                uiState.transactionWrapper?.transaction?.id?.let { txId -> viewModel.discardTransaction(context, txId) }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = error, contentColor = Color.White),
                    )
                    TransparentButton(
                        labelResourceId = R.string.never_mind,
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }
                    )
                }
            }
        }
    ) {
        PreviewMainContent(
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            coroutineScope = coroutineScope,
            uiState = uiState,
            onNextScreen = onNextScreen,
            viewModel = viewModel,
        )
    }
}

@Preview
@Composable
fun DiscardBottomSheetPreview(){
    FireblocksNCWDemoTheme {
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded,
                skipHiddenState = false
            )
        )
        SignOutBottomSheet(bottomSheetScaffoldState, coroutineScope, {}, {}, {}, {})
    }
}