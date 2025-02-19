package com.fireblocks.sdkdemo.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.QRScannerActivity
import com.fireblocks.sdkdemo.ui.compose.components.AddressTextField
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun Web3ConnectionCreateScreen(
    viewModel: Web3ViewModel,
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val focusManager = LocalFocusManager.current

    var mainModifier = Modifier
        .fillMaxSize()
        .padding(dimensionResource(R.dimen.padding_default))
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focusManager.clearFocus() }

    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        mainModifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default))
            .alpha(floatResource(R.dimen.progress_alpha))
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    LaunchedEffect(key1 = uiState.web3ConnectionCreatedResponse) {
        val response = uiState.web3ConnectionCreatedResponse
        if (response != null) {
            onNextScreen()
        }
    }

    Column(
        modifier = mainModifier
    ) {
        val context = LocalContext.current
        val addressTextState = remember { mutableStateOf("") }
        val continueEnabledState = remember { mutableStateOf(false) }
        continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

        Column(modifier = Modifier.weight(1f)) {
            FireblocksText(
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.padding_large),
                    start = dimensionResource(id = R.dimen.padding_small)
                ),
                text = stringResource(id = R.string.address),
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
                        onCreateWeb3ConnectionClicked(addressTextState, viewModel = viewModel)
                    }
                )

                val scannedQRCode = remember { mutableStateOf("") }
                if (scannedQRCode.value.isNotEmpty()) {
                    addressTextState.value = scannedQRCode.value
                }

                val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
                    if (result.contents == null) {
                        val originalIntent: Intent? = result.originalIntent
                        if (originalIntent == null) {
                            Timber.d("Cancelled scan")
                        } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                            Timber.w("Cancelled scan due to missing camera permission")
                            Toast.makeText(
                                context,
                                "Cancelled due to missing camera permission",
                                Toast.LENGTH_LONG
                            ).show()
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

                Image(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                        .clickable {
                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                startScannerActivity(scannerLauncher)
                            } else {
                                // Request a permission
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    painter = painterResource(id = R.drawable.ic_scan_qr),
                    contentDescription = ""
                )
            }

        }
        if (userFlow is UiState.Error) {
            ErrorView(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),//.align(Alignment.BottomEnd),
                errorState = userFlow as UiState.Error, defaultResId = R.string.recover_wallet_error)
        }
        if (showProgress) {
            ProgressBar()
        }
        ContinueButton(continueEnabledState,
            onClick = {
                onCreateWeb3ConnectionClicked(addressTextState, viewModel = viewModel)
            })
    }
}

private fun startScannerActivity(scannerLauncher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
    scannerLauncher.launch(
        ScanOptions()
            .setOrientationLocked(true)
            .setPrompt("QR Scan")
            .setCaptureActivity(QRScannerActivity::class.java)//addExtra Intents.Scan.SHOW_MISSING_CAMERA_PERMISSION_DIALOG
    )
}

private fun onCreateWeb3ConnectionClicked(
    addressTextState: MutableState<String>,
    feeLevel: Web3ConnectionFeeLevel? = null,
    viewModel: Web3ViewModel
) {
    val uri = addressTextState.value
    viewModel.createWeb3Connection(feeLevel = feeLevel ?: Web3ConnectionFeeLevel.MEDIUM, uri = uri)
}

@Preview
@Composable
fun Web3ConnectionCreateScreenPreview() {
    val viewModel = Web3ViewModel()

    FireblocksNCWDemoTheme {
        Surface {
            Web3ConnectionCreateScreen(viewModel = viewModel)
        }
    }
}

