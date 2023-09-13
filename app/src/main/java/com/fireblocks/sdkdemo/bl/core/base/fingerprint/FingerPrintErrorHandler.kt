package com.fireblocks.sdkdemo.bl.core.base.fingerprint

import androidx.annotation.Keep

/**
 * Created by Fireblocks Ltd. on 2019-12-29
 */
interface FingerPrintErrorHandler {
    enum class FingerPrintError(val errorCode: Int) {
        FINGERPRINT_NO_ERROR(-1),//
        FINGERPRINT_MODULE_NOT_AVAILABLE(-7), //
        FINGERPRINT_NOT_ENROLLED(-2), //
        FINGERPRINT_ENROLLMENT_INVALIDATED(-1000), //
        FINGERPRINT_NOT_ENROLLED_WORK_PROFILE(-3), //
        USER_CANCELLED(-4), //
        FINGERPRINT_TOO_MANY_ATTEMPTS(-5), //
        FINGERPRINT_TIMED_OUT(-6), //
        FINGERPRINT_NOT_COMPATIBLE(-8) //
    }

    fun onError(error: FingerPrintError)

    @Keep
    companion object {
        val EMPTY = object : FingerPrintErrorHandler {
            override fun onError(error: FingerPrintError) {
            }
        }
    }
}
