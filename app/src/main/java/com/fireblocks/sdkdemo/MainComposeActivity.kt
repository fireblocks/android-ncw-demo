package com.fireblocks.sdkdemo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.FireblocksApp

/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 */
class MainComposeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireblocksNCWDemoTheme {
                FireblocksApp()
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
fun FireblocksAppPreview() {
    FireblocksNCWDemoTheme {
        FireblocksApp()
    }
}