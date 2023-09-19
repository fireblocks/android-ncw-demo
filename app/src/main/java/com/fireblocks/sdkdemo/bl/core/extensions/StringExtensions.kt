package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

/**
 * Created by Fireblocks Ltd. on 28/03/2023.
 */
fun String.copyToClipboard(context : Context, label : String = "copied_text") {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, this)
    clipboardManager.setPrimaryClip(clipData)
}

fun String?.isNotNullAndNotEmpty(): Boolean {
    return !isNullOrEmpty()
}

fun String.capitalizeFirstLetter(): String {
    if(this.isEmpty()) {
        return this
    }
    return this.capitalizeFirstChar()!!
}

fun String?.capitalizeFirstChar(): String? {
    if (this.isNullOrBlank()) {
        return this
    }
    return this.lowercase(Locale.getDefault()).replaceFirstChar(Char::titlecase)
}

fun String.roundToDecimalFormat(pattern: String = EXTENDED_PATTERN): String {
    return this.toDouble().roundToDecimalFormat(pattern)
}