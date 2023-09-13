package com.fireblocks.sdkdemo.bl.core.server

import android.content.Context

interface HeaderProvider {
    fun deviceId(): String

    fun context(): Context
}