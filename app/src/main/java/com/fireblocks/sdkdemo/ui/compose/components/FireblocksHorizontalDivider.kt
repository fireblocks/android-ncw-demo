package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.ui.theme.divider_1

@Composable
fun FireblocksHorizontalDivider(){
    HorizontalDivider(color = divider_1, thickness = 1.dp)
}