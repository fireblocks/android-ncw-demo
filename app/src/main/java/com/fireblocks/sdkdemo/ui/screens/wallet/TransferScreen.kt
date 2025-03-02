package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.StatusText
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
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
    val justApproved = transactionWrapper?.justApproved ?: false
    val selectedTransactionWrapper = transactions.find { it.id == txId }

    selectedTransactionWrapper?.let {
        val userFlow by viewModel.userFlow.collectAsState()
        val feeCurrency = it.feeCurrency ?: ""
        val id = it.assetName
        val symbol = it.blockchainSymbol
        val assetId = it.assetId

        val amount = it.amount?.roundToDecimalFormat() ?: 0.0
        val amountUSD = it.amountUSD?.roundToDecimalFormat() ?: 0.0 //TODO implement

        val supportedAsset = SupportedAsset(
            id = it.assetId ?: "",
            type = feeCurrency)

        val createdAt = it.createdAt?.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", timeFormat = "hh:mm", useSpecificDays = false)
        val deviceId = viewModel.getDeviceId(context = LocalContext.current)
        val address = if (it.isOutgoingTransaction(LocalContext.current, deviceId)) {
            it.destinationAddress
        } else {
            it.sourceAddress
        }
        val feeAmount = it.networkFee?.roundToDecimalFormat() ?: "0"
        val txHash = it.txHash ?: ""
        val status = it.getStatus()

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
                Row(modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_default)),
                    verticalAlignment = Alignment.CenterVertically) {
                    AssetView(
                        modifier = Modifier.weight(1f),
                        context = context,
                        supportedAsset = supportedAsset,
                        id = id,
                        symbol = symbol,
                        assetAmount = amount.toString(),
                        assetUsdAmount = amountUSD.toString(),
                        assetAmountTextStyle = FireblocksNCWDemoTheme.typography.b1
                    )
                    StatusText(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),status)
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
                    TitleContentView(
                        titleResId = stringResId,
                        contentText = address,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, address) })
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
                    TitleContentView(titleResId = R.string.asset_id,
                        contentText = assetId,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, assetId) })

                }
            }
            if (userFlow is UiState.Error) {
                ErrorView(errorState = userFlow as UiState.Error, defaultResId = R.string.deny_error) //TODO fix error in case of approve failure
            }
            if (status == SigningStatus.PENDING_SIGNATURE && !justApproved) {
                Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
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