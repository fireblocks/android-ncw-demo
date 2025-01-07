package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
abstract class BaseTakeoverViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(TakeoverUiState())
    val uiState: StateFlow<TakeoverUiState> = _uiState.asStateFlow()

    data class TakeoverUiState(
        val takeoverResult: Set<FullKey> = setOf(),
        val assets: List<SupportedAsset> = arrayListOf(),
    )

    fun onTakeoverResult(value: Set<FullKey>) {
        _uiState.update { currentState ->
            currentState.copy(
                takeoverResult = value,
            )
        }
    }

    internal fun onAssets(assets: List<SupportedAsset>) {
        _uiState.update { currentState ->
            currentState.copy(
                assets = assets,
            )
        }
    }

    fun takeover(context: Context) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().takeover(context) {
                showProgress(false)
                if (isTakeoverResultValid(it)) {
                    onTakeoverResult(it)
                }
            }
        }.onFailure {
            showError()
        }
    }

    private fun isTakeoverResultValid(fullKeySet: Set<FullKey>): Boolean {
        fullKeySet.forEach {
            if (it.privateKey.isNullOrEmpty() || it.error != null) {
                showError(fireblocksError = it.error)
                return false
            }
        }
        return true
    }

    abstract fun loadAssets(context: Context, takeoverResult: Set<FullKey>)

    override fun clean() {
        super.clean()
        _uiState.update { TakeoverUiState() }
    }
}