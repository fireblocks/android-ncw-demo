package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.wallet.FeeContentView
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun NFTFeeScreen(
    viewModel: WalletViewModel,
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    FeeContentView(viewModel = viewModel, uiState = uiState, onNextScreen = onNextScreen)
}

@Preview
@Composable
fun NFTFeeScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            NFTFeeScreen(viewModel = WalletViewModel())
        }
    }
}

