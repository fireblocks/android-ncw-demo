package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary

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
        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
        title = {
            FireblocksText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(R.dimen.padding_small_2)),
                text = currentScreen.title?.let { stringResource(it) },
                textStyle = FireblocksNCWDemoTheme.typography.h4,
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
                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp)))
                {
                    DropdownMenu(
                        modifier = Modifier.background(color = grey_1).padding(end = dimensionResource(R.dimen.padding_extra_large_1)),
                        expanded = showDropDown,
                        onDismissRequest = { showDropDown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                FireblocksText(
                                    text = stringResource(id = R.string.share_logs),
                                    textColor = text_secondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_list),
                                    contentDescription = "Share logs")
                            },
                            onClick = {
                                onMenuActionClicked(MenuItem.SHARE_LOGS)
                                showDropDown = false
                            }
                        )
                        if (BuildConfig.FLAVOR_wallet == "ncw") {
                            DropdownMenuItem(
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                                text = {
                                    FireblocksText(
                                        text = stringResource(id = R.string.delete_wallet),
                                        textColor = text_secondary)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_trash),
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
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(21.dp)
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