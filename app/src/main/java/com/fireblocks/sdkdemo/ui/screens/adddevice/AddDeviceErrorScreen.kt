package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fireblocks.sdkdemo.ui.compose.components.BulletText
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun AddDeviceErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: AddDeviceViewModel = viewModel(),
    onCloseAddDevice: () -> Unit = {},
    onCloseJoinWallet: () -> Unit = {},
    onBackToAddDevice: () -> Unit = {},
    onBackToJoinWallet: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var title = stringResource(id = R.string.add_device_error)
    var subtitle = ""
    when(uiState.errorType) {
        AddDeviceViewModel.AddDeviceErrorType.CANCELED -> {
            subtitle = stringResource(id = R.string.process_was_canceled_error)
        }
        AddDeviceViewModel.AddDeviceErrorType.FAILED -> {
            subtitle = stringResource(id = R.string.process_has_failed_error)
        }
        AddDeviceViewModel.AddDeviceErrorType.QR_GENERATION_FAILED -> {
            title = stringResource(id = R.string.generate_qr_error)
        }
        else -> {}
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.AddDeviceError,
                onCloseClicked = {
                    viewModel.clean()
                    viewModel.stopJoinWallet(context)
                    when(uiState.approveAddDeviceFlow){
                        true -> onCloseAddDevice()
                        false -> onCloseJoinWallet()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_default)),
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
                        painter = painterResource(R.drawable.ic_error_screen),
                        contentDescription = null,
                        modifier = Modifier.width(300.dp)
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = title,
                        textStyle = FireblocksNCWDemoTheme.typography.h3
                    )
                    subtitle.takeIf { it.isNotEmpty() }?.let {
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_small)),
                            text = it,
                            textStyle = FireblocksNCWDemoTheme.typography.h3,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (uiState.errorType == AddDeviceViewModel.AddDeviceErrorType.TIMEOUT) {
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_large), bottom = dimensionResource(id = R.dimen.padding_default)),
                            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                            colors = CardDefaults.cardColors(containerColor = grey_1),
                        ) {
                            FireblocksText(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = dimensionResource(R.dimen.padding_large)),
                                text = stringResource(id = R.string.add_device_timeout_error_description_title),
                                textStyle = FireblocksNCWDemoTheme.typography.h4,
                                textAlign = TextAlign.Center,
                            )
                            Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_default), vertical = dimensionResource(R.dimen.padding_large))) {
                                BulletText(
                                    modifier = Modifier,
                                    text = stringResource(id = R.string.add_device_timeout_error_description_1_title),
                                    textStyle = FireblocksNCWDemoTheme.typography.h4)
                                FireblocksText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = dimensionResource(R.dimen.padding_default)),
                                    text = stringResource(id = R.string.add_device_timeout_error_description_1_subtitle),
                                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                                )
                                BulletText(
                                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                                    text = stringResource(id = R.string.add_device_timeout_error_description_2_title),
                                    textStyle = FireblocksNCWDemoTheme.typography.h4)
                                FireblocksText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = dimensionResource(R.dimen.padding_default)),
                                    text = stringResource(id = R.string.add_device_timeout_error_description_2_subtitle),
                                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                                )
                            }
                        }
                    }
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
                        labelResourceId = R.string.try_again,
                        onClick = {
                            viewModel.clean()
                            when(uiState.approveAddDeviceFlow){
                                true -> onBackToAddDevice()
                                false -> onBackToJoinWallet()
                            }
                        }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun AddDeviceErrorScreenPreview() {
    val viewModel = AddDeviceViewModel()
    viewModel.updateErrorType(AddDeviceViewModel.AddDeviceErrorType.FAILED)
    FireblocksNCWDemoTheme {
        AddDeviceErrorScreen(viewModel = viewModel)
    }
}