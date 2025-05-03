package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NFTsViewModel: BaseViewModel() {
    val _uiState: MutableStateFlow<NFTsUiState> = MutableStateFlow(NFTsUiState())
    val uiState: StateFlow<NFTsUiState> = _uiState.asStateFlow()

    data class NFTsUiState(
        val nfts: List<TokenOwnershipResponse> = emptyList(),
        val selectedNFT : TokenOwnershipResponse? = null
    )

    fun onNFTs(nfts: List<TokenOwnershipResponse>) {
        _uiState.update { currentState ->
            currentState.copy(
                nfts = nfts
            )
        }
    }

    fun onNFTSelected(nft: TokenOwnershipResponse) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedNFT = nft
            )
        }
    }

    fun loadNFTs(state: UiState) {
        updateUserFlow(state)
        launch {
            withContext(coroutineContext) {
                // Load NFTs
                FireblocksManager.getInstance().getOwnedNfts(viewModel = this@NFTsViewModel).onSuccess { paginatedResponse ->
                    showProgress(false)
                    val nfts = arrayListOf<TokenOwnershipResponse>()
                    paginatedResponse.data?.forEach {
                        nfts.add(it)
                    }
                    onNFTs(nfts)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    fun getSelectedNFT(): TokenOwnershipResponse? {
        return uiState.value.selectedNFT
    }
}