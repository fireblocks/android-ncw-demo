package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
class TakeoverViewModel: BaseViewModel() {
    private val _uiState = MutableStateFlow(TakeoverUiState())
    val uiState: StateFlow<TakeoverUiState> = _uiState.asStateFlow()

    data class TakeoverUiState(
        val takeoverResult: Set<FullKey> = setOf(),
        val derivedAssetKey: KeyData = KeyData()
    )

    fun onTakeoverResult(value: Set<FullKey>){
        _uiState.update { currentState ->
            currentState.copy(
                takeoverResult = value,
            )
        }
    }


    fun takeover() {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().takeover {
                showProgress(false)
                if (isTakeoverResultValid(it)) {
                    onTakeoverResult(it)
                } else {
                    onError(true)
                }
            }
        }.onFailure {
            onError(true)
        }
    }

    private fun isTakeoverResultValid(fullKeySet: Set<FullKey>): Boolean {
        fullKeySet.forEach {
            if (it.privateKey.isNullOrEmpty() || it.error != null){
                return false
            }
        }
        return true
    }
}