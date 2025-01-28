package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Select if you want to recover an existing wallet or return to login screen and join an existing one.
 */
@Composable
fun ExistingAccountScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onRecoverClicked: () -> Unit = {},
    onJoinWalletScreen: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    var mainModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = dimensionResource(R.dimen.padding_large))
    var topBarModifier: Modifier = Modifier
    var backClickedCallback = onBackClicked
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_large))
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
        backClickedCallback = {}
    }

    LaunchedEffect(key1 = uiState.passedJoinWallet) {
        if (uiState.passedJoinWallet) {
            viewModel.onPassedInitForJoinWallet(false)
            onJoinWalletScreen()
        }
    }

    LaunchedEffect(key1 = uiState.passedInitForRecover) {
        if (uiState.passedInitForRecover) {
            viewModel.onPassedInitForRecover(false)
            onRecoverClicked()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.ExistingAccount,
                navigateUp = backClickedCallback
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.existing_account_illustration),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.weight(1f)
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    text = stringResource(id = R.string.existing_account_screen_title),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                    text = stringResource(id = R.string.existing_account_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
                ColoredButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.screen_top_padding)),
                    labelResourceId = R.string.join_wallet,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = {
                        viewModel.initFireblocksSdkForJoinWalletFlow(context = context)
                    }
                )
                FireblocksText(
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_default), vertical = dimensionResource(id = R.dimen.padding_default)),
                    text = stringResource(id = R.string.or),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_secondary
                )
                ColoredButton(
                    modifier = Modifier.fillMaxWidth(),
                    labelResourceId = R.string.recover_wallet_button,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = {
                        viewModel.initFireblocksSdkForRecoveryFlow(context = context)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            if (userFlow is UiState.Error) {
                ErrorView(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)).align(Alignment.BottomEnd),
                    errorState = userFlow as UiState.Error,
                    defaultResId = R.string.try_again)
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }
}

@Preview
@Composable
fun ExistingAccountScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            ExistingAccountScreen()
        }
    }
}

@Preview(
    name = "Small Device",
    device = "spec:width=350dp,height=640dp,dpi=320"
)
@Composable
fun ExistingAccountScreenPreviewSmallDevice() {
    FireblocksNCWDemoTheme {
        Surface {
            ExistingAccountScreen()
        }
    }
}
