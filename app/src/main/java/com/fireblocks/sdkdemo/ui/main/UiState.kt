package com.fireblocks.sdkdemo.ui.main

/**
 * Created by Fireblocks ltd. on 10/09/2023.
 */
sealed interface UiState {
    object Idle : UiState
    object Loading : UiState

    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null,
    ) : UiState
}