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
 * Created by Fireblocks ltd. on 18/09/2023
 */
class MultiDeviceManager private constructor() : CoroutineScope {

    private lateinit var deviceIds: StringSetPreference
    private lateinit var lastUsedDeviceId: StringPreference


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
        deviceIds.addItem(deviceId)
        lastUsedDeviceId.set(deviceId)
    }

    fun removeDeviceId(context: Context, deviceId: String) {
        if (deviceIds.allItems().contains(deviceId)) {
            deviceIds.removeItem(deviceId)
            Timber.i("$deviceId already exists in the system. Clearing.")
            StorageManager.get(context, deviceId).apply {
                removeEnvironment()
                clear()
            }
            StorageManager.clear(deviceId, true)
            if (lastUsedDeviceId() == deviceId) {
                lastUsedDeviceId.reset()
            }
        }
    }

    fun isMultiDevice(): Boolean {
        return deviceIds.value().count() > 1
    }

    fun isSingleDevice(): Boolean {
        return deviceIds.value().count() == 1
    }

    fun clear() {
        deviceIds.remove()
    }

    fun allDeviceIds(): ArrayList<String> {
        return deviceIds.allItems()
    }

    fun lastUsedDeviceId(): String {
        val lastUsedDevice = lastUsedDeviceId.value()
        if (lastUsedDevice.isEmpty() && allDeviceIds().isNotEmpty()) {
            return allDeviceIds().first()
        }
        return lastUsedDeviceId.value()
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