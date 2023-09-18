package com.fireblocks.sdkdemo.bl.core.base.fingerprint

/**
 * Created by Fireblocks ltd. on 06/04/2020
 */
class FingerPrintException(val errorCode: Int, val cancelled: Boolean) : RuntimeException("Fingerprint error occurred:$errorCode")