package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class RecoverKeysViewModel: BaseBackupKeysViewModel() {

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
        )

    fun setPassphraseId(passphraseId: String) {
        this.passphraseId = passphraseId
    }

    fun getPassphraseId(): String? = passphraseId

    fun setPassphraseCallback(callback: (passphrase: String) -> Unit) {
        passphraseCallback = callback
    }

    override fun showError(throwable: Throwable?, message: String?, resId: Int?) {
        val errorResId = resId ?: R.string.recover_wallet_error
        super.showError(throwable, message, resId = errorResId)
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

    fun recoverKeys(context: Context, passphraseResolver: FireblocksPassphraseResolver) {
        showProgress(true)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            fireblocksManager.recoverKeys(context, passphraseResolver) { keyRecoverSet ->
                showProgress(false)
                val success = fireblocksManager.isRecoveredSuccessfully(keyRecoverSet)
                if (!success) {
                    showError()
                }
                onRecoverSuccess(success)
            }
        }.onFailure {
            Timber.e(it)
            showError()
            snackBar.postValue(ObservedData("${it.message}"))
        }
    }

    fun resolvePassphrase(passphrase: String) {
        passphraseCallback?.invoke(passphrase)
    }
}