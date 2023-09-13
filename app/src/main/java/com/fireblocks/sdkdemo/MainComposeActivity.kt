package com.fireblocks.sdkdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksApp

class MainComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireblocksNCWDemoTheme {
                FireblocksApp()
            }
        }
    }
}

@Preview
@Composable
fun FireblocksAppPreview() {
    FireblocksNCWDemoTheme {
        FireblocksApp()
    }
}