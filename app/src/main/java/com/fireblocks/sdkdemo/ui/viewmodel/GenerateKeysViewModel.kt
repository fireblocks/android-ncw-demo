package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.screens.generatedSuccessfully
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class GenerateKeysViewModel: BaseViewModel() {

    private val _uiState = MutableStateFlow(GenerateKeysUiState())
    val uiState: StateFlow<GenerateKeysUiState> = _uiState.asStateFlow()

    data class GenerateKeysUiState(
        val generatedKeys: Boolean = false,
    )

    private fun onGeneratedKeys(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                generatedKeys = value,
            )
        }
    }

    fun generateKeys(context: Context, algorithms: Set<Algorithm>) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().generateMpcKeys(algorithms) {
                val generatedSuccessfully = generatedSuccessfully(context)
                if (generatedSuccessfully){
                    FireblocksManager.getInstance().createAssets(context)
                    showProgress(false)
                } else {
                    showError()
                }
                onGeneratedKeys(generatedSuccessfully)
            }
        }.onFailure {
            showError()
            snackBar.postValue(ObservedData(it.message ?: "Failed to generate keys"))
        }
    }
}