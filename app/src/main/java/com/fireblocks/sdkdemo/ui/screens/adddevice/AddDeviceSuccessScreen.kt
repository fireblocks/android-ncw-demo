package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun AddDeviceSuccessScreen(
    modifier: Modifier = Modifier,
    onHomeClicked: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.AddDeviceSuccess,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_default)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_add_device_success),
                        contentDescription = null,
                        modifier = Modifier.width(300.dp)
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.add_device_success),
                        textStyle = FireblocksNCWDemoTheme.typography.h3
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_default)),
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
fun AddDeviceSuccessScreenPreview() {
    FireblocksNCWDemoTheme {
        AddDeviceSuccessScreen(
            onHomeClicked = {}
        )
    }
}