package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.BackupInfo
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 01/12/2024.
 */
abstract class BaseBackupKeysViewModel: BaseViewModel() {

    fun getBackupInfo(context: Context, callback: (backupInfo: BackupInfo?) -> Unit) {
        showProgress(true)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            val deviceId = fireblocksManager.getDeviceId(context)
            val walletId = StorageManager.get(context, deviceId).walletId.value()
            fireblocksManager.getLatestBackupInfo(context, deviceId = deviceId, walletId = walletId) { backupInfo ->
                if (backupInfo == null) {
                    showError()
                    callback( null)
                } else {
                    callback(backupInfo)
                }
            }
        }.onFailure {
            Timber.e(it)
            showError()
            snackBar.postValue(ObservedData("${it.message}"))
            callback( null)
        }
    }
}