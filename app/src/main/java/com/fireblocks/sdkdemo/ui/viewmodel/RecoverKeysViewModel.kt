package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.KeyRecoveryStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 03/07/2023.
 */
class RecoverKeysViewModel: BaseViewModel() {

    private val _uiState = MutableStateFlow(RecoverKeysUiState())
    val uiState: StateFlow<RecoverKeysUiState> = _uiState.asStateFlow()

    data class RecoverKeysUiState(
        val recoverSuccess: Boolean = false,
        val showCopyLocallyScreen: Boolean = false,
        val passphrase: String = "",
        val showRecoverFromSavedKey: Boolean = false
    )

    fun onRecoverSuccess(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                recoverSuccess = value,
            )
        }
    }

    fun onCopyLocallyState(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                showCopyLocallyScreen = value,
            )
        }
    }

    fun setPassphrase(value: String){
        _uiState.update { currentState ->
            currentState.copy(
                passphrase = value,
            )
        }
    }

    fun recoverKeys(context: Context, passphrase: String) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().recoverKeys(context, passphrase) { keyRecoverSet ->
                showProgress(false)
                val backupError = keyRecoverSet.firstOrNull {
                    it.keyRecoveryStatus != KeyRecoveryStatus.SUCCESS
                }
                val success = backupError == null
                onError(!success)
                onRecoverSuccess(success)
            }
        }.onFailure {
            Timber.e(it)
            onError(true)
            snackBar.postValue(ObservedData("${it.message}"))
        }
    }

}