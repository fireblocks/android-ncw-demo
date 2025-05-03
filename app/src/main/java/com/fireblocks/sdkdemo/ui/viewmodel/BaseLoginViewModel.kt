package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.signin.SignInResult
import com.fireblocks.sdkdemo.ui.signin.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by Fireblocks Ltd. on 13/01/2025.
 */
open class BaseLoginViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    data class LoginUiState(
        val loginFlow: LoginFlow = LoginFlow.SIGN_IN,
        val passedLogin: Boolean = false,
        val passedJoinWallet: Boolean = false,
        val passedInitForRecover: Boolean = false,
        val showSnackbar: Boolean = false,
        val snackbarText: String = "",
        val signInState: SignInState = SignInState(),
    )

    enum class LoginFlow {
        SIGN_IN,
        SIGN_UP,
        DELETE_AND_CREATE_NEW_WALLET
    }

    fun onSignInResult(result: SignInResult) {
        _uiState.update {
            it.copy(
                signInState = SignInState(
                    isSignInSuccessful = result.data != null,
                    signInError = result.errorMessage
                ),
            )
        }
    }

    fun resetSignInState() {
        _uiState.update {
            it.copy(
                signInState = SignInState(),
            )
        }
    }

    fun setLoginFlow(value: LoginFlow) {
        _uiState.update { currentState ->
            currentState.copy(
                loginFlow = value,
            )
        }
    }

    fun onSnackbarChanged(show: Boolean, text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                showSnackbar = show,
                snackbarText = text
            )
        }
    }

    fun onPassedLogin(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                passedLogin = value
            )
        }
    }

    fun onPassedInitForJoinWallet(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                passedJoinWallet = value
            )
        }
    }

    fun onPassedInitForRecover(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                passedInitForRecover = value
            )
        }
    }

    fun onError(context: Context, throwable: Throwable? = null, message: String? = null, @StringRes resId: Int? = null) {
        FireblocksManager.getInstance().signOut(context) {
            clearUiState()
            if (throwable != null) {
                showError(throwable = throwable)
            } else if (message != null) {
                showError(message = message)
            } else if (resId != null) {
                showError(resId = resId)
            } else {
                showError()
            }
        }
    }

    fun addNewDeviceId(context: Context): String {
        val deviceId = Fireblocks.generateDeviceId()
        StorageManager.get(context, deviceId).apply {
            MultiDeviceManager.instance.addDeviceId(context, deviceId)
        }
        return deviceId
    }

    fun clearUiState() {
        onPassedLogin(false)
        onPassedInitForJoinWallet(false)
        onPassedInitForRecover(false)
    }

}