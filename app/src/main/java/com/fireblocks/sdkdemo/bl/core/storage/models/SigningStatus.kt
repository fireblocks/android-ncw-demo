package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

enum class SigningStatus: Serializable {
    @SerializedName("BROADCASTING")
    BROADCASTING,
    @SerializedName("SUBMITTED")
    SUBMITTED,
    @SerializedName("QUEUED")
    QUEUED,
    @SerializedName("PENDING_SIGNATURE")
    PENDING_SIGNATURE,
    @SerializedName("PENDING_AUTHORIZATION")
    PENDING_AUTHORIZATION,
    @SerializedName("PENDING_3RD_PARTY_MANUAL_APPROVAL")
    PENDING_3RD_PARTY_MANUAL_APPROVAL,
    @SerializedName("PENDING_3RD_PARTY")
    PENDING_3RD_PARTY,
    @SerializedName("PENDING_CONSOLE_APPROVAL")
    PENDING_CONSOLE_APPROVAL,
    @SerializedName("SIGNED")
    SIGNED,
    @SerializedName("SIGNED_BY_CLIENT")
    SIGNED_BY_CLIENT,
    @SerializedName("COMPLETED")
    COMPLETED,
    @SerializedName("CONFIRMING")
    CONFIRMING,
    @SerializedName("REJECTED_BY_CLIENT")
    REJECTED_BY_CLIENT,
    @SerializedName("CANCELLED")
    CANCELLED,
    @SerializedName("FAILED")
    FAILED,
    @SerializedName("BLOCKED")
    BLOCKED
    ;

    companion object {
        @JvmStatic
        fun from(value: String): SigningStatus {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                SIGNED
            }
        }

        @JvmStatic
        val displayStatuses = arrayOf(PENDING_SIGNATURE,
            PENDING_CONSOLE_APPROVAL)
    }
}