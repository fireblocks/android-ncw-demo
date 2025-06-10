package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.hasFailed
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TransferDetailsListView
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.viewmodel.TransfersViewModel
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun TransferDetailsScreen(transactionWrapper: TransactionWrapper? = null,
                          viewModel: TransfersViewModel = viewModel(),
                          onGoBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    viewModel.loadTransactions(context)

    val transactions = uiState.transactions
    val txId = transactionWrapper?.transaction?.id
    val justApproved = transactionWrapper?.justApproved ?: false
    val selectedTransactionWrapper = transactions.find { it.id == txId }

    selectedTransactionWrapper?.let {
        val userFlow by viewModel.userFlow.collectAsState()
        val feeCurrency = it.feeCurrency ?: ""
        val symbol = it.blockchainSymbol

        val amount = it.amount?.roundToDecimalFormat() ?: "0.0"
        val amountUSD = it.amountUSD?.roundToDecimalFormat() ?: "0.0"

        val supportedAsset = transactionWrapper?.supportedAsset ?: SupportedAsset(
            id = it.assetId ?: "",
            type = feeCurrency,
            blockchain = it.blockchainSymbol,
            name = it.assetName,
            symbol = symbol
        )

        val createdAt = it.createdAt?.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", timeFormat = "HH:mm", useSpecificDays = false)
        val deviceId = viewModel.getDeviceId(context = LocalContext.current)
        val isOutgoingTransaction = it.isOutgoingTransaction(LocalContext.current, deviceId)
        val address = if (isOutgoingTransaction) {
            it.destinationAddress
        } else {
            it.sourceAddress
        }
        val feeAmount = it.networkFee?.roundToDecimalFormat() ?: "0"
        val txHash = it.txHash
        val status = it.status

        val showProgress = userFlow is UiState.Loading
        val mainModifier = Modifier.createMainModifier(showProgress)

        if (showProgress) {
            ProgressBar()
        }

        Column(
            modifier = mainModifier,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TransferScreenContent(
                    isOutgoingTransaction = isOutgoingTransaction,
                    amount,
                    amountUSD,
                    supportedAsset,
                    symbol,
                    status,
                    createdAt,
                    address,
                    feeAmount,
                    txHash,
                    txId,
                    nftWrapper = transactionWrapper?.nftWrapper
                )
            }

            if (userFlow is UiState.Error) {
                Timber.d("status $status, justApproved $justApproved")
                ErrorView(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                    errorState = userFlow as UiState.Error, defaultResId = R.string.failed_try_again)
            }
            if ((status == SigningStatus.PENDING_SIGNATURE || status == SigningStatus.BROADCASTING) && !justApproved) {
                Row(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_default)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                    DefaultButton(
                        modifier = Modifier.weight(1f),
                        labelResourceId = R.string.deny,
                        onClick = {
                            viewModel.deny(context, txId ?: "")
                        },
                    )
                    DefaultButton(
                        modifier = Modifier.weight(1f),
                        labelResourceId = R.string.approve,
                        onClick = {
                            viewModel.approve(context, txId ?: "")
                        },
                    )
                }
            }

        }
        LaunchedEffect(key1 = uiState.transactionCanceled) {
            if (uiState.transactionCanceled){
                onGoBack()
            }
        }

        LaunchedEffect(key1 = uiState.transactionSignature) {
            val transactionSignature = uiState.transactionSignature
            if (transactionSignature != null && !transactionSignature.transactionSignatureStatus.hasFailed()) {
                onGoBack()
            }
        }
    }
}

@Composable
private fun TransferScreenContent(
    isOutgoingTransaction: Boolean,
    amount: String,
    amountUSD: String,
    supportedAsset: SupportedAsset,
    symbol: String,
    status: SigningStatus,
    createdAt: String?,
    address: String?,
    feeAmount: String,
    txHash: String? = null,
    txId: String?,
    nftWrapper: NFTWrapper? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransferDetailsListView(
            rate = supportedAsset.rate,
            showAssetItem = true,
            isOutgoingTransaction = isOutgoingTransaction,
            supportedAsset = supportedAsset,
            assetAmount = amount,
            assetUsdAmount = amountUSD,
            recipientAddress = address,
            fee = feeAmount,
            symbol = symbol,
            status = status,
            txId = txId,
            txHash = txHash,
            nftId= nftWrapper?.id,
            creationDate = createdAt,
            nftWrapper = nftWrapper
        )
    }
}

@Preview
@Composable
fun TransferScreenContentPreview(){
    FireblocksNCWDemoTheme{
        Surface(color = background) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val supportedAsset = SupportedAsset(
                    id = "ETH",
                    symbol = "ETH",
                    name = "Ether",
                    type = "BASE_ASSET",
                    blockchain = "Ethereum",
                    iconUrl = "",
                    balance = "1.0",
                    price = "2000.0"
                )

                TransferScreenContent(
                    isOutgoingTransaction = false,
                    amount = "0.0001",
                    amountUSD = "2.0",
                    supportedAsset = supportedAsset,
                    symbol = "symbol",
                    status = SigningStatus.PENDING_SIGNATURE,
                    createdAt = "createdAt",
                    address = "0x324387ynckc83y48fhlc883mf",
                    feeAmount = "0.00002",
                    txHash = "txHash",
                    txId = "b4cf722f-34e9-47d0-b206-81c641ae87c7",
                )
            }
        }
    }
}