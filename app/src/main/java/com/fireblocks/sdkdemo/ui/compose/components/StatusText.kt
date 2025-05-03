package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fireblocks.sdkdemo.bl.core.extensions.beautifySigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.wallet.getStatusColor

@Composable
fun StatusText(modifier: Modifier = Modifier, status: SigningStatus) {
    FireblocksText(
        modifier = modifier,
        text = status.name.beautifySigningStatus(),
        textStyle = FireblocksNCWDemoTheme.typography.b3,
        textColor = getStatusColor(status),
    )
}