package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.WarningView
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.compose.components.createToolbarModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.TakeoverViewModel

/**
 * Created by Fireblocks Ltd. on 10/08/2023.
 */
@Composable
fun ExportPrivateKeyScreen(
    viewModel: TakeoverViewModel = viewModel(),
    onBackClicked: () -> Unit = {},
    onTakeoverSuccess: (takeoverResult: Set<FullKey>) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.takeoverResult) {
        if (uiState.takeoverResult.isNotEmpty()) {
            onTakeoverSuccess(uiState.takeoverResult)
            viewModel.clean()
        }
    }

    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress, paddingTop = R.dimen.padding_extra_large_1)
    var topBarModifier = Modifier.createToolbarModifier(showProgress)

    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.ExportPrivateKey,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = mainModifier) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Image(
                        painter = painterResource(R.drawable.export_private_keys),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    FireblocksText(
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_default), horizontal = dimensionResource(R.dimen.padding_extra_large)),
                        text = stringResource(id = R.string.export_private_key_description),
                        textStyle = FireblocksNCWDemoTheme.typography.h4,
                        textColor = text_secondary,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (userFlow is UiState.Error) {
                        ErrorView(errorState = userFlow as UiState.Error, defaultResId = R.string.takeover_error)
                    }
                    WarningView(modifier = Modifier
                        .padding(top = dimensionResource(id = R.dimen.padding_default)),
                        message = stringResource(id = R.string.takeover_warning_content))
                    DefaultButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.export_keys,
                        onClick = {
                            viewModel.takeover(context)
                        }
                    )
                }
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }

}

@Preview
@Composable
fun ExportPrivateKeyScreenPreview() {
    FireblocksNCWDemoTheme {
        ExportPrivateKeyScreen()
    }
}