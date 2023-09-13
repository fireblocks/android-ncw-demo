package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.view.WindowInsetsCompat

fun PopupWindow.dimBehind(dimAmount: Float = 0.5f) {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = dimAmount
    wm.updateViewLayout(container, p)
}


val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()


fun Color.asStateList(): ColorStateList {
    return ColorStateList.valueOf(this.toArgb())
}

fun Int.asStateList(context: Context): ColorStateList {
    return ColorStateList.valueOf(context.getColor(this))
}

fun Int.asStateList(): ColorStateList {
    return ColorStateList.valueOf(this)
}

private fun isKeyboardVisible(insets: WindowInsets): Boolean {
    val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
    val systemWindow = insetsCompat.systemWindowInsets
    val rootStable = insetsCompat.stableInsets
    if (systemWindow.bottom > rootStable.bottom) {
        return true
    }
    return false
}

fun copyToClipboard(context: Context, textToCopy: CharSequence?) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("copied_text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
}