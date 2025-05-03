package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.NFTWrapperCard
import com.fireblocks.sdkdemo.ui.compose.components.TransferDetailsListView
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.TransfersViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun SendingScreen(uiState: WalletUiState,
                  transfersViewModel: TransfersViewModel = viewModel(),
                  onHomeClicked: () -> Unit = {}) {
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
                    painter = painterResource(R.drawable.ic_transfer),
                    contentDescription = null,
                    modifier = Modifier.width(300.dp)
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    text = stringResource(id = R.string.transferring),
                    textStyle = FireblocksNCWDemoTheme.typography.h3
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_large)),
                    text = stringResource(id = R.string.sending_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = text_secondary
                )
                var showAssetItem = true
                val nftWrapper = uiState.selectedNFT
                nftWrapper?.let {
                    showAssetItem = false
                    NFTWrapperCard(nftWrapper = it)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
                }

                transfersViewModel.loadTransactions(context)
                val transfersUiState by transfersViewModel.uiState.collectAsState()
                val transactions = transfersUiState.transactions
                val txId = uiState.transactionWrapper?.transaction?.id
                val transactionWrapper = transactions.find { it.id == txId } ?: uiState.transactionWrapper
                TransferDetailsListView(
                    isOutgoingTransaction = true,
                    rate = uiState.selectedAsset.rate,
                    showAssetItem = showAssetItem,
                    supportedAsset = supportedAsset,
                    assetAmount = assetAmount,
                    assetUsdAmount = assetUsdAmount,
                    recipientAddress = uiState.sendDestinationAddress,
                    fee = transactionWrapper?.networkFee?.roundToDecimalFormat() ?: "0",
                    symbol = uiState.selectedAsset.symbol,
                    status = transactionWrapper?.status,
                    txId = txId,
                    txHash = transactionWrapper?.txHash,
                    nftId= nftWrapper?.id,
                )
            }
            DefaultButton(
                modifier = Modifier.fillMaxWidth(),
                labelResourceId = R.string.go_home,
                onClick = onHomeClicked,
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
                selectedNFT = NFTWrapper(id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91", name = "Bored Ape Yacht Club", balance = "1", standard = "ERC721", blockchain = "Ethereum"),
                selectedAsset = asset,
                assetAmount = "1",
                assetUsdAmount = "1,000",
                sendDestinationAddress = "0x324387ynckc83y48fhlc883mf",
                selectedFeeData = fee.low,
                transactionWrapper = TransactionWrapper(deviceId = "123"),
                createdTransactionStatus = SigningStatus.PENDING_SIGNATURE
            )
            SendingScreen(uiState = uiState)
        }
    }
}