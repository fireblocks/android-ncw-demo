package com.fireblocks.sdkdemo.bl.core.environment

import android.content.Context

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
data class EnvironmentImpl(
    private val context: Context,
    private val env: String,
    private val isDefault: Boolean,
    private val envIndicator: String,
    private val host: String,
    private val logTag: String,
) : Environment {

    override fun env(): String {
        return env
    }

    override fun host(): String {
        return host
    }

    override fun getLogTag(): String {
        return logTag
    }

    override fun envIndicator(): String {
        return envIndicator
    }

    override fun isDefault(): Boolean {
        return isDefault
    }
}