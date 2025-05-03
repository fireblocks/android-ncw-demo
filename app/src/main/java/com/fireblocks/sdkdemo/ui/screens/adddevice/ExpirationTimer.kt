package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTime
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 31/12/2023.
 */
@Composable
fun ExpirationTimer(viewModel: AddDeviceViewModel, onExpired: () -> Unit = {}) {
    var timeLeft by remember { mutableIntStateOf(180) }

    LaunchedEffect(key1 = timeLeft) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }
    val timeLeftInMillis = TimeUnit.SECONDS.toMillis(timeLeft.toLong())

    FireblocksText(
        modifier = Modifier
            .padding(top = dimensionResource(id = R.dimen.padding_large), bottom = dimensionResource(R.dimen.padding_extra_large)),
        text = stringResource(id = R.string.code_expires_in, timeLeftInMillis.toFormattedTime()),
        textStyle = FireblocksNCWDemoTheme.typography.b3,
        textColor = text_secondary,
    )
    if (timeLeft == 0) {
        viewModel.updateErrorType(AddDeviceViewModel.AddDeviceErrorType.TIMEOUT)
        onExpired()
    }
}