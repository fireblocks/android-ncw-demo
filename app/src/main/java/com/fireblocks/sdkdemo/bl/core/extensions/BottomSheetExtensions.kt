package com.fireblocks.sdkdemo.bl.core.extensions

import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity

/**
 * Created by Fireblocks Ltd. on 26/09/2023.
 */

@Composable
fun rememberSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    initialValue: SheetValue = SheetValue.Hidden,
    skipHiddenState: Boolean = false
): SheetState {
    val density = LocalDensity.current
    return rememberSaveable(
        skipPartiallyExpanded, confirmValueChange,
        saver = SheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange,
            density = density,
            skipHiddenState = skipHiddenState
        )
    ) {
        SheetState(skipPartiallyExpanded = skipPartiallyExpanded, density = density, initialValue = initialValue, confirmValueChange = confirmValueChange, skipHiddenState = skipHiddenState)
    }
}