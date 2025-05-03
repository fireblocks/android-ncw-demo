package com.fireblocks.sdkdemo.bl.core.storage.models

/**
 * Created by Fireblocks Ltd. on 08/08/2023.
 */
enum class FeeLevel {
    LOW, MEDIUM, HIGH;

    companion object {
        @JvmStatic
        fun from(value: String?): FeeLevel {
            return value?.let {
                try {
                    valueOf(value)
                } catch (e: IllegalArgumentException) {
                    MEDIUM
                }
            } ?: MEDIUM
        }
    }
}