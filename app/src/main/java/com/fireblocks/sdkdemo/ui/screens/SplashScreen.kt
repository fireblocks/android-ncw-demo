package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.StartupTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel

/**
 * Created by Fireblocks Ltd. on 16/12/2024.
 */
@Composable
fun SplashScreen(onNextScreen: () -> Unit = {}) {
    val modifier = Modifier.fillMaxSize()
    val mainModifier = modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_default))
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
                    modifier = Modifier.padding(
                        top = dimensionResource(R.dimen.padding_extra_large_1),
                        bottom = dimensionResource(R.dimen.padding_extra_large_2)),
                    onNextScreen = onNextScreen)
                Image(
                    contentScale = ContentScale.Inside,
                    painter = painterResource(R.drawable.splash_illustration),
                    contentDescription = null,
                )
            }
            VersionAndEnvironmentLabel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_default)))
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
            modifier = Modifier.clickable { onNextScreen() },
            painter = painterResource(R.drawable.go_button),
            contentDescription = null,
            contentScale = ContentScale.None
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