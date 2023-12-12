package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Fireblocks Ltd. on 16/11/2023.
 */
enum class PassphraseLocation : Serializable {
    @SerializedName("GoogleDrive")
    GoogleDrive,

    @SerializedName("iCloud")
    iCloud,
}
