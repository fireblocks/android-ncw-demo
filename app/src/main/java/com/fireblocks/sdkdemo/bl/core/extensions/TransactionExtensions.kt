package com.fireblocks.sdkdemo.bl.core.extensions

import com.fireblocks.sdk.transactions.TransactionSignatureStatus

/**
 * Created by Fireblocks Ltd. on 29/01/2024.
 */

fun TransactionSignatureStatus.hasFailed(): Boolean {
    return when (this) {
        TransactionSignatureStatus.ERROR, TransactionSignatureStatus.TIMEOUT -> true
        else -> false
    }
}