package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.KeyBackupStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
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
class BackupKeysViewModel: BaseBackupKeysViewModel() {

    private val _uiState = MutableStateFlow(BackupKeysUiState())
    val uiState: StateFlow<BackupKeysUiState> = _uiState.asStateFlow()
    private var passphraseId: String? = null

    data class BackupKeysUiState(
        val backupSuccess: Boolean = false,
        val errorResId: Int = R.string.backup_keys_error,
        val shouldGetBackupInfo: Boolean = true,
        val lastBackupDate: String = "",
    )

    fun updateShouldGetBackupInfo(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                shouldGetBackupInfo = value,
            )
        }
    }

    fun updateLastBackupDate(lastBackupDate: String) {
        _uiState.update { currentState ->
            currentState.copy(
                lastBackupDate = lastBackupDate,
            )
        }
    }

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


    override fun showError(throwable: Throwable?, message: String?, resId: Int?) {
        val errorResId = resId ?: R.string.backup_keys_error
        super.showError(throwable, message, resId = errorResId)
    }

    fun backupKeys(context: Context, passphrase: String) {
        showProgress(true)
        runCatching {
            val passphraseId = getPassphraseId()
            if (passphraseId.isNullOrEmpty()){
                Timber.e("Passphrase id is empty")
                showError()
            } else {
                FireblocksManager.getInstance().backupKeys(context, passphrase, passphraseId) { keyBackupSet ->
                    updateUserFlow(UiState.Idle)
                    val backupError = keyBackupSet.firstOrNull {
                        it.keyBackupStatus != KeyBackupStatus.SUCCESS
                    }
                    val success = backupError == null
                    if (!success){
                        showError()
                    }
                    onBackupSuccess(success)
                }
            }
        }.onFailure {
            Timber.e(it)
            showError()
            snackBar.postValue(ObservedData("${it.message}"))
        }
    }

    fun getPassphraseId(context: Context, passphraseLocation: PassphraseLocation, callback: (passphraseId: String?) -> Unit) {
        showProgress(true)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            fireblocksManager.getOrCreatePassphraseId(context, passphraseLocation) { passphraseId ->
                if (passphraseId.isNullOrEmpty()) {
                    showError()
                    callback( null)
                } else {
                    setPassphraseId(passphraseId)
                    callback(passphraseId)
                }
            }
        }.onFailure {
            Timber.e(it)
            showError()
            snackBar.postValue(ObservedData("${it.message}"))
            callback( null)
        }
    }
}