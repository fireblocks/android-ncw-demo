package com.fireblocks.sdkdemo.ui.screens.wallet

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.screens.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.screens.TopBarMenuActionType
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView

/**
 * Created by Fireblocks Ltd. on 27/09/2023.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QRScannerScreen(onClose: (barCodeOrQr: String?) -> Unit = {},) {
    Scaffold(
        topBar = {
            FireblocksTopAppBar(
                modifier = Modifier,
                currentScreen = FireblocksScreen.QRScannerScreen,
                canNavigateBack = false,
                navigateUp = {},
                onMenuActionClicked = {onClose(null)},
                menuActionType = TopBarMenuActionType.Close
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            val context = LocalContext.current
            var scanFlag by remember {
                mutableStateOf(false)
            }

            val compoundBarcodeView = remember {
                CompoundBarcodeView(context).apply {
                    if (context is Activity) {
                        val capture = CaptureManager(context, this)
                        capture.initializeFromIntent(context.intent, null)
                        this.setStatusText("")
                        capture.decode()
                        this.resume()
                        this.decodeContinuous { result ->
                            if (scanFlag) {
                                return@decodeContinuous
                            }
                            scanFlag = true
                            result.text?.let { barCodeOrQr ->
                                //put scanFlag = false to scan another item
                                scanFlag = false
                                onClose(barCodeOrQr)
                            }
                            //If you don't put this scanFlag = false, it will never work again.
                            //you can put a delay over 2 seconds and then scanFlag = false to prevent multiple scanning

                        }
                    }
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { compoundBarcodeView },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
                    .align(Alignment.Center)
            ) {
                Image(
                    modifier = Modifier.align(Alignment.TopStart),
                    painter = painterResource(id = R.drawable.ic_rectangle_14),
                    contentDescription = "image description",
                    contentScale = ContentScale.None
                )
                Image(
                    modifier = Modifier.align(Alignment.TopEnd),
                    painter = painterResource(id = R.drawable.ic_rectangle_15),
                    contentDescription = "image description",
                    contentScale = ContentScale.None
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.qr_scan_rect_height)))

                Image(
                    modifier = Modifier.align(Alignment.BottomStart),
                    painter = painterResource(id = R.drawable.ic_rectangle_13),
                    contentDescription = "image description",
                    contentScale = ContentScale.None
                )
                Image(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    painter = painterResource(id = R.drawable.ic_rectangle_12),
                    contentDescription = "image description",
                    contentScale = ContentScale.None
                )
            }
        }
    }
}

@Preview
@Composable
fun QRScannerScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            QRScannerScreen()
        }
    }
}