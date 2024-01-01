package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.viewmodel.GenerateKeysViewModel


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun GenerateKeysScreen(
    modifier: Modifier = Modifier,
    viewModel: GenerateKeysViewModel = viewModel(),
    onSettingsClicked: () -> Unit,
    onSuccessScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current
    viewModel.observeDialogListener(LocalLifecycleOwner.current)

    LaunchedEffect(key1 = uiState.generatedKeys ) {
        if (uiState.generatedKeys){
            onSuccessScreen()
        }
    }

    var mainModifier = modifier.fillMaxSize()
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    var menuClickListener = onSettingsClicked
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
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
        menuClickListener = {}
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            FireblocksTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.GenerateKeys,
                canNavigateBack = false,
                navigateUp = {},
                onMenuActionClicked = menuClickListener
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_extra_large)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_generate_keys),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.generate_keys_description),
                        textStyle = FireblocksNCWDemoTheme.typography.b1
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (userFlow is UiState.Error) {
                        ErrorView(message = stringResource(id = R.string.generate_keys_error))
                    }
                    ColoredButton(
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.generate_keys,
                        onClick = {
                            viewModel.generateKeys(context = context, setOf(Algorithm.MPC_ECDSA_SECP256K1))
                        }
                    )
                }
            }
            if (showProgress) {
                ProgressBar(R.string.progress_message)
            }
        }
    }
}

@Preview
@Composable
fun GenerateKeysScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            GenerateKeysScreen(
                onSettingsClicked = {}
            ) {}
        }
    }
}
