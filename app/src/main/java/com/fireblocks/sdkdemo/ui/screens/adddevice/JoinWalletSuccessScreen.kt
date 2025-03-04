package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun JoinWalletSuccessScreen(
    onHomeClicked: () -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.JoinWalletSuccess,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_success),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.add_device_success),
                        textStyle = FireblocksNCWDemoTheme.typography.h3
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    ColoredButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.go_home,
                        onClick = onHomeClicked
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun JoinWalletSuccessScreenPreview() {
    FireblocksNCWDemoTheme {
        JoinWalletSuccessScreen(
            onHomeClicked = {}
        )
    }
}