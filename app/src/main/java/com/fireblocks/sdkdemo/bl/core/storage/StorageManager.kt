package com.fireblocks.sdkdemo.bl.core.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.fireblocks.sdkdemo.bl.core.base.fingerprint.FingerPrintPasswordCryptoPreference
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.mpc.MPCSecretPreference
import com.fireblocks.sdkdemo.bl.core.server.HeaderProvider
import com.fireblocks.sdkdemo.bl.fingerprint.FingerPrintErrorHandlerDialogImpl
import com.fireblocks.sdkdemo.log.logDebug
import com.fireblocks.sdkdemo.prefs.base.Preference
import com.fireblocks.sdkdemo.prefs.base.json.PreferenceFactory
import com.fireblocks.sdkdemo.prefs.base.password.PasswordCryptoPreference
import com.fireblocks.sdkdemo.prefs.preferences.StringPreference
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class StorageManager private constructor(val context: Context, val deviceId: String): HeaderProvider {
    override fun toString(): String {
        return "deviceId:${deviceId}"
    }

    override fun deviceId(): String {
        return deviceId
    }

    override fun context(): Context {
        return context
    }

    val passphrase = StringPreference(context, deviceId, "passphrase", "")
    val walletId = StringPreference(context, deviceId, "walletId", "")

    val mpcSecret = MPCSecretPreference(context, deviceId, object : PreferenceFactory {
        override fun getPreference(context: Context, group: String, key: String): PasswordCryptoPreference {

            return FingerPrintPasswordCryptoPreference(context, group, key, FingerPrintErrorHandlerDialogImpl(context))
        }
    })

    fun getKeyId(): String {
        return "MPCSecret-${deviceId}"
    }

    fun clear(): Boolean {
        mpcSecret.remove(getKeyId())
        arrayOf<Preference<*>>(passphrase, walletId).forEach {
            it.reset()
        }
        val deleted = context.deleteSharedPreferences(deviceId)
        logDebug("deleted $deviceId prefs: $deleted")
        return deleted
    }

    fun userState(): String {
        return """
            deviceId:$deviceId
            walletId:${walletId.value()}
        """.trimIndent()
    }

    companion object {
        fun get(context: Context, deviceId: String): StorageManager {
            return instances[deviceId] ?: synchronized(this) {
                instances[deviceId] ?: StorageManager(context.applicationContext, deviceId).also {
                    Timber.d("$deviceId - Created new storage manager")
                    instances[deviceId] = it
                }
            }
        }

        fun getOrNull(deviceId: String): StorageManager? {
            return instances[deviceId]
        }

        fun clear(deviceId: String, completely: Boolean = false) {
            val storageManager = instances.remove(deviceId)
        }

        @VisibleForTesting
        fun set(deviceId: String, storageManager: StorageManager) {
            instances[deviceId] = storageManager
        }

        fun forEach(block: (String, StorageManager) -> Unit) {
            instances.forEach(block)
        }

        fun findAll(condition: (String, StorageManager) -> Boolean): List<StorageManager> {
            val storageManagers = ArrayList<StorageManager>()
            forEach { s, storageManager ->
                if (condition(s, storageManager)) {
                    storageManagers.add(storageManager)
                }
            }
            return storageManagers
        }

        private val instances: HashMap<String, StorageManager> = hashMapOf()
    }

}