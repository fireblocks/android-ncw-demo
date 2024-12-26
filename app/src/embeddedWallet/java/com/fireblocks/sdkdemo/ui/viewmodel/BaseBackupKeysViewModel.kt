package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.ew.bl.core.error.ResponseError
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.BackupInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_NOT_FOUND

/**
 * Created by Fireblocks Ltd. on 01/12/2024.
 */
abstract class BaseBackupKeysViewModel : BaseViewModel() {

    fun getBackupInfo(context: Context, callback: (backupInfo: BackupInfo?) -> Unit) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().getLatestBackup(viewModel = this@BaseBackupKeysViewModel).onSuccess { latestBackupResponse ->
                    showProgress(false)
                    if (latestBackupResponse.keys.isNullOrEmpty()) {
                        showError()
                        callback( null)
                    } else {
                        val backupInfo = BackupInfo(deviceId = latestBackupResponse.keys?.firstOrNull()?.deviceId,
                            createdAt = latestBackupResponse.createdAt,
                            passphraseId = latestBackupResponse.passphraseId,
                            location = PassphraseLocation.GoogleDrive)
                        callback(backupInfo)
                    }
                }.onFailure {
                    Timber.e(it)
                    if (it is ResponseError && it.code == HTTP_NOT_FOUND) {
                        callback(BackupInfo())
                    } else {
                        showError()
                        callback(null)
                    }
                }
            }
        }
    }
}