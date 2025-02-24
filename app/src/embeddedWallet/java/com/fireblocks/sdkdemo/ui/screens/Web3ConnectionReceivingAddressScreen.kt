package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ReceivingAddressView
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun Web3ConnectionReceivingAddressScreen(
    viewModel: Web3ViewModel,
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    LaunchedEffect(key1 = uiState.web3ConnectionCreatedResponse) {
        val response = uiState.web3ConnectionCreatedResponse
        if (response != null) {
            onNextScreen()
        }
    }

    ReceivingAddressView(
        modifier = mainModifier,
        userFlow = userFlow,
        showProgress = showProgress,
        onContinueClicked = { address ->
            onCreateWeb3ConnectionClicked(
                uri = address,
                viewModel = viewModel
            )
        }
    )
}

private fun onCreateWeb3ConnectionClicked(
    uri: String,
    feeLevel: Web3ConnectionFeeLevel? = null,
    viewModel: Web3ViewModel
) {
    viewModel.createWeb3Connection(feeLevel = feeLevel ?: Web3ConnectionFeeLevel.MEDIUM, uri = uri)
}

@Preview
@Composable
fun Web3ConnectionReceivingAddressScreenPreview() {
    val viewModel = Web3ViewModel()

    FireblocksNCWDemoTheme {
        Surface(color = background) {
            Web3ConnectionReceivingAddressScreen(viewModel = viewModel)
        }
    }
}

