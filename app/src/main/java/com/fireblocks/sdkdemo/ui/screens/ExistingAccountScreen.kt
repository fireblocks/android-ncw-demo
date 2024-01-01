package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.theme.grey_1


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Select if you want to recover an existing wallet or return to login screen and join an existing one.
 */
@Composable
fun ExistingAccountScreen(
    modifier: Modifier = Modifier,
    onSettingsClicked: () -> Unit = {},
    onRecoverClicked: () -> Unit = {},
    onCloseClicked: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            FireblocksTopAppBar(
                currentScreen = FireblocksScreen.ExistingAccount,
                canNavigateBack = false,
                navigateUp = {},
                onMenuActionClicked = onSettingsClicked
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.recover_existing_wallet_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                )
                ColoredButton(
                    modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_large)),
                    labelResourceId = R.string.recover_existing_wallet,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = onRecoverClicked
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.return_to_login_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                )
                ColoredButton(
                    modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_large)),
                    labelResourceId = R.string.return_to_login,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = onCloseClicked
                )
            }
        }
    }
}

@Preview
@Composable
fun ExistingAccountScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            ExistingAccountScreen(
            )
        }
    }
}
