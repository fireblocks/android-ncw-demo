package com.fireblocks.sdkdemo.ui.screens.wallet

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.transactions.TransactionSignatureStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.NFTWrapperCard
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TransferDetailsListView
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun TransferApprovalScreen(
    uiState: WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel(),
    onDiscard: () -> Unit = {},
    bottomPadding: Dp = dimensionResource(id = R.dimen.padding_default),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val isDiscardButtonEnabled = remember { mutableStateOf(true) }

    DiscardBottomSheet(
        bottomSheetScaffoldState,
        coroutineScope,
        uiState,
        onNextScreen,
        viewModel,
        bottomPadding,
        isDiscardButtonEnabled)

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
            isDiscardButtonEnabled.value = false
            viewModel.clean()
            onDiscard()
        }
    }

    LaunchedEffect(key1 = uiState.transactionCancelFailed) {
        if (uiState.transactionCancelFailed) {
            Toast.makeText(context, context.getString(R.string.discarded_transaction_failed), Toast.LENGTH_LONG).show()
        }
    }
}

@Preview
@Composable
fun TransferApprovalScreenPreview() {
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
            price = "2,825.04")

        val uiState = WalletUiState(
            selectedAsset = asset,
            assetAmount = "1",
            assetUsdAmount = "1,000",
            sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
            selectedFeeData = fee.low)
    FireblocksNCWDemoTheme {
        Surface {
            TransferApprovalScreen(uiState = uiState)
        }
    }
}


@Composable
fun TransferApprovalMainContent(
    uiState: WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel(),
    bottomPadding: Dp = dimensionResource(id = R.dimen.padding_default)
) {
    val assetAmount = uiState.assetAmount
    val assetUsdAmount = uiState.assetUsdAmount
    val userFlow by viewModel.userFlow.collectAsState()
    uiState.selectedAsset?.let { supportedAsset ->

    val context = LocalContext.current
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    Column(
        modifier = mainModifier,
    ) {
        val status = uiState.transactionWrapper?.status
        val approveEnabledState = remember { mutableStateOf(true) }
        approveEnabledState.value = (userFlow !is UiState.Loading && status == SigningStatus.PENDING_SIGNATURE)

        Column(modifier = Modifier.weight(1f)) {
            var fee = uiState.selectedFeeData?.networkFee
            uiState.selectedNFT?.let { nftWrapper ->
                fee = null
                NFTWrapperCard(nftWrapper = nftWrapper)
            } ?: run {
                TransferAssetView(supportedAsset, assetAmount, assetUsdAmount)
            }

            val txId = uiState.transactionWrapper?.transaction?.id
            TransferDetailsListView(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                rate = uiState.selectedAsset.rate,
                recipientAddress = uiState.sendDestinationAddress,
                fee = fee,
                symbol = uiState.selectedAsset.symbol,
                status = status,
                txId = txId,
            )
        }
        if (userFlow is UiState.Error) {
            ErrorView(errorState = userFlow as UiState.Error, defaultResId = R.string.approve_tx_error)
        }
        if (uiState.showPendingSignatureError) {
            ErrorView(message = stringResource(id = R.string.pending_signature_error))
        }
        ContinueButton(
            approveEnabledState,
            labelResourceId = R.string.approve,
            imageResourceId = R.drawable.ic_approve,
            onClick = {
                val deviceId = viewModel.getDeviceId(context)
                uiState.transactionWrapper?.transaction?.id?.let { txId -> viewModel.approve(context, deviceId, txId) }
            })
        Spacer(modifier = Modifier.padding(bottom = bottomPadding))
    }
    if (showProgress) {
        ProgressBar(R.string.progress_message)
    }

    LaunchedEffect(key1 = uiState.transactionSignature) {
        if (uiState.transactionSignature?.transactionSignatureStatus?.isCompleted() == true) {
            Timber.d("Transaction approval completed, navigating to NextScreen")
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

@Composable
fun TransferAssetView(supportedAsset: SupportedAsset, assetAmount: String, assetUsdAmount: String) {
    val asset = supportedAsset.copy(balance = assetAmount, price = assetUsdAmount)
    Column(modifier = Modifier
        .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
    ) {
        AssetListItem(
            supportedAsset = asset,
            clickable = false
        )
    }
}

@Preview
@Composable
fun TransferApprovalMainContentPreview() {
    FireblocksNCWDemoTheme {
        val fee = Fee(
            FeeData("0.00008", feeLevel = FeeLevel.LOW),
            FeeData("0.0001", feeLevel = FeeLevel.MEDIUM),
            FeeData("0.0002", feeLevel = FeeLevel.HIGH))
        val asset = SupportedAsset(id = "ETH",
            symbol = "ETH_TEST5",
            name = "Ethereum",
            type = "BASE_ASSET",
            blockchain = "Ethereum",
            balance = "132.4",
            price = "2,825.04")
        val uiState = WalletUiState(
            selectedAsset = asset,
            assetAmount = "1.9999",
            assetUsdAmount = "1,000",
            sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
            selectedFeeData = fee.medium,
            transactionWrapper = TransactionWrapper("123"),
            createdTransactionStatus = SigningStatus.PENDING_SIGNATURE)

        Surface {
            TransferApprovalMainContent(uiState = uiState)
        }
    }
}

@Preview
@Composable
fun TransferApprovalMainContentNFTPreview() {
    FireblocksNCWDemoTheme {
        val fee = Fee(
            FeeData("0.00008", feeLevel = FeeLevel.LOW),
            FeeData("0.0001", feeLevel = FeeLevel.MEDIUM),
            FeeData("0.0002", feeLevel = FeeLevel.HIGH))
        val asset = SupportedAsset(id = "ETH",
            symbol = "ETH_TEST5",
            name = "Ethereum",
            type = "BASE_ASSET",
            blockchain = "Ethereum",
            balance = "132.4",
            price = "2,825.04")
        val uiState = WalletUiState(
            selectedAsset = asset,
            assetAmount = "1",
            assetUsdAmount = "0",
            sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
            selectedFeeData = fee.medium,
            transactionWrapper = TransactionWrapper("123"),
            selectedNFT = NFTWrapper(id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91", name = "nft name", collectionName = "collection name",iconUrl =  "https://fireblocks.com/nft.png", blockchain = "ETH"),)
        Surface {
            TransferApprovalMainContent(uiState = uiState)
        }
    }
}

@Composable
fun DiscardBottomSheet (
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    uiState: WalletUiState,
    onNextScreen: () -> Unit = {},
    viewModel: WalletViewModel = viewModel(),
    bottomPadding: Dp = dimensionResource(id = R.dimen.padding_default),
    isDiscardButtonEnabled: MutableState<Boolean>
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
        containerColor = background,
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
                        painter = painterResource(id = R.drawable.ic_error_screen),
                        contentDescription = null,
                    )
                    FireblocksText(
                        text = stringResource(id = R.string.discard_warning),
                        textStyle = FireblocksNCWDemoTheme.typography.h3,
                        textAlign = TextAlign.Center
                    )
                    DefaultButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.cancel_transaction,
                        onClick = {
                            if (isDiscardButtonEnabled.value) {
                                Timber.d("Discard button clicked")
                                isDiscardButtonEnabled.value = false
                                uiState.transactionWrapper?.transaction?.id?.let { txId -> viewModel.discardTransaction(context, txId) }

                            }
                        },
                        enabledState = isDiscardButtonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = grey_1)
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
        TransferApprovalMainContent(
            uiState = uiState,
            onNextScreen = onNextScreen,
            viewModel = viewModel,
            bottomPadding = bottomPadding,
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

        val uiState = WalletUiState()
        val viewModel: WalletViewModel = viewModel()

        DiscardBottomSheet(
            bottomSheetScaffoldState,
            coroutineScope,
            uiState,
            {},
            viewModel,
            isDiscardButtonEnabled = remember { mutableStateOf(true) })
    }
}