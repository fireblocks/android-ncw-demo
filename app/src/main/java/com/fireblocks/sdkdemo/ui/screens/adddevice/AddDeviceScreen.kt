package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ReceivingAddressGenericView
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 24/12/2023.
 */
@Composable
fun AddDeviceScreen(
    viewModel: AddDeviceViewModel,
    onBackClicked: () -> Unit = {},
    onNextScreen: () -> Unit = {},
) {
    val userFlow by viewModel.userFlow.collectAsState()

    val modifier: Modifier = Modifier
    var mainModifier = modifier
        .fillMaxSize()
        .padding(start = dimensionResource(R.dimen.padding_default), end = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(start = dimensionResource(R.dimen.padding_default), end = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_default))
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = Modifier
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    val onContinueClicked: (joinRequestDataEncoded: String) -> Unit = {
        runCatching {
            val joinRequestData = JoinRequestData.decode(it)
            if (joinRequestData == null || joinRequestData.requestId.isNullOrEmpty()) {
                Timber.e("Missing request id")
                viewModel.showError(resId = R.string.missing_request_id_error)
            } else {
                viewModel.updateJoinRequestData(joinRequestData)
                onNextScreen()
            }
        }.onFailure {
            viewModel.showError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.AddDevice,
                navigateUp = {
                    viewModel.clean()
                    onBackClicked()
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier
            ) {
                val addressTextState = remember { mutableStateOf("") }
                val continueEnabledState = remember { mutableStateOf(false) }
                continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

                ReceivingAddressGenericView(
                    userFlow = userFlow,
                    onContinueClicked = { address ->
                        onContinueClick(onContinueClicked, address)
                    },
                    scanTitleResId = R.string.add_device_scan_the_qr_code,
                    hint = R.string.add_device_enter_address_hint,
                )
            }
        }
    }
}

private fun onContinueClick(onNextScreen: (address: String) -> Unit,
                            address: String) {
    onNextScreen(address)
}

@Preview
@Composable
fun AddDeviceScreenPreview() {

    FireblocksNCWDemoTheme {
        Surface {
            AddDeviceScreen(AddDeviceViewModel())
        }
    }
}