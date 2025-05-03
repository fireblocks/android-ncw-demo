package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.adddevice.FireblocksJoinWalletHandler
import com.fireblocks.sdk.adddevice.JoinWalletDescriptor
import com.fireblocks.sdk.adddevice.JoinWalletStatus
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksError
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.screens.adddevice.JoinRequestData
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 24/12/2023.
 */
class AddDeviceViewModel: BaseViewModel()  {

    private val _uiState = MutableStateFlow(AddDeviceUiState())
    val uiState: StateFlow<AddDeviceUiState> = _uiState.asStateFlow()
    data class AddDeviceUiState(
        // Approve Join Wallet Request
        val addDeviceSuccess: Boolean = false,
        val joinRequestData: JoinRequestData? = null,
        val approveJoinWalletSuccess: Boolean = false,
        // Join Wallet Request
        val joinedExistingWallet: Boolean = false,
        // Error Screen
        val errorType: AddDeviceErrorType? = null,
        val approveAddDeviceFlow: Boolean = false, // when true we are in the source device, approving the join wallet request. else, we are in the destination device, joining the wallet
    )
    var stopJoinedWalletCalled: Boolean = false

    override fun clean(){
        super.clean()
        _uiState.update { AddDeviceUiState() }
        FireblocksManager.getInstance().clearTempDeviceId()
    }

    override fun showError(throwable: Throwable?, message: String?, resId: Int?, fireblocksError: FireblocksError?) {
        val errorResId = resId ?: R.string.add_device_error_try_again
        super.showError(throwable, message, resId = errorResId, fireblocksError = fireblocksError)
    }

    private fun updateAddDeviceFlow(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                approveAddDeviceFlow = value,
            )
        }
    }

    fun updateErrorType(errorType: AddDeviceErrorType) {
        _uiState.update { currentState ->
            currentState.copy(
                errorType = errorType,
            )
        }
    }

    enum class Platform(val value: String) {
        ANDROID("Android"),
        iOS("iOS"),
        Web("Web"),
        UNKNOWN("Unknown")
    }

    enum class AddDeviceErrorType {
        TIMEOUT,
        CANCELED,
        FAILED,
        QR_GENERATION_FAILED,
    }

    fun joinExistingWallet(context: Context) {
        showProgress(true)
        updateAddDeviceFlow(false)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            val joinWalletHandler: FireblocksJoinWalletHandler = object : FireblocksJoinWalletHandler {
                override fun onRequestId(requestId: String) {
                    showProgress(false)
                    val email = SignInUtil.getInstance().getUserData(context)?.email ?: ""
                    val joinRequestData = JoinRequestData(requestId, Platform.ANDROID, email)
                    updateJoinRequestData(joinRequestData)
                }

                override fun onProvisionerFound() {
                    showProgress(true)
                }
            }

            fireblocksManager.requestJoinExistingWallet(joinWalletHandler) {
                val deviceId = fireblocksManager.getTempDeviceId()
                val generatedSuccessfully = hasKeys(context, deviceId)
                if (generatedSuccessfully){
                    Timber.i("requestJoinExistingWallet succeeded. keys were generated")
                    showProgress(false)
                    fireblocksManager.persistTempDeviceId(context)
                    fireblocksManager.updateWalletIdAfterJoinWallet(context = context, deviceId = deviceId, viewModel = this@AddDeviceViewModel)
                    fireblocksManager.startPollingTransactions(context)
                } else {
                    Timber.w("requestJoinExistingWallet failed. keys were not generated. stopJoinedWalletCalled: $stopJoinedWalletCalled")
                    if (!stopJoinedWalletCalled) {
                        stopJoinedWalletCalled = false
                        fireblocksManager.getLatestEventErrorByType(Event.KeyCreationEvent::class.java)?.let { error ->
                            showError(fireblocksError = error)
                        } ?: showError()
                    }
                }
                onJoinedExitingWallet(generatedSuccessfully)
            }
        }.onFailure {
            Timber.e(it)
            showError()
        }
    }

    private fun onJoinedExitingWallet(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                joinedExistingWallet = value,
            )
        }
    }

    fun approveJoinWalletRequest(context: Context) {
        showProgress(true)
        updateAddDeviceFlow(true)
        runCatching {
            uiState.value.joinRequestData?.requestId?.let { requestId ->
                val fireblocksManager = FireblocksManager.getInstance()
                fireblocksManager.approveJoinWalletRequest(context, requestId) { joinWalletDescriptors ->
                    val approveJoinWalletSuccess = isDeviceApproved(joinWalletDescriptors)
                    when (approveJoinWalletSuccess) {
                        true -> showProgress(false)
                        false -> {
                            fireblocksManager.getLatestEventErrorByType(Event.JoinWalletEvent::class.java)?.let { error ->
                                showError(fireblocksError = error)
                            } ?: showError()
                        }
                    }
                    onApproveJoinWalletSuccess(approveJoinWalletSuccess)
                }
            } ?: {
                showError()
            }
        }.onFailure {
            Timber.e(it)
            showError()
        }
    }

    private fun isDeviceApproved(joinWalletDescriptors: Set<JoinWalletDescriptor>): Boolean {
        if (joinWalletDescriptors.isEmpty()) {
            return false
        }
        var atLeastOneCompleted = false
        var atLeastOneFailed = false
        val failureStatusList = listOf(JoinWalletStatus.STOPPED, JoinWalletStatus.ERROR, JoinWalletStatus.TIMEOUT)
        joinWalletDescriptors.forEach {
            if (it.status == JoinWalletStatus.PROVISION_SETUP_COMPLETED) {
                atLeastOneCompleted = true
            }
            if (failureStatusList.contains(it.status)) {
                atLeastOneFailed = true
            }
        }
        return !atLeastOneFailed && atLeastOneCompleted
    }

    fun onApproveJoinWalletSuccess(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                approveJoinWalletSuccess = value,
            )
        }
    }

    fun updateJoinRequestData(value: JoinRequestData?) {
        _uiState.update { currentState ->
            currentState.copy(
                joinRequestData = value,
            )
        }
    }

    fun stopJoinWallet(context: Context) {
        runCatching {
            FireblocksManager.getInstance().stopJoinWallet(context, !uiState.value.approveAddDeviceFlow)
            stopJoinedWalletCalled = true
            showProgress(false)
        }.onFailure {
            Timber.e(it)
            showProgress(false)
        }
    }
}