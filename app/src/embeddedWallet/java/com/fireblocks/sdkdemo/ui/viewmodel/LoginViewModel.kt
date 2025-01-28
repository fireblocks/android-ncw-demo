package com.fireblocks.sdkdemo.ui.viewmodel

import android.app.Activity.RESULT_OK
import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.ew.bl.core.error.ResponseError
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider
import com.fireblocks.sdkdemo.bl.core.extensions.resultReceiver
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_NOT_FOUND

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class LoginViewModel : BaseLoginViewModel() {

    fun handleSuccessSignIn(context: Context) {
        val multiDeviceManager = MultiDeviceManager.instance
        if (multiDeviceManager.shouldChangeAuthClientId()) {
            val currentAuthClientId = multiDeviceManager.getAuthClientId()
            val resultReceiver = resultReceiver { resultCode, bundle ->
                val authClientId: String
                if (resultCode == RESULT_OK) {
                    authClientId = bundle?.getString(DialogUtil.EDIT_FIELD_TEXT, "") ?: ""
                    multiDeviceManager.setAuthClientId(authClientId)
                    multiDeviceManager.setChangeAuthClientId(false)
                    continueSuccessSignIn(context)
                }
            }
            DialogUtil.getInstance().start("Auth Client Id",
                "Enter authClientId",
                buttonText = context.getString(R.string.OK),
                negativeButtonText = context.getString(R.string.cancel),
                editField = true,
                resultReceiver = resultReceiver,
                inputText = currentAuthClientId)
        } else {
            continueSuccessSignIn(context)
        }
    }

    private fun continueSuccessSignIn(context: Context) {
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
                                onPassedLogin(true)
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

    fun initFireblocksSdkForJoinWalletFlow(context: Context) {
        val deviceId = Fireblocks.generateDeviceId()
        MultiDeviceManager.instance.addTempDeviceId(deviceId)
        FireblocksManager.getInstance().initFireblocks(context, viewModel = this, forceInit = true, startPollingTransactions = false, deviceId = deviceId, joinWallet = true)
    }

    fun initFireblocksSdkForRecoveryFlow(context: Context) {
        val deviceId = MultiDeviceManager.instance.getTempDeviceId()
        initializeFireblocksSdk(context = context, deviceId = deviceId, viewModel = this, recoverWallet = true)
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel, loginFlow: LoginFlow? = null, joinWallet: Boolean = false, recoverWallet: Boolean = false) {
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
        fireblocksManager.init(context, viewModel, true, deviceId = deviceId, loginFlow = loginFlow, joinWallet = joinWallet, recoverWallet = recoverWallet)
    }

    fun onCreateWalletClicked(context: Context) {
        FireblocksManager.getInstance().deleteWallet(context)
        MultiDeviceManager.instance.setChangeAuthClientId(true)
        setLoginFlow(LoginFlow.DELETE_AND_CREATE_NEW_WALLET)
        onPassedLogin(false)
    }
}