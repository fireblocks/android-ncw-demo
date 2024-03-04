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
import kotlinx.coroutines.CancellableContinuation
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Keep
fun resultReceiver(onResult: (Int, Bundle?) -> Unit): ResultReceiver {
    return object : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)
            onResult(resultCode, resultData)
        }
    }
}

fun fingerPrintCancelledDialogModel(context: Context, cont: CancellableContinuation<Boolean>): DialogModel {
    return DialogModel(context.getString(R.string.authentication_required),
        context.getString(R.string.without_authentication_cant_sign),
        context.getString(R.string.authenticate),
        context.getString(R.string.abort_setup),
        resultReceiver { result, _ ->
            Timber.i("fingerPrintCancelledDialogModel result: $result")
            if (cont.isActive) {
                when (result) {
                    Activity.RESULT_OK -> cont.resume(true)
                    else -> cont.resume(false)
                }
            }
        })
}

fun postOnMain(runnable: Runnable) {
    Handler(Looper.getMainLooper()).post(runnable)
}

