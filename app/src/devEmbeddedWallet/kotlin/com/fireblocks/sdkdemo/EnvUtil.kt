package com.fireblocks.sdkdemo

import com.fireblocks.sdk.ew.EmbeddedWalletEnvironment

object EnvUtil {
    fun getEmbeddedWalletEnvironment() : EmbeddedWalletEnvironment {
        return EmbeddedWalletEnvironment.DEFAULT
    }
}