package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.grey_1

/**
 * Created by Fireblocks Ltd. on 16/07/2023.
 */
@Composable
internal fun StartupTopAppBar(
    modifier: Modifier = Modifier,
    currentScreen: FireblocksScreen,
    onMenuActionClicked: ((MenuItem) -> Unit)? = null
) {
    var showDropDown by remember { mutableStateOf(false) }
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
            if (onMenuActionClicked != null) {
                IconButton(onClick = { showDropDown = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Show menu"
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(grey_1),
                    expanded = showDropDown,
                    onDismissRequest = { showDropDown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.share_logs)) },
                        leadingIcon = { Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Share logs") },
                        onClick = {
                            onMenuActionClicked(MenuItem.SHARE_LOGS)
                            showDropDown = false }
                    )
                    if (BuildConfig.FLAVOR_wallet == "ncw") {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.delete_wallet)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete and create new wallet")
                            },
                            onClick = {
                                onMenuActionClicked(MenuItem.REGENERATE_WALLET)
                                showDropDown = false
                            }
                        )
                    }
                }
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

enum class MenuItem {
    SHARE_LOGS,
    REGENERATE_WALLET
}

@Composable
@Preview
fun SplashTopAppBarPreview() {
    FireblocksNCWDemoTheme {
        StartupTopAppBar(currentScreen = FireblocksScreen.SplashScreen)
    }
}