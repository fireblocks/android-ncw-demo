package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Created by Fireblocks Ltd. on 05/10/2023.
 */
abstract class BaseSelectAssetViewModel: BaseViewModel()  {
    private val _uiState = MutableStateFlow(SelectAssetUiState())
    val uiState: StateFlow<SelectAssetUiState> = _uiState.asStateFlow()

    data class SelectAssetUiState(
        val assets: List<SupportedAsset> = arrayListOf(),
        val assetAddedToWallet: Boolean = false,
    )

    fun onAssets(assets: List<SupportedAsset>) {
        _uiState.update { currentState ->
            currentState.copy(
                assets = assets
            )
        }
        _filteredAssets.value = assets
    }

    protected fun onAssetAdded(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                assetAddedToWallet = value
            )
        }
    }

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
    private val _filteredAssets = MutableStateFlow(uiState.value.assets)
    val filteredAssets = searchText
        .onEach { _isSearching.update { true } }
        .combine(_filteredAssets) { text, assets ->
            if(text.isBlank()) {
                assets
            } else {
                assets.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _filteredAssets.value
        )

    abstract fun addAssetToWallet(context: Context, assetId: String)

    abstract fun loadAssets(context: Context, state: UiState)
}

fun SupportedAsset.doesMatchSearchQuery(query: String): Boolean {
    return this.name.contains(query, ignoreCase = true) || this.symbol.contains(query, ignoreCase = true)
}