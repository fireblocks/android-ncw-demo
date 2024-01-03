package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 20/11/2023.
 */
data class BackupInfo(
    @SerializedName("deviceId") var deviceId: String? = null,
    @SerializedName("passphraseId") var passphraseId: String? = null,
    @SerializedName("location") var location: PassphraseLocation? = null,
    @SerializedName("createdAt") val createdAt: Long? = null,
)
