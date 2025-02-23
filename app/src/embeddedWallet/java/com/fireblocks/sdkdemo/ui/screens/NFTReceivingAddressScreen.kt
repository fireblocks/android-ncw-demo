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
import androidx.compose.runtime.MutableState
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
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.QRScannerActivity
import com.fireblocks.sdkdemo.ui.compose.components.AddressTextField
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun NFTReceivingAddressScreen(
    viewModel: NFTsViewModel,
    onNextScreen: (address: String) -> Unit = {},
) {
    val selectedNFT = viewModel.getSelectedNFT()
    selectedNFT?.let { nft ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
        ) {
            val context = LocalContext.current
            val addressTextState = remember { mutableStateOf("") }
            val continueEnabledState = remember { mutableStateOf(false) }
            continueEnabledState.value = addressTextState.value.trim().isNotEmpty()

            Column(modifier = Modifier.weight(1f)) {
                NFTListItem(nft = nft)
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
                            onContinueClick(onNextScreen, addressTextState)
                        }
                    )

                    val scannedQRCode = remember { mutableStateOf("") }
                    if (scannedQRCode.value.isNotEmpty()) {
                        addressTextState.value = scannedQRCode.value
                    }

                    val scannerLauncher =
                        rememberLauncherForActivityResult(ScanContract()) { result ->
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
            ContinueButton(continueEnabledState,
                onClick = {
                    onContinueClick(onNextScreen, addressTextState)
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


private fun onContinueClick(
    onNextScreen: (address: String) -> Unit,
    addressTextState: MutableState<String>
) {
    onNextScreen(addressTextState.value)
}

@Preview
@Composable
fun NFTReceivingAddressScreenPreview() {
    val viewModel = NFTsViewModel()
    viewModel.onNFTSelected(
        TokenOwnershipResponse(
            id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91",
            tokenId = "1",
            standard = "ERC1155",
            blockchainDescriptor = BlockchainDescriptor.XTZ_TEST,
            description = "This is the NFT description",
            name = "sword",
            metadataURI = "ipfs://bafybeidstfcbqv2v2ursvchraw64hu2p4e3rx37dpoqzeguvketcfetlji/1",
            cachedMetadataURI = "https://stage-static.fireblocks.io/dev9/nft/24d1bea228cbcb5010db9a91376c65ea/metadata.json",
            media = listOf(
                MediaEntityResponse(
                    url = "https://stage-static.fireblocks.io/dev9/nft/media/aXBmczovL2JhZnliZWloamNuYXFrd3lucG9kaW5taW54dXdiZ3VucWNxYnNlMmxwb2kzazJibnIyempneXhyaHV1LzE",
                    contentType = MediaEntityResponse.ContentType.IMAGE
                )
            ),
            collection = TokenCollectionResponse(
                id = "0xe0e2C83BdE2893f93012b9FE7cc0bfC2893b344B",
                name = "my collection",
                symbol = "A"
            ),
            spam = SpamOwnershipResponse(
                true,
                source = SpamOwnershipResponse.SpamOwnershipResponseSource.SYSTEM
            ),
            balance = "1",
            ownershipStartTime = 123,
            ownershipLastUpdateTime = 123,
            status = TokensStatus.LISTED,
            vaultAccountId = "1",
            ncwId = "1",
            ncwAccountId = "1",
        )
    )

    FireblocksNCWDemoTheme {
        Surface {
            NFTReceivingAddressScreen(viewModel = viewModel)
        }
    }
}

