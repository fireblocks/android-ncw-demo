package com.fireblocks.sdkdemo.bl.core

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.prefs.base.json.SerializablePreference
import com.fireblocks.sdkdemo.prefs.preferences.BooleanPreference
import com.fireblocks.sdkdemo.prefs.preferences.JsonSerializer
import com.fireblocks.sdkdemo.prefs.preferences.MemoryPreference
import com.fireblocks.sdkdemo.prefs.preferences.StringPreference
import com.fireblocks.sdkdemo.prefs.preferences.type
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class MultiDeviceManager private constructor() : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private var users: SerializablePreference<HashMap<String, String>>? = null
    private val tempDeviceIdMemoryPref = MemoryPreference("tempDeviceIdMemoryPref", DEVICE, "")
    private val tempWalletIdMemoryPref = MemoryPreference("tempWalletIdMemoryPref", DEVICE, "")
    private lateinit var lastSignInProvider: StringPreference
    private lateinit var splashScreenSeen: BooleanPreference


    companion object {
        private const val DEVICE = "DEMO_DEVICE"

        @JvmStatic
        fun initialize(context: Context) {
            instance.users = SerializablePreference(context, DEVICE, JsonSerializer(HashMap<String, String>().type()),"users", hashMapOf() )
            instance.lastSignInProvider = StringPreference(context, DEVICE, "lastSignInProvider", "")
            instance.splashScreenSeen = BooleanPreference(context, DEVICE, "splashScreenSeen", false)
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

    fun addTempDeviceId(deviceId: String) {
        tempDeviceIdMemoryPref.set(deviceId)
    }

    fun getTempDeviceId(): String {
        return tempDeviceIdMemoryPref.valueOrDefault()
    }

    fun clearTempDeviceId() {
        tempDeviceIdMemoryPref.remove()
    }

    fun addTempWalletId(walletId: String) {
        tempWalletIdMemoryPref.set(walletId)
    }

    fun getTempWalletId(): String {
        return tempWalletIdMemoryPref.valueOrDefault()
    }

    fun clearTempWalletId() {
        tempWalletIdMemoryPref.remove()
    }

    fun allDeviceIds(): ArrayList<String> {
        val hashMap = users?.value()
        return hashMap?.values?.toCollection(ArrayList()) ?: arrayListOf()
    }

    fun lastUsedDeviceId(context: Context): String? {
        val email = SignInUtil.getInstance().getUserData(context)?.email
        val hashMap = users?.value()
        val deviceId = hashMap?.get(email)
        return deviceId
    }

    fun deleteLastUsedDevice(context: Context) {
        val email = SignInUtil.getInstance().getUserData(context)?.email
        users?.let { users ->
            val hashMap = users.value()
            hashMap.remove(email)
            users.set(hashMap)
        }
    }

    fun usersStatus(context: Context): String {
        val state: StringBuilder = StringBuilder()
        allDeviceIds().forEachIndexed { index, deviceId ->
            val storageManager = StorageManager.get(context, deviceId)
            state.append("\n#### Device $index ####\n${storageManager.userState()}")
        }
        return state.toString()
    }

    fun setSplashScreenSeen(value: Boolean) {
        splashScreenSeen.set(value)
    }

    fun isSplashScreenSeen(): Boolean {
        return splashScreenSeen.value()
    }

    fun setLastSignInProvider(provider: String) {
        lastSignInProvider.set(provider)
    }

    fun getLastSignInProvider(): String? {
        return lastSignInProvider.value().takeIf { it.isNotEmpty() }
    }

    fun clearLastSignInProvider() {
        lastSignInProvider.remove()
    }
}