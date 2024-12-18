package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.signin.SignInResult
import com.fireblocks.sdkdemo.ui.signin.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun handleSuccessSignIn(context: Context) {
        val fireblocksManager = FireblocksManager.getInstance()
        val lastUsedDeviceId = getDeviceId(context)
        if (lastUsedDeviceId.isNotEmpty()) {
            if (!hasKeys(context)) {
                setLoginFlow(LoginFlow.SIGN_UP)
            }
            initializeFireblocksSdk(lastUsedDeviceId, context, this)
        } else {
            FireblocksManager.getInstance().getLatestDevice(context) { device ->
                if (device == null || device.deviceId.isNullOrEmpty() || device.walletId.isNullOrEmpty()) {
                    //showError(errorResId = R.string.sign_in_error_no_wallet) // no previous device or wallet
                    setLoginFlow(LoginFlow.SIGN_UP)
                    val deviceId = addNewDeviceId(context)
                    initializeFireblocksSdk(deviceId, context, this@LoginViewModel)
                } else {
                    FireblocksManager.getInstance().getLatestBackupInfo(context, deviceId = device.deviceId, walletId = device.walletId, useDefaultEnv = true) { backupInfo ->
                        if (backupInfo == null || backupInfo.deviceId.isNullOrEmpty()) {
                            showError(errorResId = R.string.sign_in_error_no_backup) // no previous backup for this deviceId
                        } else {
                            fireblocksManager.addTempDeviceId(backupInfo.deviceId!!)
                            initializeFireblocksSdk(backupInfo.deviceId!!, context, this)
                        }
                    }
                }
            }
        }
    }

    fun initFireblocksSdkForJoinWalletFlow(context: Context) {
        FireblocksManager.getInstance().getLatestDevice(context) { device ->
            if (device == null || device.walletId.isNullOrEmpty()) {
                showError(errorResId = R.string.join_wallet_error_no_wallet) // no source device
            } else {
                val walletId = device.walletId
                val deviceId = Fireblocks.generateDeviceId()
                MultiDeviceManager.instance.addTempDeviceId(deviceId)
                initializeFireblocksSdk(context = context, deviceId = deviceId, viewModel = this, joinWallet = true, walletId = walletId)
            }
        }
    }

    private fun addNewDeviceId(context: Context): String {
        val deviceId = Fireblocks.generateDeviceId()
        StorageManager.get(context, deviceId).apply {
            MultiDeviceManager.instance.addDeviceId(context, deviceId)
        }
        return deviceId
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel, joinWallet: Boolean = false, walletId: String? = null) {
        val fireblocksManager = FireblocksManager.getInstance()
        fireblocksManager.clearTransactions()
        fireblocksManager.setupEnvironmentsAndDevice(context)

        val availableEnvironments = EnvironmentProvider.availableEnvironments()
        val items = hashSetOf<String>()
        availableEnvironments.forEach { environment ->
            items.add(environment.env())
        }
        val defaultEnv = availableEnvironments.firstOrNull {
            it.isDefault()
        }
        if (defaultEnv == null) {
            Timber.e("No default environment found")
            return
        }
        FireblocksManager.getInstance().initEnvironments(context, deviceId, defaultEnv.env())

        fireblocksManager.init(context, viewModel, deviceId = deviceId, forceInit = true, joinWallet = joinWallet, walletId = walletId)
    }
}