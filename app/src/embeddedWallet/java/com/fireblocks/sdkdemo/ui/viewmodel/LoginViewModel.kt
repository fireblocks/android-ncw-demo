package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
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

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class LoginViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    data class LoginUiState(
        val loginFlow: LoginFlow = LoginFlow.SIGN_IN,
        val showSnackbar: Boolean = false,
        val snackbarText: String = "",
        val signInState: SignInState = SignInState(),
        val errorResId: Int? = null,
    )

    enum class LoginFlow {
        SIGN_IN,
        SIGN_UP,
        JOIN_WALLET
    }

    fun showError(errorResId: Int? = null) {
        updateErrorResId(errorResId)
        super.showError()
    }

    override fun onError(showError: Boolean) {
        if (showError) {
            showError(errorResId = null)
        }
    }

    private fun updateErrorResId(errorResId: Int? = null) {
        _uiState.update { currentState ->
            currentState.copy(
                errorResId = errorResId,
            )
        }
    }

    fun onSignInResult(result: SignInResult) {
        _uiState.update { it.copy(
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

    fun handleSuccessSignIn(loginFlow: LoginFlow, context: Context, viewModel: LoginViewModel) {
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.createEmbeddedWallet(context, viewModel)
        fireblocksManager.createAccountIfNeeded(context, viewModel)
        when(loginFlow) {
            LoginFlow.SIGN_IN -> {
                val lastUsedDeviceId = getDeviceId(context)
                if (lastUsedDeviceId.isNotEmpty()) {
                    initializeFireblocksSdk(lastUsedDeviceId, context, viewModel)
                } else {
                    launch {
                        withContext(coroutineContext) {
                            fireblocksManager.getLatestBackup(viewModel = this@LoginViewModel).onSuccess { latestBackupResponse ->
                                val keys = latestBackupResponse.keys
                                if (keys.isEmpty()) {
                                    showError(errorResId = R.string.sign_in_error_no_wallet) // no previous device or wallet
                                } else {
                                    val deviceId: String? = keys.firstOrNull()?.deviceId
                                    deviceId?.let {
                                        initializeFireblocksSdk(it, context, viewModel)
                                    } ?: showError(errorResId = R.string.sign_in_error_no_wallet) // no previous device or wallet
                                }
                            }.onFailure {
                                showError(errorResId = R.string.sign_in_error_no_backup) // no previous backup for this deviceId
                            }
                        }
                    }
                }
            }
            LoginFlow.SIGN_UP -> {
                initializeFireblocksSdk(Fireblocks.generateDeviceId(), context, viewModel)
            }
            LoginFlow.JOIN_WALLET -> {
                //TODO add event handler.
                val deviceId = Fireblocks.generateDeviceId()
                MultiDeviceManager.instance.addJoinWalletDeviceId(deviceId)
                initializeFireblocksSdk(deviceId, context, viewModel, true)
            }
        }
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel, joinWallet: Boolean = false) {
        if (deviceId.isNotEmpty() && !joinWallet) {
            StorageManager.get(context, deviceId).apply {
                MultiDeviceManager.instance.addDeviceId(context, deviceId)
            }
        }
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.clearTransactions()

        val availableEnvironments = EnvironmentProvider.availableEnvironments()
        val defaultEnv = availableEnvironments.firstOrNull {
            it.isDefault()
        }
        if (defaultEnv == null) {
            Timber.e("No default environment found")
            return
        }
        EnvironmentProvider.getInstance().setEnvironment(context, deviceId, defaultEnv)
        fireblocksManager.init(context, viewModel, true, joinWallet)
    }
}