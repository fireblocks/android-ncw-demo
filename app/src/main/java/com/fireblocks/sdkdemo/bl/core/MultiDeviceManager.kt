package com.fireblocks.sdkdemo.bl.core

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.environment.removeEnvironment
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.prefs.base.StringSetPreference
import com.fireblocks.sdkdemo.prefs.preferences.StringPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class MultiDeviceManager private constructor() : CoroutineScope {

    private var deviceIds: StringSetPreference? = null
    private var lastUsedDeviceId: StringPreference? = null


    companion object {
        private const val DEVICE = "DEVICE"

        @JvmStatic
        fun initialize(context: Context) {
            instance.deviceIds = StringSetPreference(context, DEVICE, "deviceIds")
            instance.lastUsedDeviceId = StringPreference(context, DEVICE, "lastUsedDeviceId", "")
        }

        @JvmStatic
        val instance : MultiDeviceManager by lazy { MultiDeviceManager() }

    }

    fun addDeviceId(deviceId: String) {
        deviceIds?.addItem(deviceId)
        lastUsedDeviceId?.set(deviceId)
    }

    fun allDeviceIds(): ArrayList<String> {
        return deviceIds?.allItems() ?: arrayListOf()
    }

    fun lastUsedDeviceId(): String {
        val lastUsedDevice = lastUsedDeviceId?.value()
        if (lastUsedDevice.isNullOrEmpty() && allDeviceIds().isNotEmpty()) {
            return allDeviceIds().first()
        }
        return lastUsedDeviceId?.value() ?: ""
    }

    fun usersStatus(context: Context): String {
        val state: StringBuilder = StringBuilder()
        allDeviceIds().forEachIndexed { index, deviceId ->
            val storageManager = StorageManager.get(context, deviceId)
            state.append("\n#### Device $index ####\n${storageManager.userState()}")
        }
        return state.toString()
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO
}