package com.fireblocks.sdkdemo.bl.core.extensions

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */

const val DEFAULT_PATTERN = "#.##"
const val EXTENDED_PATTERN = "#.######"

fun Double.roundToDecimalFormat(pattern: String = DEFAULT_PATTERN): String {
    val symbols = DecimalFormatSymbols(Locale.ENGLISH)
    val df = DecimalFormat(pattern, symbols)
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}

