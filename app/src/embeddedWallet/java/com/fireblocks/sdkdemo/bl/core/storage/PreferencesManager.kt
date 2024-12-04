package com.fireblocks.sdkdemo.bl.core.storage

import android.content.Context
import com.fireblocks.sdk.ew.models.Account
import com.fireblocks.sdk.ew.models.AssetAddress
import com.fireblocks.sdkdemo.prefs.base.json.SerializablePreference
import com.fireblocks.sdkdemo.prefs.preferences.JsonSerializer
import com.fireblocks.sdkdemo.prefs.preferences.type
import com.google.gson.reflect.TypeToken
import timber.log.Timber

class PreferencesManager private constructor(val context: Context, val authClientId: String) {
    companion object {
        private const val PREFIX = "com_fireblocks_sdkdemo_"
        private val instances: HashMap<String, PreferencesManager> = hashMapOf()

        fun get(context: Context, authClientId: String): PreferencesManager {
            return instances[authClientId] ?: synchronized(this) {
                instances[authClientId] ?: PreferencesManager(context, authClientId).also {
                    Timber.d("$authClientId - Created new preferences")
                    instances[authClientId] = it
                }
            }
        }
    }

    fun authClientId(): String {
        return authClientId
    }

    fun context(): Context {
        return context
    }

    private fun getGroup(): String {
        return "$PREFIX$authClientId"
    }

    val assetsAddress = SerializablePreference(context, getGroup(), JsonSerializer(HashMap<String, AssetAddress>().type()), "assetsAddress", HashMap<String, AssetAddress>())
    val account = SerializablePreference<Account?>(context, getGroup(), JsonSerializer(object: TypeToken<Account>() {}.type), "account", null)

    fun clear() {
        Timber.d("$authClientId - Clearing preferences")
        assetsAddress.remove()
        account.remove()
    }
}