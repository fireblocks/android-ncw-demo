package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 19/11/2023.
 */
data class PassphraseInfos(@SerializedName("passphrases") val passphrases: ArrayList<PassphraseInfo>? = null)
