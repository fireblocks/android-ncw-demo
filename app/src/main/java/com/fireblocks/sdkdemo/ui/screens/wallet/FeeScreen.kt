package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks Ltd. on 19/07/2023.
 */
@Composable
fun FeeScreen(
    viewModel: WalletViewModel,
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    uiState.selectedAsset?.let { asset ->
        val symbol = asset.symbol
        val context = LocalContext.current

        FeeContentView(viewModel = viewModel, uiState = uiState, symbol = symbol, onNextScreen = onNextScreen)

        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    viewModel.getEstimatedFee(context)
                }

                else -> {}
            }
        }
    }
}

@Composable
fun FeeContentView (viewModel: WalletViewModel, uiState: WalletUiState, symbol: String? = null, onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    if (showProgress) {
        ProgressBar()
    }

    LaunchedEffect(key1 = uiState.createdTransaction) {
        if (uiState.createdTransaction) {
            onNextScreen()
        }
    }
    val continueEnabledState = remember { mutableStateOf(true) }
    Column(
        modifier = mainModifier,
    ) {
        val feeItems = arrayListOf<FeeItem>()
        val selectedIndex = remember { mutableIntStateOf(0) }

        Column(modifier = Modifier.weight(1f)) {
            Card(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = grey_1),
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item))
            ) {
                FireblocksText(
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_default), top = dimensionResource(R.dimen.padding_extra_large)),
                    text = stringResource(id = R.string.transaction_speed),
                    textStyle = FireblocksNCWDemoTheme.typography.b2
                )

                val feeData = uiState.estimatedFee
                feeItems.add(FeeItem(R.string.low, feeData?.low ?: FeeData(), symbol))
                feeItems.add(FeeItem(R.string.medium, feeData?.medium ?: FeeData(), symbol))
                feeItems.add(FeeItem(R.string.high, feeData?.high ?: FeeData(), symbol))
                FeeList(selectedIndex, feeItems)
            }
        }

        if (userFlow is UiState.Error) {
            ErrorView(
                errorState = userFlow as UiState.Error,
                defaultResId = R.string.create_transaction_error
            )
            continueEnabledState.value = true
        }
        if (uiState.showFeeError) {
            ErrorView(message = stringResource(id = R.string.get_estimation_fee_error))
            continueEnabledState.value = true
        }

        ContinueButton(
            continueEnabledState,
            labelResourceId = R.string.continue_button,
            onClick = {
                continueEnabledState.value = false
                val selectedFee = feeItems[selectedIndex.intValue].feeData
                viewModel.onSelectedFee(selectedFee)
                viewModel.createTransaction(context)
            })
    }
}

@Composable
fun FeeList(
    selectedIndex: MutableIntState,
    feeItems: ArrayList<FeeItem>,
) {
    LazyColumn(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
    ) {
        feeItems.forEachIndexed { index, feeItem ->
            val fee = if (feeItem.symbol.isNullOrEmpty()) {
                null
            } else {
                feeItem.feeData.networkFee ?: "0"
            }
            item {
                FeeListItem(
                    feeItem.labelResId,
                    fee,
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
    val symbol: String? = null,
)

@Composable
private fun FeeListItem(
    @StringRes labelResId: Int,
    fee: String? = null,
    symbol: String? = null,
    selected: Boolean = false,
    clickAction: () -> Unit = {}
) {

    Surface(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = { clickAction() }),
        color = if (selected) grey_1 else transparent,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_corners_default)),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (selected) grey_2 else transparent),
            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_small))
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        vertical = dimensionResource(id = R.dimen.padding_large),
                        horizontal = dimensionResource(id = R.dimen.padding_default)
                    )
            ) {
                FireblocksText(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = labelResId),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = if (selected) white else text_secondary
                )
                fee?.let {
                    FireblocksText(
                        text = stringResource(id = R.string.proximate_fee, fee.roundToDecimalFormat(), symbol ?:""),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textColor = if (selected) white else text_secondary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FeeScreenPreview() {
    FireblocksNCWDemoTheme {
        val viewModel = WalletViewModel()
        val asset = SupportedAsset(
            id = "ETH",
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
            FeeData("0.0002", feeLevel = FeeLevel.HIGH)
        )
        viewModel.onSelectedAsset(asset)
        viewModel.onEstimatedFee(fee)
        Surface {
            FeeScreen(
                viewModel = viewModel
            )
        }
    }
}