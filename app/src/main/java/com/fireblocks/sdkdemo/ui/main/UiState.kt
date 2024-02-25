package com.fireblocks.sdkdemo.ui.main

/**
 * Created by Fireblocks Ltd. on 10/09/2023.
 */
sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    object Refreshing : UiState

    data class Error(
        val message: String? = null,
        val id: Int? = null,
        val throwable: Throwable? = null,
    ) : UiState
}