package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.keys.KeyRecoveryStatus
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class RecoverKeysViewModel: BaseViewModel() {

    private val _uiState = MutableStateFlow(RecoverKeysUiState())
    val uiState: StateFlow<RecoverKeysUiState> = _uiState.asStateFlow()

    private var passphraseCallback: ((String) -> Unit)? = null
    private var passphraseId: String? = null

    data class RecoverKeysUiState(
        val recoverSuccess: Boolean = false,
        val showCopyLocallyScreen: Boolean = false,
        val passphrase: String = "",
        val showRecoverFromSavedKey: Boolean = false,
        val shouldStartRecover: Boolean = true,
        val canRecoverFromGoogleDrive: Boolean = true,
        val errorResId: Int = R.string.recover_wallet_error,
        )

    fun setPassphraseId(passphraseId: String) {
        this.passphraseId = passphraseId
    }

    fun getPassphraseId(): String? = passphraseId

    fun setPassphraseCallback(callback: (passphrase: String) -> Unit) {
        passphraseCallback = callback
    }

    private fun updateErrorResId(errorResId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                errorResId = errorResId,
            )
        }
    }

    fun showError(errorResId: Int? = uiState.value.errorResId) {
        updateErrorResId(errorResId ?: R.string.recover_wallet_error)
        super.showError()
    }

    fun onCanRecoverFromGoogleDrive(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                canRecoverFromGoogleDrive = value,
            )
        }
    }

    private fun onRecoverSuccess(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                recoverSuccess = value,
            )
        }
    }

    fun updateShouldStartRecover(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                shouldStartRecover = value,
            )
        }
    }

    fun recoverKeys(passphraseResolver: FireblocksPassphraseResolver) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().recoverKeys(passphraseResolver) { keyRecoverSet ->
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

    fun resolvePassphrase(passphrase: String) {
        passphraseCallback?.invoke(passphrase)
    }
}