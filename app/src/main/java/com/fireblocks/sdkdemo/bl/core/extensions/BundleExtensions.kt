package com.fireblocks.sdkdemo.bl.core.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
fun Bundle.put(key: String, value: Any?): Bundle {
    when (value) {
        is String -> this.putString(key, value)
        is Serializable -> this.putSerializable(key, value)
        is Int -> this.putInt(key, value)
        is Long -> this.putLong(key, value)
        is Parcelable -> this.putParcelable(key, value)
        is IntArray -> this.putIntArray(key, value)
        is LongArray -> this.putLongArray(key, value)
        is Bundle -> this.putBundle(key, value)
        is CharArray -> this.putCharArray(key, value)
        is Boolean -> this.putBoolean(key, value)
    }
    return this
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

fun Bundle.putBundleContent(bundle: Bundle): Bundle {
    this.putAll(bundle)
    return this
}