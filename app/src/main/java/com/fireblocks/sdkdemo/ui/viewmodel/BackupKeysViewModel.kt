package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.KeyBackupStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
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
    private var passphraseId: String? = null

    data class BackupKeysUiState(
        val backupSuccess: Boolean = false,
        val errorResId: Int = R.string.backup_keys_error,
    )

    fun onBackupSuccess(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                backupSuccess = value,
            )
        }
    }

    private fun setPassphraseId(passphraseId: String) {
        this.passphraseId = passphraseId
    }

    fun getPassphraseId(): String? = passphraseId

    private fun updateErrorResId(errorResId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                errorResId = errorResId,
            )
        }
    }

    fun showError(errorResId: Int? = uiState.value.errorResId) {
        updateErrorResId(errorResId ?: R.string.backup_keys_error)
        super.showError()
    }

    fun backupKeys(passphrase: String) {
        showProgress(true)
        runCatching {
            val passphraseId = getPassphraseId()
            if (passphraseId.isNullOrEmpty()){
                Timber.e("Passphrase id is empty")
                onError()
            } else {
                FireblocksManager.getInstance().backupKeys(passphrase, passphraseId) { keyBackupSet ->
                    updateUserFlow(UiState.Idle)
                    val backupError = keyBackupSet.firstOrNull {
                        it.keyBackupStatus != KeyBackupStatus.SUCCESS
                    }
                    val success = backupError == null
                    onError(!success)
                    onBackupSuccess(success)
                }
            }
        }.onFailure {
            Timber.e(it)
            onError()
            snackBar.postValue(ObservedData("${it.message}"))
        }
    }

    fun getPassphraseId(context: Context, passphraseLocation: PassphraseLocation, callback: (passphraseId: String?) -> Unit) {
        showProgress(true)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            fireblocksManager.getOrCreatePassphraseId(context, passphraseLocation) { passphraseId ->
                if (passphraseId.isNullOrEmpty()) {
                    onError()
                    callback( null)
                } else {
                    setPassphraseId(passphraseId)
                    callback(passphraseId)
                }
            }
        }.onFailure {
            Timber.e(it)
            onError()
            snackBar.postValue(ObservedData("${it.message}"))
            callback( null)
        }
    }
}