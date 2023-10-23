package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.FireblocksManager
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
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class LoginViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    data class LoginUiState(
        val signInSelected: Boolean = true,
        val showSnackbar: Boolean = false,
        val snackbarText: String = "",
        val signInState: SignInState = SignInState(),
    )

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

    fun setSignInSelected(selected: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                signInSelected = selected,
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

    fun handleSuccessSignIn(signInFlow: Boolean, context: Context, viewModel: LoginViewModel) {
        var deviceId : String?
        if (signInFlow) {
            FireblocksManager.getInstance().getLatestDeviceId(context) {
                deviceId = it
                if (deviceId.isNullOrEmpty()) {
                    deviceId = Fireblocks.generateDeviceId()
                }
                initializeFireblocksSdk(deviceId!!, context, viewModel)
            }
        } else {
            deviceId = Fireblocks.generateDeviceId()
            initializeFireblocksSdk(deviceId!!, context, viewModel)
        }
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel) {
        if (deviceId.isNotEmpty()) {
            Timber.d("before My All deviceIds: ${MultiDeviceManager.instance.allDeviceIds()}")
            StorageManager.get(context, deviceId).apply {
                MultiDeviceManager.instance.addDeviceId(deviceId)
            }
            Timber.d("after My All deviceIds: ${MultiDeviceManager.instance.allDeviceIds()}")
        }
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

        fireblocksManager.init(context, viewModel, true)
    }
}