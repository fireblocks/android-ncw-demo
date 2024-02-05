package com.fireblocks.sdkdemo.bl.core

import android.content.Context
import com.fireblocks.sdkdemo.prefs.preferences.MemoryPreference
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.prefs.base.json.SerializablePreference
import com.fireblocks.sdkdemo.prefs.preferences.JsonSerializer
import com.fireblocks.sdkdemo.prefs.preferences.type
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class MultiDeviceManager private constructor() : CoroutineScope {

    private var users: SerializablePreference<HashMap<String, String>>? = null
    private val joinWalletDeviceIdMemoryPref = MemoryPreference("joinWalletDeviceIdMemoryPref", DEVICE, "")


    companion object {
        private const val DEVICE = "DEMO_DEVICE"

        @JvmStatic
        fun initialize(context: Context) {
            instance.users = SerializablePreference(context, DEVICE, JsonSerializer(HashMap<String, String>().type()),"users", hashMapOf() )
        }

        @JvmStatic
        val instance : MultiDeviceManager by lazy { MultiDeviceManager() }
    }

    fun addDeviceId(context: Context, deviceId: String) {
        users?.let { users ->
            val hashMap = users.value()
            val email = SignInUtil.getInstance().getUserData(context)?.email
            email?.let {
                hashMap[it] = deviceId
                users.set(hashMap)
            }
        }
    }

    fun addJoinWalletDeviceId(deviceId: String) {
        joinWalletDeviceIdMemoryPref.set(deviceId)
    }

    fun getJoinWalletDeviceId(): String {
        return joinWalletDeviceIdMemoryPref.valueOrDefault()
    }

    fun clearJoinWalletDeviceId() {
        joinWalletDeviceIdMemoryPref.remove()
    }

    fun allDeviceIds(): ArrayList<String> {
        val hashMap = users?.value()
        return hashMap?.values?.toCollection(ArrayList()) ?: arrayListOf()
    }

    fun lastUsedDeviceId(context: Context): String {
        val email = SignInUtil.getInstance().getUserData(context)?.email
        val hashMap = users?.value()
        val deviceId = hashMap?.get(email)
        return deviceId ?: ""
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