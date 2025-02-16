package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Web3ViewModel: BaseViewModel() {
    val _uiState: MutableStateFlow<Web3UiState> = MutableStateFlow(Web3UiState())
    val uiState: StateFlow<Web3UiState> = _uiState.asStateFlow()

    data class Web3UiState(
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

    fun loadNFTs() {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                // Load NFTs
                FireblocksManager.getInstance().getOwnedNfts(viewModel = this@Web3ViewModel).onSuccess { paginatedResponse ->
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