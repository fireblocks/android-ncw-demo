package com.fireblocks.sdkdemo.ui.compose.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.QRScannerActivity
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber

@Composable
fun ReceivingAddressView(
    modifier: Modifier = Modifier,
    userFlow: UiState = UiState.Idle,
    showProgress: Boolean = false,
    onContinueClicked: (address: String) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        val context = LocalContext.current
        val addressTextState = remember { mutableStateOf("") }
        val continueEnabledState = remember { mutableStateOf(false) }
        continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

        Column(modifier = Modifier.weight(1f)) {
            val focusManager = LocalFocusManager.current
            Card(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = grey_1),
                shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_list_item))
            ) {
                Column(modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_default),
                    vertical = dimensionResource(id = R.dimen.padding_large)
                ),) {

                    FireblocksText(
                        text = stringResource(id = R.string.address),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Start
                    )
                    Row(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                        verticalAlignment = Alignment.CenterVertically) {
                        AddressTextField(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = dimensionResource(id = R.dimen.padding_small_2)),
                            readOnly = false,
                            text = addressTextState,
                            onKeyboardDoneClick = {
                                focusManager.clearFocus()
                                onContinueClicked(addressTextState.value)
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
                                onContinueClicked(scannedQRCode.value)
                            }
                        }

                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) {
                            if (it) {
                                startScannerActivity(scannerLauncher)
                            }
                        }
                        Card(
                            modifier = Modifier,
                            colors = CardDefaults.cardColors(containerColor = grey_2),
                            shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_small))
                        ) {
                            Image(
                                modifier = Modifier
                                    .padding(dimensionResource(id = R.dimen.padding_default))
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
                }
            }
        }
        if (userFlow is UiState.Error) {
            ErrorView(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),
                errorState = userFlow, defaultResId = R.string.try_again)
        }
        if (showProgress) {
            ProgressBar()
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FireblocksText(
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_default)),
                text = stringResource(id = R.string.add_receiving_address),
                textStyle = FireblocksNCWDemoTheme.typography.b4,
                textAlign = TextAlign.Center
            )
            ContinueButton(continueEnabledState,
                onClick = {
                    onContinueClicked(addressTextState.value)
                })
        }
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

@Preview
@Composable
fun ReceivingAddressViewPreview() {
    val uiState = UiState.Idle
    FireblocksNCWDemoTheme {
        ReceivingAddressView(
            userFlow = uiState,
            showProgress = false,
            onContinueClicked = {}
        )
    }
}