package com.fireblocks.sdkdemo.bl.core.mpc

import android.content.Context
import com.fireblocks.sdkdemo.prefs.base.PasswordPreferenceResult
import com.fireblocks.sdkdemo.prefs.base.json.PreferenceFactory
import com.fireblocks.sdkdemo.prefs.base.json.SerializablePasswordCryptoPreference
import com.fireblocks.sdkdemo.prefs.preferences.JsonSerializer
import com.fireblocks.sdkdemo.prefs.preferences.type

/**
 * Created by Fireblocks ltd. on 2019-12-29
 */

class MPCSecretPreference(context: Context, group: String, prefFactory: PreferenceFactory) {

    private val pref = SerializablePasswordCryptoPreference(context,
            group,
            JsonSerializer(HashMap<String, ByteArray>().type()),
            prefFactory,
            "MPCSecret",
        HashMap<String, ByteArray>())

    suspend fun setKeys(keys: HashMap<String, ByteArray>, keyId: String, pinCode: CharArray): PasswordPreferenceResult<Boolean> {
        return pref.setSync(keys, keyId, pinCode)
    }

    suspend fun getKeys(keyId: String, pinCode: CharArray): PasswordPreferenceResult<HashMap<String, ByteArray>> {
        return getKeysOrNull(keyId, pinCode)
    }

    suspend fun getKeysOrNull(keyId: String, pinCode: CharArray): PasswordPreferenceResult<HashMap<String, ByteArray>> {
        return pref.valueSync(keyId, pinCode)
    }

    fun remove(keyId: String) {
        pref.remove(keyId)
    }

    fun containsKey(keyId: String): Boolean {
        return pref.containsKey(keyId)
    }

}