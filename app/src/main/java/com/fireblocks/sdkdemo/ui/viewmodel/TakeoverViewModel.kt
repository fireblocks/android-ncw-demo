package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.DerivationParams
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
class TakeoverViewModel: BaseViewModel() {
    private val _uiState = MutableStateFlow(TakeoverUiState())
    val uiState: StateFlow<TakeoverUiState> = _uiState.asStateFlow()

    data class TakeoverUiState(
        val takeoverResult: Set<FullKey> = setOf(),
        val assets: List<SupportedAsset> = arrayListOf(),
    )

    fun onTakeoverResult(value: Set<FullKey>){
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

    fun loadAssets(context: Context, takeoverResult: Set<FullKey>) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().getAssetsSummary(context) { assets ->
                takeoverResult.forEach { fullKey ->
                    fullKey.privateKey?.let { privateKey ->
                        assets.forEach { asset ->
                            val derivationParams =  DerivationParams(
                                account = asset.assetAddress?.accountId?.toInt() ?: 0,
                                coinType = asset.coinType ?: 0,
                                change = 0,
                                index = asset.assetAddress?.addressIndex?.toInt() ?: 0)
                            FireblocksManager.getInstance().deriveAssetKey(privateKey, derivationParams) { keyData ->
                                asset.derivedAssetKey = keyData
                            }
                        }
                    }
                }
                showProgress(false)
                onAssets(assets = assets)
            }
        }.onFailure {
            onError(true)
        }
    }

    override fun clean() {
        super.clean()
        _uiState.update { TakeoverUiState() }
    }
}