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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.SplashTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
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
            SplashTopAppBar(
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
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large)),
                    text = stringResource(id = R.string.splash_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                Image(
                    modifier = Modifier
                        .padding(top = dimensionResource(R.dimen.padding_extra_large))
                        .clickable {
                            MultiDeviceManager.instance.setSplashScreenSeen()
                            onNextScreen()
                        },
                    painter = painterResource(R.drawable.splash_go_button),
                    contentDescription = null,
                )
            }
            VersionAndEnvironmentLabel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = dimensionResource(id = R.dimen.login_screen_build_top_padding)),
                ncwVersion = viewModel.getNCWVersion())
        }
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