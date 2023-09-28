package com.fireblocks.sdkdemo.ui.compose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.screens.wallet.QRScannerScreen
import com.google.zxing.client.android.Intents

class QRScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FireblocksNCWDemoTheme {
                QRScannerScreen(onClose = { barCodeOrQr ->
                    if (barCodeOrQr.isNotNullAndNotEmpty()) {
                        intent.putExtra(Intents.Scan.RESULT, barCodeOrQr)
                        setResult(Activity.RESULT_OK, intent)
                    }
                    finish()
                })
            }
        }
        hideSystemUI()
    }

    private fun hideSystemUI() {
        //Hides the ugly action bar at the top
        actionBar?.hide()

        //Hide the status bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}


@Preview
@Composable
fun QRScannerActivityPreview() {
    FireblocksNCWDemoTheme {
        QRScannerScreen()
    }
}