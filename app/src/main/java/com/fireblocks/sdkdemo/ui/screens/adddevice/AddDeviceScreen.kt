package com.fireblocks.sdkdemo.ui.screens.adddevice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.core.content.ContextCompat
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.QRScannerActivity
import com.fireblocks.sdkdemo.ui.compose.components.AddressTextField
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.disabled
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 24/12/2023.
 */
@Composable
fun AddDeviceScreen(
    viewModel: AddDeviceViewModel,
    onBackClicked: () -> Unit = {},
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    val modifier: Modifier = Modifier
    var mainModifier = modifier
        .fillMaxSize()
        .padding(start = dimensionResource(R.dimen.padding_default), end = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(start = dimensionResource(R.dimen.padding_default), end = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_default))
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

    val onContinueClicked: (joinRequestDataEncoded: String) -> Unit = {
        runCatching {
            val joinRequestData = JoinRequestData.decode(it)
            if (joinRequestData == null || joinRequestData.requestId.isNullOrEmpty()) {
                Timber.e("Missing request id")
                viewModel.showError(resId = R.string.missing_request_id_error)
            } else {
                viewModel.updateJoinRequestData(joinRequestData)
                onNextScreen()
            }
        }.onFailure {
            viewModel.showError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.AddDevice,
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
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large), start = dimensionResource(id = R.dimen.padding_small)),
                    text = stringResource(id = R.string.add_device_scan_qr_code),
                    textStyle = FireblocksNCWDemoTheme.typography.h4,
                    textAlign = TextAlign.Center
                )

                OpenScannerButton(onContinueClicked, viewModel)

                FireblocksText(
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.or),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = disabled
                )

                val enterQRManuallyState = remember { mutableStateOf(false) }
                if (!enterQRManuallyState.value) {
                    TransparentButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.enter_qr_code_link,
                        onClick = {
                            enterQRManuallyState.value = true
                        }
                    )
                    if (userFlow is UiState.Error) {
                        ErrorView(errorState = userFlow as UiState.Error)
                    }
                } else {
                    val addressTextState = remember { mutableStateOf("") }
                    val continueEnabledState = remember { mutableStateOf(false) }
                    continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

                    Column(modifier = Modifier.weight(1f)) {
                        FireblocksText(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(id = R.string.enter_qr_code_link),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Start
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AddressTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                                readOnly = false,
                                text = addressTextState,
                                onKeyboardDoneClick = {
                                    focusManager.clearFocus()
                                    onContinueClick(onContinueClicked, addressTextState)
                                }
                            )
                        }

                    }
                    if (userFlow is UiState.Error) {
                        (userFlow as UiState.Error).getErrorMessage(LocalContext.current)?.let {
                            ErrorView(message = it)
                        }
                    }
                    ContinueButton(continueEnabledState,
                        onClick = {
                            onContinueClick(onContinueClicked, addressTextState)
                        })
                }
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }
}

@Composable
fun OpenScannerButton(onContinueClicked: (joinRequestDataJson: String) -> Unit, viewModel: AddDeviceViewModel) {
    val context: Context = LocalContext.current
    val scannedQRCode = remember { mutableStateOf("") }
    val scannedValue = scannedQRCode.value
    if (scannedValue.isNotEmpty()) {
        scannedQRCode.value = ""
        onContinueClicked(scannedValue)
    }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            val originalIntent: Intent? = result.originalIntent
            if (originalIntent == null) {
                Timber.d("Cancelled scan")
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Timber.w("Cancelled scan due to missing camera permission")
                Toast.makeText(context, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show()
            }
        } else {
            Timber.d("Scanned")
            scannedQRCode.value = result.contents
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            startScannerActivity(scannerLauncher)
        }
    }

    DefaultButton(
        modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(id = R.dimen.padding_extra_large)),
        labelResourceId = R.string.scan_qr_code,
        imageResourceId = R.drawable.ic_scan_qr_white,
        onClick = {
            viewModel.updateUserFlow(UiState.Idle)
            val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                startScannerActivity(scannerLauncher)
            } else {
                // Request a permission
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    )
}

private fun startScannerActivity(scannerLauncher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
    scannerLauncher.launch(
        ScanOptions()
            .setOrientationLocked(true)
            .setPrompt("QR Scan")
            .setCaptureActivity(QRScannerActivity::class.java)//addExtra Intents.Scan.SHOW_MISSING_CAMERA_PERMISSION_DIALOG
    )
}

private fun onContinueClick(onNextScreen: (address: String) -> Unit,
                            addressTextState: MutableState<String>) {
    onNextScreen(addressTextState.value)
}

@Preview
@Composable
fun AddDeviceScreenPreview() {

    FireblocksNCWDemoTheme {
        Surface {
            AddDeviceScreen(AddDeviceViewModel())
        }
    }
}