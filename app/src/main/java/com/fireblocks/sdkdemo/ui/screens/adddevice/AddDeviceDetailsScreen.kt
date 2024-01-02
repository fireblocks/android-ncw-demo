package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.BulletText
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import com.google.gson.Gson


/**
 * Created by Fireblocks Ltd. on 24/12/2023.
 */
@Composable
fun AddDeviceDetailsScreen(
    viewModel: AddDeviceViewModel,
    onBackClicked: () -> Unit = {},
    onAddDeviceSuccess: () -> Unit = {},
    onAddDeviceCanceled: () -> Unit = {},
    onExpired: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.approveJoinWalletSuccess) {
        if (uiState.approveJoinWalletSuccess) {
            viewModel.onApproveJoinWalletSuccess(false)
            onAddDeviceSuccess()
        }
    }

    val focusManager = LocalFocusManager.current
    val modifier: Modifier = Modifier
    var mainModifier = modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_default))
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = Modifier
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.AddDeviceDetails,
                navigateUp = {
                    viewModel.clean()
                    onBackClicked()
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null) { focusManager.clearFocus() },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                Image(
                    painter = painterResource(R.drawable.ic_add_device_screen),
                    contentDescription = null,
                )
                val addressTextState = remember { mutableStateOf("") }
                val continueEnabledState = remember { mutableStateOf(false) }
                continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

                Column(modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,) {
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                        text = stringResource(id = R.string.add_this_device),
                        textStyle = FireblocksNCWDemoTheme.typography.h3,
                    )
                    uiState.joinRequestData?.let { joinRequestData ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(id = R.dimen.padding_extra_large))
                                .background(color = grey_1, shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_corners_default))),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                        ) {
                            FireblocksText(
                                modifier = Modifier
                                    .padding(top = dimensionResource(R.dimen.padding_default))
                                    .align(Alignment.CenterHorizontally),
                                text = stringResource(id = R.string.device_details),
                                textStyle = FireblocksNCWDemoTheme.typography.h4,
                                textAlign = TextAlign.Center
                            )
                            BulletText(
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small_2), start = dimensionResource(id = R.dimen.padding_small), end = dimensionResource(id = R.dimen.padding_small)),
                                text = stringResource(id = R.string.type, joinRequestData.platform.value))
                            BulletText(
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small_2), bottom = dimensionResource(id = R.dimen.padding_large),start = dimensionResource(id = R.dimen.padding_small), end = dimensionResource(id = R.dimen.padding_small)),
                                text = stringResource(id = R.string.user, joinRequestData.email))
                        }
                    }
                }
                if (userFlow is UiState.Error) {
                    ErrorView(
                        message = stringResource(id = uiState.errorResId),
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_default)))
                }
                ColoredButton(
                    modifier = Modifier.fillMaxWidth(),
                    labelResourceId = R.string.add_device,
                    onClick = {
                        viewModel.approveJoinWalletRequest(context)
                    }
                )
                TransparentButton(
                    modifier = Modifier.fillMaxWidth(),
                    labelResourceId = R.string.cancel,
                    onClick = {
                        viewModel.clean()
                        viewModel.stopJoinWallet(context)
                        onAddDeviceCanceled()
                    })

                ExpirationTimer(viewModel = viewModel, onExpired = onExpired)

            }
            if (showProgress) {
                ProgressBar(R.string.adding_device_progress_message)
            }
        }
    }
}

@Preview
@Composable
fun AddDeviceDetailsScreenPreview() {
    val viewModel = AddDeviceViewModel()
    val dataJson = """
        {
            "requestId": "123456",
            "platform": "ANDROID",
            "email": "xxx@gmail.com"
        }
    """.trimIndent()
    Gson().fromJson(dataJson, JoinRequestData::class.java)
        .let { viewModel.updateJoinRequestData(it) }

    FireblocksNCWDemoTheme {
        Surface {
            AddDeviceDetailsScreen(viewModel = viewModel)
        }
    }
}

