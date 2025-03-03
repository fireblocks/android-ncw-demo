package com.fireblocks.sdkdemo.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.QRScannerActivity
import com.fireblocks.sdkdemo.ui.compose.components.AddressTextField
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
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
fun Web3ConnectionReceivingAddressScreen(
    viewModel: Web3ViewModel,
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Timber.d("Screen Height: $screenHeight")
    val smallDevice = screenHeight < 700.dp
    val imageHeight = screenHeight * 0.3f
    val scrollState = rememberScrollState()

    val mainModifier = Modifier.createMainModifier(showProgress, smallDevice = smallDevice, scrollState = scrollState)

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

        Image(
            painter = painterResource(R.drawable.qr_illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_default))
                .fillMaxWidth()
                .heightIn(max = imageHeight)
                .aspectRatio(1f) // Adjust the aspect ratio as needed
        )

        Column(modifier = Modifier.weight(1f)) {
            val focusManager = LocalFocusManager.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item)),
                        color = grey_1
                    )
                    .padding(dimensionResource(id = R.dimen.padding_default)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                Card(
                    modifier = Modifier,
                    colors = CardDefaults.cardColors(containerColor = grey_2),
                    shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_small))
                ) {
                    Image( //TODO make all the card clickable
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

                Column(modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.padding_default),
                ),) {

                    FireblocksText(
                        text = stringResource(id = R.string.scan_dapp_qr_code),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Start
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
                        text = stringResource(id = R.string.scan_dapp_qr_code_subtitle),
                        textStyle = FireblocksNCWDemoTheme.typography.b4,
                        textAlign = TextAlign.Start,
                        textColor = text_secondary
                    )
                }
            }
            FireblocksText(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large)),
                text = stringResource(id = R.string.paste_dapp_address),
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textAlign = TextAlign.Start
            )
            Row(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                verticalAlignment = Alignment.CenterVertically) {
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    readOnly = false,
                    text = addressTextState,
                    onKeyboardDoneClick = {
                        focusManager.clearFocus()
                        onCreateWeb3ConnectionClicked(uri = addressTextState.value, viewModel = viewModel)
                    }
                )



            }
        }
        if (userFlow is UiState.Error) {
            ErrorView(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),
                errorState = userFlow as UiState.Error, defaultResId = R.string.try_again)
        }
        if (showProgress) {
            ProgressBar()
        }
        FireblocksText(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large)).align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.please_enter_address),
            textStyle = FireblocksNCWDemoTheme.typography.b4,
        )
        ContinueButton(continueEnabledState,
            onClick = {
                onCreateWeb3ConnectionClicked(uri = addressTextState.value, viewModel = viewModel)
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
    uri: String,
    feeLevel: Web3ConnectionFeeLevel? = null,
    viewModel: Web3ViewModel
) {
    viewModel.createWeb3Connection(feeLevel = feeLevel ?: Web3ConnectionFeeLevel.MEDIUM, uri = uri)
}

@Preview
@Composable
fun Web3ConnectionReceivingAddressScreenPreview() {
    val viewModel = Web3ViewModel()

    FireblocksNCWDemoTheme {
        Surface(color = background) {
            Web3ConnectionReceivingAddressScreen(viewModel = viewModel)
        }
    }
}

