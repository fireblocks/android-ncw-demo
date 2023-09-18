package com.fireblocks.sdkdemo.bl.core.environment

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
interface Environment {
    fun env(): String
    fun host(): String
    fun getLogTag(): String
    fun envIndicator(): String
    fun isDefault(): Boolean
}