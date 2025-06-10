package com.fireblocks.sdkdemo.bl.notifications

import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus

data class NotificationPayload(
    val type: String? = null,
    val txId: String? = null,
    val txHash: String? = null,
    val status: SigningStatus? = null
) {
    fun isEmpty(): Boolean {
        return txId.isNullOrEmpty()
    }
}
