package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
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
            FireblocksManager.getInstance().generateMpcKeys(context, algorithms) {
                val keyDescriptors = FireblocksManager.getInstance().getKeyCreationStatus(context, getDeviceId(context))
                val isECDSAReady = keyDescriptors.any { it.algorithm == Algorithm.MPC_ECDSA_SECP256K1 && it.keyStatus == KeyStatus.READY }
                val isEDDSAReady = keyDescriptors.any { it.algorithm == Algorithm.MPC_EDDSA_ED25519 && it.keyStatus == KeyStatus.READY }
                if (isECDSAReady && isEDDSAReady) {
                    onGeneratedKeys(true)
                } else if (!isECDSAReady && !isEDDSAReady){
                    showError(UiState.Error(id = R.string.generate_keys_error))
                } else if (!isECDSAReady){
                    showError(UiState.Error(id = R.string.generate_ecdsa_key_error))
                } else {
                    showError(UiState.Error(id = R.string.generate_eddsa_key_error))
                }
            }
        }.onFailure {
            showError()
        }
    }
}