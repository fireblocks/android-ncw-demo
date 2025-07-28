package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.supportSmallDevice
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.GenerateKeysViewModel
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun GenerateKeysScreen(
    viewModel: GenerateKeysViewModel = viewModel(),
    onSettingsClicked: () -> Unit,
    onSuccessScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current
    viewModel.observeDialogListener(LocalLifecycleOwner.current, context)

    LaunchedEffect(key1 = uiState.generatedKeys ) {
        if (uiState.generatedKeys){
            onSuccessScreen()
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Timber.d("Screen Height: $screenHeight")
    val smallDevice = screenHeight < 700.dp
    val imageHeight = screenHeight * 0.3f
    val scrollState = rememberScrollState()

    val modifier: Modifier = Modifier
    var mainModifier = modifier.supportSmallDevice(smallDevice, scrollState)

    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    var menuClickListener = onSettingsClicked
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier.supportSmallDevice(smallDevice, scrollState)
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = modifier
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
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.generate_keys_illustration),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = imageHeight)
                        .aspectRatio(1f) // Adjust the aspect ratio as needed
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    text = stringResource(id = R.string.generate_keys_top_bar_title),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                    text = stringResource(id = R.string.generate_keys_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_extra_large_2)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (BuildConfig.FLAVOR_server == "dev") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default))
                        ) {
                            DefaultButton(
                                modifier = Modifier.weight(1f),
                                labelResourceId = R.string.generate_ecdsa,
                                onClick = {
                                    viewModel.generateKeys(context = context, setOf(Algorithm.MPC_ECDSA_SECP256K1))
                                }
                            )
                            DefaultButton(
                                modifier = Modifier.weight(1f),
                                labelResourceId = R.string.generate_eddsa,
                                onClick = {
                                    viewModel.generateKeys(context = context, setOf(Algorithm.MPC_EDDSA_ED25519))
                                }
                            )
                        }
                    }
                    DefaultButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.generate_keys,
                        onClick = {
                            viewModel.generateKeys(context = context, setOf(Algorithm.MPC_ECDSA_SECP256K1, Algorithm.MPC_EDDSA_ED25519))
                        }
                    )
                }
                if (userFlow is UiState.Error) {
                    if (smallDevice) {
                        LaunchedEffect(scrollState) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                    ErrorView(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),//.align(Alignment.BottomEnd),
                        errorState = (userFlow as UiState.Error), defaultResId = R.string.generate_keys_error)
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

@Preview(
    name = "Small Device",
    device = "spec:width=350dp,height=640dp,dpi=320"
)
@Composable
fun GenerateKeysScreenPreviewSmall() {
    FireblocksNCWDemoTheme {
        Surface {
            GenerateKeysScreen(
                onSettingsClicked = {}
            ) {}
        }
    }
}