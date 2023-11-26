package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 16/11/2023.
 */
data class PassphraseInfo(@SerializedName("passphraseId") val passphraseId: String? = null,
                          @SerializedName("location") val location: PassphraseLocation? = null)
