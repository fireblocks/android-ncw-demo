package com.fireblocks.sdkdemo.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ReceivingAddressGenericView
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun Web3ConnectionReceivingAddressScreen(
    viewModel: Web3ViewModel,
    onNextScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Timber.d("Screen Height: $screenHeight")
    val smallDevice = screenHeight < 700.dp
    val scrollState = rememberScrollState()

    val mainModifier = Modifier.createMainModifier(showProgress, smallDevice = smallDevice, scrollState = scrollState)

    LaunchedEffect(key1 = uiState.web3ConnectionCreatedResponse) {
        val response = uiState.web3ConnectionCreatedResponse
        if (response != null) {
            onNextScreen()
        }
    }

    Column(
        modifier = mainModifier
    ) {
        val addressTextState = remember { mutableStateOf("") }
        val continueEnabledState = remember { mutableStateOf(false) }
        continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

        ReceivingAddressGenericView(
            userFlow = userFlow,
            onContinueClicked = { address ->
                onCreateWeb3ConnectionClicked(context = context, uri = address, viewModel = viewModel)
            },
            scanTitleResId = R.string.scan_the_qr_code,
            scanSubtitleResId = R.string.scan_dapp_qr_code_subtitle,
            hint = R.string.dapp_enter_address_hint,
        )
    }
}

private fun onCreateWeb3ConnectionClicked(
    context: Context,
    uri: String,
    feeLevel: Web3ConnectionFeeLevel? = null,
    viewModel: Web3ViewModel
) {
    viewModel.createWeb3Connection(context = context ,feeLevel = feeLevel ?: Web3ConnectionFeeLevel.MEDIUM, uri = uri)
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

