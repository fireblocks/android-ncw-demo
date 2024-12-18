package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel


/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 * Select if you want to recover an existing wallet or return to login screen and join an existing one.
 */
@Composable
fun ExistingAccountScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onRecoverClicked: () -> Unit = {},
    onJoinWalletScreen: () -> Unit = {},
    onCloseClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.ExistingAccount,
                onCloseClicked = onCloseClicked
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
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                Image(
                    painter = painterResource(R.drawable.ic_add_device_screen),
                    contentDescription = null,
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                    text = stringResource(id = R.string.existing_account_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                )
                ColoredButton(
                    modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.existing_account_screen_top_padding)),
                    labelResourceId = R.string.join_wallet,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = {
                        viewModel.initFireblocksSdkForJoinWalletFlow(context = context)
                        onJoinWalletScreen()
                    }
                )
                Row(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small_2)),
                    verticalAlignment = Alignment.CenterVertically) {
                    Divider(
                        color = grey_2,
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    FireblocksText(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_default)),
                        text = stringResource(id = R.string.or),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Start,
                    )
                    Divider(
                        color = grey_2,
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
                ColoredButton(
                    modifier = Modifier.fillMaxWidth(),
                    labelResourceId = R.string.recover_wallet_button,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = onRecoverClicked
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
            ExistingAccountScreen()
        }
    }
}
