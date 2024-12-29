package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.StartupTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
import com.fireblocks.sdkdemo.ui.theme.grey_1_semi_transparent
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel

/**
 * Created by Fireblocks Ltd. on 16/12/2024.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier,
                 viewModel: LoginViewModel = viewModel(),
                 onNextScreen: () -> Unit = {}) {
    val mainModifier = modifier
        .fillMaxSize()
        .padding(start = dimensionResource(R.dimen.padding_default), end = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_default))
    val topBarModifier: Modifier = Modifier

    Scaffold(
        modifier = modifier,
        topBar = {
            StartupTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.SplashScreen,
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
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.screen_top_padding)),
                    text = stringResource(id = R.string.splash_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                LetsGoButton(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    onNextScreen = onNextScreen)
            }
            VersionAndEnvironmentLabel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                ncwVersion = viewModel.getNCWVersion())
        }
    }
}

@Composable
fun LetsGoButton(modifier: Modifier = Modifier, onNextScreen: () -> Unit) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.go_button),
            contentDescription = null,
            contentScale = ContentScale.None
        )
        DefaultButton(
            onClick = onNextScreen,
            labelText = stringResource(id = R.string.lets_go),
            colors = ButtonDefaults.buttonColors(containerColor = grey_1_semi_transparent),
            modifier = Modifier
                .width(168.dp)
                .padding(horizontal = dimensionResource(R.dimen.padding_small_2)),
            innerVerticalPadding = R.dimen.padding_small_2,
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            SplashScreen()
        }
    }
}