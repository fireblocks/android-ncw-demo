package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksHorizontalDivider
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.StatusText
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.compose.utils.AssetsUtils
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.blue
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.success
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.TransfersViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 * Updated to include pull-to-refresh functionality for better user experience
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransferHistoryScreen(
    viewModel: TransfersViewModel = viewModel(),
    walletViewModel: WalletViewModel,
    onNextScreen: (transactionWrapper: TransactionWrapper) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    // Initialize pull refresh state - using fetchTransactions for proper refresh functionality
    val refreshing = userFlow is UiState.Refreshing
    fun refresh() = viewModel.fetchTransactions(context)
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    viewModel.loadTransactions(context)

    // Wrap the entire content in a Box with pull refresh modifier
    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        Card(
            modifier = mainModifier,
            colors = CardDefaults.cardColors(containerColor = grey_1),
            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item))
        ) {
            LazyColumn {
                val itemsList = mutableListOf<@Composable () -> Unit>()

                val transactions = uiState.transactions
                val sortedItems = transactions.sortedByDescending { it.lastUpdated }
                sortedItems.forEach { transactionWrapper ->
                    itemsList.add {
                        transactionWrapper.assetId?.let { assetId ->
                            val asset = walletViewModel.getAsset(assetId)
                            asset?.let {
                                transactionWrapper.setAsset(asset)
                            }
                        }
                        TransactionListItem(
                            transactionWrapper = transactionWrapper,
                            deviceId = viewModel.getDeviceId(context = context)
                        ) {
                            onNextScreen(it)
                        }
                    }
                }
                itemsIndexed(itemsList) { index, item ->
                    item()
                    if (index < itemsList.size - 1) {
                        FireblocksHorizontalDivider()
                    }
                }
            }
        }

        // Add pull refresh indicator at the top center of the screen
        PullRefreshIndicator(
            refreshing,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter),
            contentColor = white,
            backgroundColor = transparent
        )
    }
}

@Composable
fun TransactionListItem(transactionWrapper: TransactionWrapper,
                        deviceId: String,
                        onClick: (TransactionWrapper) -> Unit = {}) {
    Box(Modifier
        .clickable { onClick.invoke(transactionWrapper) },) {
        Row(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
                .height(dimensionResource(R.dimen.details_list_item_height)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val assetSymbol = AssetsUtils.removeTestSuffix(transactionWrapper.assetName)
            val status = transactionWrapper.status
            val amount = transactionWrapper.amount?.roundToDecimalFormat() ?: "0.0"
            val balance = transactionWrapper.amountUSD?.roundToDecimalFormat() ?: "0"

            Column(modifier = Modifier.weight(1f)) {
                val stringResId =
                    if (transactionWrapper.isOutgoingTransaction(LocalContext.current, deviceId)) {
                        when(status) {
                            SigningStatus.PENDING_SIGNATURE -> R.string.send
                            else -> R.string.sent_top_bar_title
                        }
                    } else {
                        R.string.received_top_bar_title
                    }
                FireblocksText(
                    text = stringResource(id = stringResId, assetSymbol),
                )
                StatusText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                    status
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    FireblocksText(
                        text = amount,
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.End
                    )
                    FireblocksText(
                        text = stringResource(id = R.string.usd_balance, balance),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textColor = text_secondary,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionListItemPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            TransactionListItem(transactionWrapper = TransactionWrapper(deviceId = "1"), deviceId = "1") {
            }
        }
    }
}

fun getStatusColor(status: SigningStatus): Color {
    return when (status) {
        SigningStatus.SUBMITTED,
        SigningStatus.PENDING_AML_SCREENING,
        SigningStatus.BROADCASTING,
        SigningStatus.QUEUED,
        SigningStatus.PENDING_SIGNATURE,
        SigningStatus.PENDING_AUTHORIZATION,
        SigningStatus.PENDING_3RD_PARTY_MANUAL_APPROVAL,
        SigningStatus.PENDING_3RD_PARTY,
        SigningStatus.CONFIRMING,
        SigningStatus.PENDING_CONSOLE_APPROVAL -> blue

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
fun TransferListScreenPreview() {
    MultiDeviceManager.initialize(LocalContext.current)
    val viewModel = TransfersViewModel()
    val walletViewModel = WalletViewModel()
    val transactions = hashSetOf<TransactionWrapper>()
    val transactionWrapper = TransactionWrapper(deviceId = "1")
    transactions.add(transactionWrapper)
    viewModel.onTransactions(transactions)
    FireblocksNCWDemoTheme {
        Surface {
            TransferHistoryScreen(viewModel = viewModel, walletViewModel = walletViewModel) {
            }
        }
    }
}