package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.models.AmountInfo
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionDetails
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.StatusLabel
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.purple
import com.fireblocks.sdkdemo.ui.theme.success
import com.fireblocks.sdkdemo.ui.theme.text_grey
import com.fireblocks.sdkdemo.ui.viewmodel.TransfersViewModel

/**
 * Created by Fireblocks ltd. on 18/07/2023.
 */
@Composable
fun TransfersScreen(
    viewModel: TransfersViewModel = viewModel(),
    onNextScreen: (transactionWrapper: TransactionWrapper) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()

    viewModel.loadTransactions()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default)),
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val transactions = uiState.transactions
            val sortedItems = transactions.sortedByDescending { it.transaction.lastUpdated }
            sortedItems.forEach {
                item {
                    TransactionListItem(
                        transactionWrapper = it,
                        onClick = {
                            onNextScreen(it)
                        })
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(modifier: Modifier = Modifier, transactionWrapper: TransactionWrapper, onClick: (TransactionWrapper) -> Unit = {}) {
    Row(
        modifier = modifier
            .clickable { onClick.invoke(transactionWrapper) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val assetName = transactionWrapper.transaction.details?.assetId ?: ""
        val blockchain = transactionWrapper.transaction.details?.feeCurrency ?: "" //TODO implement
        val status = transactionWrapper.transaction.status
        val amount = transactionWrapper.transaction.details?.amountInfo?.amount?.roundToDecimalFormat() ?: "0.0"
        val balance = transactionWrapper.transaction.details?.amountInfo?.amountUSD?.roundToDecimalFormat() ?: "" //TODO implement

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
                    val stringResId = if (transactionWrapper.isOutgoingTransaction(LocalContext.current, deviceId)) {
                         R.string.sent_top_bar_title
                    } else {
                        R.string.received_top_bar_title
                    }
                FireblocksText(
                    text = stringResource(id = stringResId, assetName),
                )
//                Label(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)), text = blockchain)
            }

            status?.name?.let {
                StatusLabel(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                    message = it.capitalizeFirstLetter(),
                    color = getStatusColor(status),
                )
            }

        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                FireblocksText(
                    text = amount.toString(),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.End
                )
                FireblocksText(
                    text = stringResource(id = R.string.usd_balance, balance),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_grey,
                    textAlign = TextAlign.End
                )
            }
            Image(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_default)),
                painter = painterResource(R.drawable.ic_next_arrow),
                contentDescription = null)
        }
    }
}

fun getStatusColor(status: SigningStatus): Color {
    return when (status) {
        SigningStatus.SUBMITTED,
        SigningStatus.BROADCASTING,
        SigningStatus.QUEUED,
        SigningStatus.PENDING_SIGNATURE,
        SigningStatus.PENDING_AUTHORIZATION,
        SigningStatus.PENDING_3RD_PARTY_MANUAL_APPROVAL,
        SigningStatus.PENDING_3RD_PARTY,
        SigningStatus.CONFIRMING,
        SigningStatus.PENDING_CONSOLE_APPROVAL -> purple

        SigningStatus.SIGNED_BY_CLIENT,
        SigningStatus.SIGNED,
        SigningStatus.COMPLETED -> success

        SigningStatus.REJECTED_BY_CLIENT,
        SigningStatus.CANCELLED,
        SigningStatus.BLOCKED,
        SigningStatus.FAILED -> error
    }
}

@Preview
@Composable
fun TransfersScreenPreview() {
    MultiDeviceManager.initialize(LocalContext.current)
    val viewModel = TransfersViewModel()
    val transactions = hashSetOf<TransactionWrapper>()
    val transactionWrapper = TransactionWrapper(deviceId = "1",
        transaction = TransactionResponse(
            status = SigningStatus.COMPLETED,
            details = TransactionDetails(assetType = "ETH", assetId = "Ethereum", amountInfo = AmountInfo(amount = "3.0", amountUSD = "3000.0"),  feeCurrency = "ETH_TEST")
        )
    )
    transactions.add(transactionWrapper
    )
    viewModel.onTransactions(transactions)
    FireblocksNCWDemoTheme {
        Surface {
            TransfersScreen(viewModel = viewModel) {
            }
        }
    }
}