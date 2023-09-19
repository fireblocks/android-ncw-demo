package com.fireblocks.sdkdemo.bl.core.base

/**
 * Created by Fireblocks Ltd. on 17/09/2020
 */
interface ApplicationStateListener {
    fun onApplicationResumed()
    fun onApplicationPaused()
}