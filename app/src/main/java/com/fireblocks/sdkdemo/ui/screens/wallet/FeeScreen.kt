package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.light_blue
import com.fireblocks.sdkdemo.ui.theme.light_blue_1
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks ltd. on 19/07/2023.
 */
@Composable
fun FeeScreen(
    uiState: WalletViewModel.WalletUiState,
    viewModel: WalletViewModel,
    onNextScreen: () -> Unit = {},
) {

    uiState.selectedAsset?.let { asset ->
        val userFlow by viewModel.userFlow.collectAsState()
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

        if (showProgress) {
            ProgressBar()
        }

        uiState.estimatedFee?.let { feeData ->

            Column(
                modifier = mainModifier,
            ) {
                val selectedIndex = remember { mutableIntStateOf(1) }
                val feeItems = arrayListOf<FeeItem>()
                feeItems.add(FeeItem(R.string.slow, feeData.low ?: FeeData(), asset.symbol))
                feeItems.add(FeeItem(R.string.medium, feeData.medium?: FeeData(), asset.symbol))
                feeItems.add(FeeItem(R.string.fast, feeData.high?: FeeData(), asset.symbol))

                Column(modifier = Modifier.weight(1f)) {
                    Row() {
                        FireblocksText(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = R.string.speed),
                            textStyle = FireblocksNCWDemoTheme.typography.b1
                        )
                        FireblocksText(
                            text = stringResource(id = R.string.fee_top_bar_title),
                            textStyle = FireblocksNCWDemoTheme.typography.b1
                        )
                    }

                    FeeList(selectedIndex, feeItems)
                }
                if (userFlow is UiState.Error) {
                    ErrorView(message = stringResource(id = R.string.create_transaction_error))
                }
                if (uiState.showFeeError) {
                    ErrorView(message = stringResource(id = R.string.get_estimation_fee_error))
                }
                val continueEnabledState = remember { mutableStateOf(true) }
                ContinueButton(
                    continueEnabledState,
                    labelResourceId = R.string.create_transaction,
                    onClick = {
                        val selectedFee = feeItems[selectedIndex.intValue].feeData
                        viewModel.onSelectedFee(selectedFee)
                        viewModel.createTransaction(context)
                    })
            }
            LaunchedEffect(key1 = uiState.createdTransaction) {
                if (uiState.createdTransaction) {
                    onNextScreen()
                }
            }
        }

        OnLifecycleEvent { owner, event ->
            when (event){
                Lifecycle.Event.ON_CREATE -> { viewModel.getEstimatedFee(context) }
                else -> {}
            }
        }
    }
}

@Composable
private fun FeeList(
    selectedIndex: MutableIntState,
    feeItems: ArrayList<FeeItem>,
) {
    LazyColumn(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        feeItems.forEachIndexed { index, feeItem ->
            item {
                FeeListItem(
                    feeItem.labelResId,
                    feeItem.feeData.networkFee?: "0",
                    feeItem.symbol,
                    selected = selectedIndex.intValue == index,
                    clickAction = { selectedIndex.intValue = index })
            }
        }
    }
}

data class FeeItem(
    val labelResId: Int,
    val feeData: FeeData,
    val symbol: String,
)

@Composable
private fun FeeListItem(
    @StringRes labelResId: Int,
    fee: String,
    symbol: String,
    selected: Boolean = false,
    clickAction: () -> Unit = {}
) {

    Surface(
        modifier = Modifier.clickable(true, onClick = { clickAction() }),
        color = if (selected) light_blue_1 else transparent,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_default)),
    ) {
        Row(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_large), horizontal = dimensionResource(id = R.dimen.padding_default))
        ) {
            FireblocksText(
                modifier = Modifier.weight(1f),
                text = stringResource(id = labelResId),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = if (selected) light_blue else white
            )
            FireblocksText(
                text = stringResource(id = R.string.proximate_fee, fee.roundToDecimalFormat(), symbol),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = if (selected) light_blue else white
            )
        }
    }
}

@Preview
@Composable
fun FeeScreenPreview() {
    FireblocksNCWDemoTheme {
        val viewModel = WalletViewModel()
        val asset = SupportedAsset(id = "ETH",
            symbol = "ETH",
            name = "Ethereum",
            type = "BASE_ASSET",
            blockchain = "Ethereum",
            balance = "132.4",
            price = "2,825.04",
            )
        val fee = Fee(
            FeeData("0.00008", feeLevel = FeeLevel.LOW),
            FeeData("0.0001", feeLevel = FeeLevel.MEDIUM),
            FeeData("0.0002", feeLevel = FeeLevel.HIGH))
        val uiState = WalletViewModel.WalletUiState(selectedAsset = asset, estimatedFee = fee)
        Surface {
            FeeScreen(
                uiState = uiState,
                viewModel = viewModel)
        }
    }
}