package com.fireblocks.sdkdemo.bl.core.environment

interface Environment {
    fun env(): String
    fun host(): String
    fun getLogTag(): String
    fun envIndicator(): String
    fun isDefault(): Boolean
}