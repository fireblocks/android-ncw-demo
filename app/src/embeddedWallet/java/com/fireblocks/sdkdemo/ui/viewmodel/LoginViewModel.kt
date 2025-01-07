package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.ew.bl.core.error.ResponseError
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.signin.SignInResult
import com.fireblocks.sdkdemo.ui.signin.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_NOT_FOUND

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class LoginViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    data class LoginUiState(
        val loginFlow: LoginFlow = LoginFlow.SIGN_IN,
        val passedLogin: Boolean? = null,
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

    fun handleSuccessSignIn(context: Context) {
        showProgress(true)
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.createEmbeddedWallet(context)
        val lastUsedDeviceId = getDeviceId(context)
        if (lastUsedDeviceId.isNotEmpty()) {
            initializeFireblocksSdk(lastUsedDeviceId, context, this)
        } else {
            launch {
                withContext(coroutineContext) {
                    fireblocksManager.getLatestBackup(viewModel = this@LoginViewModel).onSuccess { latestBackupResponse ->
                        val keys = latestBackupResponse.keys
                        if (keys.isNullOrEmpty()) {
                            onError(context, resId = R.string.sign_in_error_no_wallet) // no previous device or wallet
                        } else {
                            val deviceId: String? = keys.firstOrNull()?.deviceId
                            if (!deviceId.isNullOrEmpty()) {
                                fireblocksManager.addTempDeviceId(deviceId)
                                initializeFireblocksSdk(deviceId, context, this@LoginViewModel)
                            } else {
                                // no previous device or wallet
                                onError(context, resId = R.string.sign_in_error_no_wallet)
                            }
                        }
                    }.onFailure {
                        if (it is ResponseError && it.code == HTTP_NOT_FOUND) {
                            Timber.w(it, "Failed to get latest backup")
                            setLoginFlow(LoginFlow.SIGN_UP)
                            val deviceId = addNewDeviceId(context)
                            initializeFireblocksSdk(deviceId, context, this@LoginViewModel, loginFlow = LoginFlow.SIGN_UP)
                        } else {
                            onError(context, throwable = it)
                        }
                    }
                }
            }
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

    fun initFireblocksSdkForJoinWalletFlow(context: Context) {
        val deviceId = Fireblocks.generateDeviceId()
        MultiDeviceManager.instance.addTempDeviceId(deviceId)
        FireblocksManager.getInstance().initFireblocks(context, viewModel = this, forceInit = true, startPollingTransactions = false, deviceId = deviceId, notifyOnSuccess = false)
    }

    private fun addNewDeviceId(context: Context): String {
        val deviceId = Fireblocks.generateDeviceId()
        StorageManager.get(context, deviceId).apply {
            MultiDeviceManager.instance.addDeviceId(context, deviceId)
        }
        return deviceId
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel, loginFlow: LoginFlow? = null) {
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.clearTransactions()

        val availableEnvironments = EnvironmentProvider.availableEnvironments()
        val defaultEnv = availableEnvironments.firstOrNull {
            it.isDefault()
        }
        if (defaultEnv == null) {
            onError(context, message = "No default environment found")
            return
        }
        EnvironmentProvider.getInstance().setEnvironment(context, deviceId, defaultEnv)
        fireblocksManager.init(context, viewModel, true, deviceId = deviceId, loginFlow = loginFlow)
    }

    fun clearUiState() {
        onPassedLogin(false)
    }

}