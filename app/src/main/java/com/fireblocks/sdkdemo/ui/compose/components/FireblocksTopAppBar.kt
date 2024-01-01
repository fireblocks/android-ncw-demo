package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.screens.TopBarMenuActionType

/**
 * Created by Fireblocks Ltd. on 03/10/2023.
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
                TopBarMenuActionType.Close -> CloseButton(onCloseClicked = onMenuActionClicked)
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