package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.adddevice.JoinWalletDescriptor
import com.fireblocks.sdk.adddevice.JoinWalletStatus
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.events.EventListener
import com.fireblocks.sdkdemo.ui.events.EventWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.screens.adddevice.JoinRequestData
import com.fireblocks.sdkdemo.ui.screens.generatedSuccessfully
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
        val errorResId: Int = R.string.add_device_error_try_again,
        val addDeviceSuccess: Boolean = false,
        val joinRequestData: JoinRequestData? = null,
        val approveJoinWalletSuccess: Boolean = false,
        val approvedJoinWalletResult: Set<JoinWalletDescriptor>? = null,
        // Join Wallet Request
        val joinedExistingWallet: Boolean = false,
        // Error Screen
        val errorType: AddDeviceErrorType? = null,
        val addDeviceFlow: Boolean = false,
    )

    override fun clean(){
        super.clean()
        _uiState.update { AddDeviceUiState() }
    }

    fun showError(errorResId: Int? = uiState.value.errorResId) {
        updateErrorResId(errorResId ?:  R.string.add_device_error_try_again)
        super.showError()
    }

    private fun updateErrorResId(errorResId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                errorResId = errorResId,
            )
        }
    }

    private fun updateAddDeviceFlow(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                addDeviceFlow = value,
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
        IOS("iOS"),
        WEB("Web"),
        UNKNOWN("Unknown")
    }

    enum class AddDeviceErrorType {
        TIMEOUT,
        CANCELED,
        QR_GENERATION_FAILED,
    }

    fun joinExistingWallet(context: Context) {
        showProgress(true)
        updateAddDeviceFlow(false)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            val eventListener = object : EventListener {
                override fun fireEvent(event: EventWrapper, count: Int) {
                    Timber.v("ModelFireEvent $event")
                    val joinWalletEvent = event.event
                    if (joinWalletEvent is Event.JoinWalletEvent && joinWalletEvent.joinWalletDescriptor?.status == JoinWalletStatus.ADD_DEVICE_SETUP_REQUESTED) {
                        showProgress(false)
                        joinWalletEvent.joinWalletDescriptor?.requestId?.let { requestId ->
                            val email = SignInUtil.getInstance().getUserData(context)?.email ?: ""
                            val joinRequestData = JoinRequestData(requestId, Platform.ANDROID, email)
                            updateJoinRequestData(joinRequestData)
                        }
                    } else if (joinWalletEvent is Event.JoinWalletEvent && joinWalletEvent.joinWalletDescriptor?.status == JoinWalletStatus.PROVISIONER_FOUND) {
                        showProgress(true)
                    }
                }

                override fun clearEventsCount() {}
            }
            fireblocksManager.addEventsListener(eventListener)

            fireblocksManager.requestJoinExistingWallet(context) {
                fireblocksManager.removeEventsListener(eventListener)
                val generatedSuccessfully = generatedSuccessfully(context)
                if (generatedSuccessfully){
                    showProgress(false)
                    fireblocksManager.startPollingTransactions(context)
                } else {
                    showError()
                }
                onJoinedExitingWallet(generatedSuccessfully)
            }
        }.onFailure {
            Timber.e(it)
            snackBar.postValue(ObservedData("${it.message}"))
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
                FireblocksManager.getInstance().approveJoinWalletRequest(context, requestId) { joinWalletDescriptors ->
                    val approveJoinWalletSuccess = isDeviceApproved(joinWalletDescriptors)
                    when (approveJoinWalletSuccess) {
                        true -> showProgress(false)
                        false -> showError()
                    }
                    onApproveJoinWalletRequest(joinWalletDescriptors)
                    onApproveJoinWalletSuccess(approveJoinWalletSuccess)
                }
            } ?: {
                onError(true)
            }
        }.onFailure {
            Timber.e(it)
            onError(true)
        }
    }

    private fun isDeviceApproved(joinWalletDescriptors: Set<JoinWalletDescriptor>): Boolean {
        var deviceApproved =  joinWalletDescriptors.isNotEmpty()
        joinWalletDescriptors.forEach {
            if (it.status != JoinWalletStatus.PROVISION_SETUP_COMPLETED) {
                deviceApproved = false
            }
        }
        return deviceApproved
    }

    private fun onApproveJoinWalletRequest(value: Set<JoinWalletDescriptor>) {
        _uiState.update { currentState ->
            currentState.copy(
                approvedJoinWalletResult = value,
            )
        }
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
            FireblocksManager.getInstance().stopJoinWallet(context)
            showProgress(false)
        }.onFailure {
            Timber.e(it)
            snackBar.postValue(ObservedData("${it.message}"))
            showProgress(false)
        }
    }
}