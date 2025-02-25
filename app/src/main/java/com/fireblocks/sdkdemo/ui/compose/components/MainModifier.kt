package com.fireblocks.sdkdemo.ui.compose.components

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource

@Composable
fun Modifier.supportSmallDevice(smallDevice: Boolean = false, scrollState: ScrollState): Modifier {
    var modifier = this.fillMaxWidth()
        .padding(horizontal = dimensionResource(R.dimen.padding_large))
    if (smallDevice) {
        modifier = modifier.verticalScroll(scrollState)
    }
    return modifier
}

@SuppressLint("ComposableModifierFactory")
@Composable
fun Modifier.createMainModifier(showProgress: Boolean, paddingTop: Int? = null, smallDevice: Boolean = false, scrollState: ScrollState? = null): Modifier {
    val padding = dimensionResource(R.dimen.padding_default)
    val alpha = floatResource(R.dimen.progress_alpha)
    var mainModifier = this
        .fillMaxSize()

    if (showProgress) {
        mainModifier = this
            .fillMaxSize()
            .alpha(alpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }
    paddingTop?.let {
        mainModifier = mainModifier.padding(top = dimensionResource(paddingTop), start = padding, end = padding, bottom = padding)
    } ?: run {
        mainModifier = mainModifier.padding(padding)
    }
    if (smallDevice && scrollState != null) {
        mainModifier = mainModifier.verticalScroll(scrollState)
    }
    return mainModifier
}