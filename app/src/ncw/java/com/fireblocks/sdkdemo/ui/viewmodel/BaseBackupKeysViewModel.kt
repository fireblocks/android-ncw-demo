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
            val walletId = StorageManager.get(context, getDeviceId(context)).walletId.value()
            FireblocksManager.getInstance().getLatestBackupInfo(context, walletId) { backupInfo ->
                if (backupInfo == null) {
                    onError()
                    callback( null)
                } else {
                    callback(backupInfo)
                }
            }
        }.onFailure {
            Timber.e(it)
            onError()
            snackBar.postValue(ObservedData("${it.message}"))
            callback( null)
        }
    }
}