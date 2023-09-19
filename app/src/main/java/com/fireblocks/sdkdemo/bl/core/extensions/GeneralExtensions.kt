package com.fireblocks.sdkdemo.bl.core.extensions

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.annotation.Keep
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.dialog.DialogModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
inline fun <reified T> Iterator<T>.nextOrNull(): T? {
    if (hasNext()) {
        return next()
    }
    return null
}

inline fun <reified T> Iterator<T>.previousOrNull(): T? {
    if (this is ListIterator) {
        if (hasPrevious()) {
            return previous()
        }
    }
    return null
}


fun String.getSuffix(suffix: Int): String {
    if (this.length < suffix || suffix < 1) {
        return this
    }

    return runCatching {
        this.substring(this.length - suffix, this.length)
    }.onFailure {
        Timber.w("could not get suffix of:$this")
    }.getOrDefault(this)
}

@Keep
fun resultReceiver(onResult: (Int, Bundle?) -> Unit): ResultReceiver {
    return object : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)
            onResult(resultCode, resultData)
        }
    }
}

fun fingerPrintCancelledDialogModel(context: Context, cont: Continuation<Boolean>): DialogModel {
    return DialogModel(context.getString(R.string.authentication_required),
        context.getString(R.string.without_authentication_cant_sign),
        context.getString(R.string.authenticate),
        context.getString(R.string.abort_setup),
        resultReceiver { result, _ ->
            when (result) {
                Activity.RESULT_OK -> cont.resume(true)
                else -> cont.resume(false)
            }
        })
}

fun postDelayedOnMain(timeInSeconds: Long, runnable: Runnable) {
    Handler(Looper.getMainLooper()).postDelayed(runnable, TimeUnit.SECONDS.toMillis(timeInSeconds))
}

fun postOnMain(runnable: Runnable) {
    Handler(Looper.getMainLooper()).post(runnable)
}

fun Context.bottomNavbarHeightPixels(): Int {
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}