package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen

/**
 * Created by Fireblocks Ltd. on 16/07/2023.
 */
@Composable
internal fun BaseTopAppBar(
    modifier: Modifier = Modifier,
    currentScreen: FireblocksScreen,
    navigateUp: (() -> Unit)? = null,
    onCloseClicked: () -> Unit = {},
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
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            if (currentScreen.showCloseButton) {
                CloseButton(onCloseClicked = onCloseClicked)
            } else {
                TopBarEmptySideBox()
            }
        },
        navigationIcon = {
            navigateUp?.let {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            } ?: TopBarEmptySideBox()
        }
    )
}

@Composable
@Preview
fun BaseTopAppBarPreview() {
    FireblocksNCWDemoTheme {
        BaseTopAppBar(currentScreen = FireblocksScreen.ExportPrivateKey, navigateUp = {})
    }
}