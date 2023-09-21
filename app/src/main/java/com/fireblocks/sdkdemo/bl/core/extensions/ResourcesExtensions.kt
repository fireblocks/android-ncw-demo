package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

/**
 * Created by Fireblocks Ltd. on 19/09/2023.
 */

fun Resources.getFloatValue(@DimenRes id: Int): Float {
    val typedValue = TypedValue()
    getValue(id, typedValue, true)
    return typedValue.float
}

@Composable
@ReadOnlyComposable
fun floatResource(@DimenRes id: Int): Float {
    val context = LocalContext.current
    return context.resources.getFloatValue(id)
}