package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun JoinWalletScreen(
    modifier: Modifier = Modifier,
    viewModel: AddDeviceViewModel = viewModel(),
    onCloseClicked: () -> Unit = {},
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.joinRequestData) {
        if (uiState.joinRequestData != null && uiState.joinRequestData?.requestId.isNotNullAndNotEmpty()) {
            onNextScreen()
        }
    }

    var mainModifier = modifier
        .fillMaxWidth()
        .padding(
            start = dimensionResource(R.dimen.padding_large),
            end = dimensionResource(R.dimen.padding_large),
            bottom = dimensionResource(R.dimen.screen_bottom_padding))
    var topBarModifier: Modifier = Modifier
    var closeClickedCallback = {
        viewModel.clean()
        viewModel.stopJoinWallet(context)
        onCloseClicked()
    }
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(
                start = dimensionResource(R.dimen.padding_large),
                end = dimensionResource(R.dimen.padding_large),
                bottom = dimensionResource(R.dimen.screen_bottom_padding))
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
        closeClickedCallback = {}
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.JoinWallet,
                onCloseClicked = closeClickedCallback
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val imageHeight = screenHeight * 0.3f

                Image(
                    painter = painterResource(R.drawable.add_device_p),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = imageHeight)
                        .aspectRatio(1f) // Adjust the aspect ratio as needed
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    text = stringResource(id = R.string.add_new_device),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                    text = stringResource(id = R.string.join_wallet_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
                ContinueButton(viewModel, userFlow)
                FireblocksText(
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_default), horizontal = dimensionResource(R.dimen.padding_large)),
                    text = stringResource(id = R.string.join_wallet_screen_note),
                    textStyle = FireblocksNCWDemoTheme.typography.b3,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
            }
            if (userFlow is UiState.Error) {
                ErrorView(
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_default)),
                    errorState = userFlow as UiState.Error, defaultResId = R.string.join_wallet_generate_qr_error)
            }
            if (showProgress) {
                ProgressBar(R.string.adding_device_progress_message)
            }
        }
    }
}

@Composable
private fun ContinueButton(viewModel: AddDeviceViewModel, userFlow: UiState) {
    val context = LocalContext.current

    var labelResourceId = R.string.continue_button
    if (userFlow is UiState.Error) {
        labelResourceId = R.string.try_again
    }

    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.screen_top_padding)),
        labelResourceId = labelResourceId,
        onClick = {
            viewModel.joinExistingWallet(context)
        }
    )
}

@Preview
@Composable
fun JoinWalletScreenPreview() {
    FireblocksNCWDemoTheme {
        JoinWalletScreen()
    }
}