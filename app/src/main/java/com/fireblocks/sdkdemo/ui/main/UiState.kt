package com.fireblocks.sdkdemo.ui.main

import android.content.Context
import androidx.annotation.StringRes

/**
 * Created by Fireblocks Ltd. on 10/09/2023.
 */
sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    object Refreshing : UiState

    data class Error(
        val message: String? = null,
        @StringRes val resId: Int? = null,
        val throwable: Throwable? = null,
    ) : UiState {

        fun getErrorMessage(context: Context, @StringRes defaultResId: Int? = null): String? {
            return when {
                !throwable?.message.isNullOrEmpty() -> throwable?.message
                !message.isNullOrEmpty() -> message
                resId != null -> context.getString(resId)
                defaultResId != null -> context.getString(defaultResId)
                else -> null
            }
        }

    }
}