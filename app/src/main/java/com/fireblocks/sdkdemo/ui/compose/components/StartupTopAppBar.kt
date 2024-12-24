package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.screens.TopBarMenuActionType

/**
 * Created by Fireblocks Ltd. on 16/07/2023.
 */
@Composable
internal fun StartupTopAppBar(
    modifier: Modifier = Modifier,
    currentScreen: FireblocksScreen,
    onMenuActionClicked: (() -> Unit)? = null,
    menuActionType: TopBarMenuActionType? = null
) {
    TopAppBar(
        title = {
            FireblocksText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(R.dimen.padding_small_1)),
                text = currentScreen.title?.let { stringResource(it) },
                textStyle = FireblocksNCWDemoTheme.typography.h3,
                textAlign = TextAlign.Start,
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            if (onMenuActionClicked != null && menuActionType == TopBarMenuActionType.Settings) {
                SettingsButton(onMenuActionClicked)
            }
        },
        modifier = modifier,
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(21.dp)
            )
        }
    )
}

@Composable
@Preview
fun SplashTopAppBarPreview() {
    FireblocksNCWDemoTheme {
        StartupTopAppBar(currentScreen = FireblocksScreen.SplashScreen)
    }
}