package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.keys.KeyBackupStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class BackupKeysViewModel: BaseViewModel() {

    private val _uiState = MutableStateFlow(BackupKeysUiState())
    val uiState: StateFlow<BackupKeysUiState> = _uiState.asStateFlow()

    data class BackupKeysUiState(
        val backupSuccess: Boolean = false,
        val showCopyLocallyScreen: Boolean = false,
        val passphrase: String = ""
    )

    fun onBackupSuccess(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                backupSuccess = value,
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

    fun backupKeys(passphrase: String, copyLocally: Boolean = false) {
        showProgress(true)
        onCopyLocallyState(copyLocally)
        runCatching {
            FireblocksManager.getInstance().backupKeys(passphrase) { keyBackupSet ->
                updateUserFlow(UiState.Idle)
                val backupError = keyBackupSet.firstOrNull {
                    it.keyBackupStatus != KeyBackupStatus.SUCCESS
                }
                val success = backupError == null
                if (copyLocally && success) {
                    setPassphrase(passphrase)
                }
                onError(!success)
                onBackupSuccess(success)
            }
        }.onFailure {
            Timber.e(it)
            onError()
            snackBar.postValue(ObservedData("${it.message}"))
        }
    }
}