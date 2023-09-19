package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdkdemo.ui.main.BaseViewModel

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
class SettingsViewModel: BaseViewModel() {
    fun shareLogs(context: Context) {
        emailAllLogs(context)
    }
}