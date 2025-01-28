package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider

/**
 * Created by Fireblocks Ltd. on 03/07/2023.
 */
class LoginViewModel : BaseLoginViewModel() {

    fun handleSuccessSignIn(context: Context) {
        val fireblocksManager = FireblocksManager.getInstance()
        if (uiState.value.loginFlow == LoginFlow.DELETE_AND_CREATE_NEW_WALLET){
            deleteAndCreateNewWallet(context)
        } else {
            val lastUsedDeviceId = getDeviceId(context)
            if (lastUsedDeviceId.isNotEmpty()) {
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
                            val deviceId = backupInfo?.deviceId
                            if (backupInfo == null || deviceId.isNullOrEmpty()) {
                                onError(context, resId = R.string.sign_in_error_no_backup) // no previous backup for this deviceId
                            } else {
                                fireblocksManager.addTempDeviceId(deviceId)
                                fireblocksManager.addTempWalletId(device.walletId)
                                onPassedLogin(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deleteAndCreateNewWallet(context: Context) {
        FireblocksManager.getInstance().deleteWallet(context)
        setLoginFlow(LoginFlow.SIGN_UP)
        val deviceId = addNewDeviceId(context)
        initializeFireblocksSdk(deviceId, context, this@LoginViewModel)
    }

    fun initFireblocksSdkForJoinWalletFlow(context: Context) {
        val walletId = MultiDeviceManager.instance.getTempWalletId()
        if (walletId.isEmpty()) {
            showError(resId = R.string.join_wallet_error_no_wallet) // no source device
        } else {
            val deviceId = Fireblocks.generateDeviceId()
            MultiDeviceManager.instance.addTempDeviceId(deviceId)
            initializeFireblocksSdk(context = context, deviceId = deviceId, viewModel = this, joinWallet = true, walletId = walletId)
        }
    }

    fun initFireblocksSdkForRecoveryFlow(context: Context) {
        val deviceId = MultiDeviceManager.instance.getTempDeviceId()
        initializeFireblocksSdk(context = context, deviceId = deviceId, viewModel = this, joinWallet = false, recoverWallet = true)
    }

    private fun initializeFireblocksSdk(deviceId: String, context: Context, viewModel: LoginViewModel, joinWallet: Boolean = false, recoverWallet: Boolean = false, walletId: String? = null) {
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
            onError(context, message = "No default environment found")
            return
        }
        FireblocksManager.getInstance().initEnvironments(context, deviceId, defaultEnv.env())

        fireblocksManager.init(context, viewModel, deviceId = deviceId, forceInit = true, joinWallet = joinWallet, recoverWallet = recoverWallet, walletId = walletId)
    }

    fun onCreateWalletClicked(context: Context) {
        setLoginFlow(LoginFlow.DELETE_AND_CREATE_NEW_WALLET)
        onPassedLogin(false)
    }
}