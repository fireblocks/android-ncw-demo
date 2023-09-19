package com.fireblocks.sdkdemo.bl.core.extensions

import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */

const val DEFAULT_PATTERN = "#.##"
const val EXTENDED_PATTERN = "#.######"

fun Double.roundToDecimalFormat(pattern: String = DEFAULT_PATTERN): String {
    val df = DecimalFormat(pattern)
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}


