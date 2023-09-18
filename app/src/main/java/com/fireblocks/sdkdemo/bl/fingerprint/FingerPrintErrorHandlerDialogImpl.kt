package com.fireblocks.sdkdemo.bl.fingerprint

import android.app.Activity
import android.content.Context
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.base.fingerprint.FingerPrintErrorHandler
import com.fireblocks.sdkdemo.bl.core.extensions.resultReceiver
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 06/04/2020
 */
class FingerPrintErrorHandlerDialogImpl(val context: Context) : FingerPrintErrorHandler {
    override fun onError(error: FingerPrintErrorHandler.FingerPrintError) {
        val message = when (error) {
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_MODULE_NOT_AVAILABLE -> context.getString(R.string.fingerprint_not_available)
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_ENROLLED -> context.getString(R.string.fingerprint_not_enrolled)
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_ENROLLED_WORK_PROFILE -> context.getString(R.string.fingerprint_not_enrolled_workprofile)
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_ENROLLMENT_INVALIDATED -> context.getString(R.string.fingerprint_invalidated)
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_TOO_MANY_ATTEMPTS -> context.getString(R.string.too_many_fingerprint_attempts)
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_TIMED_OUT -> context.getString(R.string.fingerprint_timeout)
            FingerPrintErrorHandler.FingerPrintError.USER_CANCELLED -> null
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NO_ERROR -> null
            FingerPrintErrorHandler.FingerPrintError.FINGERPRINT_NOT_COMPATIBLE -> context.getString(R.string.fingerprint_not_compatible)
        }
        message?.let {
            Timber.e("Fingerprint error: $message")
            DialogUtil.getInstance().start(context.getString(R.string.error_occurred),
                message,
                context.getString(R.string.OK),
                postOnMainThread = true,
                resultReceiver = resultReceiver { resultCode, _ ->
                    if (resultCode == Activity.RESULT_OK) {
                        FingerprintFailedListener.instance.postUpdate()
                    }
                })
        }
    }
}