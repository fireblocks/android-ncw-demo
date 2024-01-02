package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.bl.core.server.models.AmountInfo
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionDetails
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.StatusLabel
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.viewmodel.TransfersViewModel

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun TransferScreen(transactionWrapper: TransactionWrapper? = null,
                   viewModel: TransfersViewModel = viewModel(),
                   onGoBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    viewModel.loadTransactions(context)

    val transactions = uiState.transactions
    val txId = transactionWrapper?.transaction?.id
    val selectedTransactionWrapper = transactions.find { it.transaction.id == txId }

    selectedTransactionWrapper?.let {
        val userFlow by viewModel.userFlow.collectAsState()

        val transactionDetails = it.transaction.details
        val feeCurrency = transactionDetails?.feeCurrency ?: ""
        val supportedAsset = SupportedAsset(
            id = transactionDetails?.assetId ?: "",
            type = feeCurrency,
            symbol = transactionDetails?.assetId ?: "")
        val amount = transactionDetails?.amountInfo?.amount?.roundToDecimalFormat() ?: 0.0
        val amountUSD = transactionDetails?.amountInfo?.amountUSD?.roundToDecimalFormat() ?: 0.0 //TODO implement

        val createdAt = it.transaction.createdAt?.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", timeFormat = "hh:mm", useSpecificDays = false)
        val deviceId = viewModel.getDeviceId(context = LocalContext.current)
        val address = if (it.isOutgoingTransaction(LocalContext.current, deviceId)) {
            transactionDetails?.destinationAddress
        } else {
            transactionDetails?.sourceAddress
        }
        val feeAmount = transactionDetails?.networkFee?.roundToDecimalFormat() ?: "0"
        val txHash = transactionDetails?.txHash ?: ""
        val status = it.transaction.status

        var mainModifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_default))
        val showProgress = userFlow is UiState.Loading
        if (showProgress) {
            mainModifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default))
                .alpha(floatResource(R.dimen.progress_alpha))
                .clickable(
                    indication = null, // disable ripple effect
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { }
                )
        }

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
                Row(modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_default)),
                    verticalAlignment = Alignment.CenterVertically) {
                    AssetView(
                        modifier = Modifier.weight(1f),
                        supportedAsset,
                        context,
                        amount.toString(),
                        amountUSD.toString(),
                        assetAmountTextStyle = FireblocksNCWDemoTheme.typography.b1
                    )
                    status?.name?.let { statusName ->
                        StatusLabel(
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                            message = statusName.capitalizeFirstLetter(),
                            color = getStatusColor(status),
                        )
                    }
                }
                Divider(
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large)),
                    color = grey_2,
                )
                Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_default))) {
                    TitleContentView(titleResId = R.string.creation_date, contentText = createdAt, topPadding = null)

                    val stringResId = if (it.isOutgoingTransaction(LocalContext.current, deviceId)) {
                        R.string.sent_to
                    } else {
                        R.string.received_from
                    }
                    TitleContentView(titleResId = stringResId, contentText = address)
                    TitleContentView(titleResId = R.string.fee, contentText = stringResource(id = R.string.asset_amount, feeAmount, feeCurrency)) //TODO implement
                    if (txHash.isNotNullAndNotEmpty()) {
                        TitleContentView(titleResId = R.string.transaction_hash,
                            contentText = txHash,
                            contentDrawableRes = R.drawable.ic_copy,
                            onContentButtonClick = { copyToClipboard(context, txHash) })
                    }
                    TitleContentView(titleResId = R.string.fireblocks_id,
                        contentText = txId,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, txId) })
                }
            }
            if (userFlow is UiState.Error) {
                ErrorView(message = stringResource(id = R.string.deny_error))
            }
            if (status == SigningStatus.PENDING_SIGNATURE){
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                    ColoredButton(
                        modifier = Modifier.weight(1f),
                        labelResourceId = R.string.deny,
                        onClick = {
                            viewModel.deny(context, txId ?: "")
                        },
                    )
                    ColoredButton(
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
            if (uiState.transactionSignature != null) {
                onGoBack()
            }
        }
    }
}

@Preview
@Composable
fun TransferScreenPreview() {
    MultiDeviceManager.initialize(LocalContext.current)
    val transactionWrapper = TransactionWrapper(deviceId = "1",
        transaction = TransactionResponse(
            status = SigningStatus.PENDING_SIGNATURE,
            createdAt = 1690110129000,
            details = TransactionDetails(
                id = "2cf8022a-8bd4-494e-9c72-f72ce5895fc9dadad ",
                assetType = "BTC",
                assetId = "BTC",
                amountInfo = AmountInfo(
                amount = "3.0",
                amountUSD = "3000.0"),
                txHash = "0xf67f7b7afbbc12ace8d1e540a7ac15e2df5b323536213ac3a536341b83ef973d",
                destinationAddress = "0x324387ynckc83y48fhlc883mf",
                feeCurrency = "BTC"
            )
        )
    )
    FireblocksNCWDemoTheme {
        Surface {
            TransferScreen(transactionWrapper)
        }
    }
}