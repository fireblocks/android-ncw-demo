package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CloseButton
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.SettingsButton
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.viewmodel.GenerateKeysViewModel


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun FireblocksTopAppBar(
    currentScreen: FireblocksScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuActionClicked: () -> Unit,
    menuActionType: TopBarMenuActionType = TopBarMenuActionType.Settings
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            FireblocksText(
                modifier = Modifier.fillMaxWidth(),
                text = currentScreen.title?.let { stringResource(it) },
                textStyle = FireblocksNCWDemoTheme.typography.h3,
                textAlign = TextAlign.Center,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.Transparent,
        ),
        actions = {
            when (menuActionType) {
                TopBarMenuActionType.Settings -> SettingsButton(onMenuActionClicked)
                TopBarMenuActionType.Close -> CloseButton(onMenuActionClicked)
            }

        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
    )
}

@Composable
fun GenerateKeysScreen(
    modifier: Modifier = Modifier,
    viewModel: GenerateKeysViewModel = viewModel(),
    onSettingsClicked: () -> Unit,
    onRecoverClicked: () -> Unit,
    onHomeScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current
    viewModel.observeDialogListener(LocalLifecycleOwner.current)

    LaunchedEffect(key1 = uiState.generatedKeys ) {
        if (uiState.generatedKeys){
            onHomeScreen()
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
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                    Image(
                        painter = painterResource(R.drawable.ic_generate_keys),
                        contentDescription = null,
                        modifier = Modifier.width(300.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.generate_keys,
                        onClick = {
                            viewModel.generateKeys(context = context, setOf(Algorithm.MPC_ECDSA_SECP256K1))
                        }
                    )
                    TransparentButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.recover_existing_wallet,
                        onClick = onRecoverClicked)
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
        GenerateKeysScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
            onSettingsClicked = {},
            onRecoverClicked = {},
            onHomeScreen = {}
        )
    }
}
