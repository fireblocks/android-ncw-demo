package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.widget.Toast
import com.fireblocks.sdkdemo.R

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()


fun copyToClipboard(context: Context, textToCopy: CharSequence?) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("copied_text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_LONG).show()
    }
}