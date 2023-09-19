package com.fireblocks.sdkdemo.bl.core.server

import android.content.Context

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
interface HeaderProvider {
    fun deviceId(): String

    fun context(): Context
}