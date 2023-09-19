package com.fireblocks.sdkdemo.bl.core.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Created by Fireblocks Ltd. on 09/09/2023.
 */

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}