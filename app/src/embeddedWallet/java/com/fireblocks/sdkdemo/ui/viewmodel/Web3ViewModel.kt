package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.ew.models.CreateWeb3ConnectionResponse
import com.fireblocks.sdk.ew.models.RespondToConnectionRequest
import com.fireblocks.sdk.ew.models.Web3Connection
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
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
        val web3Connections: List<Web3Connection> = emptyList(),
        val selectedWeb3Connection: Web3Connection? = null,
        val web3ConnectionCreatedResponse: CreateWeb3ConnectionResponse? = null,
        val web3ConnectionApproved: Boolean = false,
        val web3ConnectionDenied: Boolean = false,
        val web3ConnectionRemoved: Boolean = false,
    )

    fun onWeb3ConnectionCreated(value: CreateWeb3ConnectionResponse) {
        _uiState.update { currentState ->
            currentState.copy(
                web3ConnectionCreatedResponse = value
            )
        }
    }

    fun onWeb3ConnectionApproved(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                web3ConnectionApproved = value
            )
        }
    }

    fun onWeb3ConnectionDenied(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                web3ConnectionDenied = value
            )
        }
    }

    fun onWeb3ConnectionsLoaded(items: List<Web3Connection>) {
        _uiState.update { currentState ->
            currentState.copy(
                web3Connections = items
            )
        }
    }

    fun onWeb3ConnectionSelected(item: Web3Connection) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedWeb3Connection = item
            )
        }
    }

    fun onWeb3ConnectionRemoved(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                web3ConnectionRemoved = value
            )
        }
    }

    fun getSelectedWeb3Connection(): Web3Connection? {
        return uiState.value.selectedWeb3Connection
    }

    fun getCreatedWeb3ConnectionResponse(): CreateWeb3ConnectionResponse? {
        return uiState.value.web3ConnectionCreatedResponse
    }

    fun createWeb3Connection(feeLevel: Web3ConnectionFeeLevel, uri: String) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().createWeb3Connection(feeLevel = feeLevel, uri = uri, viewModel = this@Web3ViewModel).onSuccess { response ->
                    showProgress(false)
                    onWeb3ConnectionCreated(response)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    fun submitWeb3Connection(id: String, payload: RespondToConnectionRequest) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().submitWeb3Connection(id, payload, viewModel = this@Web3ViewModel).onSuccess {
                    when (payload.approve) {
                        true -> onWeb3ConnectionApproved(true)
                        false -> onWeb3ConnectionDenied(true)
                    }
                    onWeb3ConnectionApproved(true)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    fun loadWeb3Connections(state: UiState) {
        updateUserFlow(state)
        launch {
            withContext(coroutineContext) {
                // Load NFTs
                FireblocksManager.getInstance().getWeb3Connections(viewModel = this@Web3ViewModel).onSuccess { paginatedResponse ->
                    showProgress(false)
                    val items = arrayListOf<Web3Connection>()
                    paginatedResponse.data?.forEach {
                        items.add(it)
                    }
                    onWeb3ConnectionsLoaded(items)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    fun removeWeb3Connection(id: String) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().removeWeb3Connection(id, viewModel = this@Web3ViewModel).onSuccess {
                    showProgress(false)
                    onWeb3ConnectionRemoved(true)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    override fun clean() {
        super.clean()
        _uiState.value = Web3UiState()
    }

}