package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.fireblocks.sdkdemo.ui.theme.text_secondary
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
                    .padding(
                        start = dimensionResource(R.dimen.padding_large),
                        end = dimensionResource(R.dimen.padding_large),
                        bottom = dimensionResource(R.dimen.screen_bottom_padding)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val imageHeight = screenHeight * 0.3f

                Image(
                    painter = painterResource(R.drawable.existing_account_image),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = imageHeight)
                        .aspectRatio(1f) // Adjust the aspect ratio as needed
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    text = stringResource(id = R.string.existing_account_screen_title),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                    text = stringResource(id = R.string.existing_account_screen_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
                ColoredButton(
                    modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.screen_top_padding)),
                    labelResourceId = R.string.join_wallet,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
                    onClick = {
                        viewModel.initFireblocksSdkForJoinWalletFlow(context = context)
                        onJoinWalletScreen()
                    }
                )
                FireblocksText(
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_default), vertical = dimensionResource(id = R.dimen.padding_large)),
                    text = stringResource(id = R.string.or),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_secondary
                )
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
