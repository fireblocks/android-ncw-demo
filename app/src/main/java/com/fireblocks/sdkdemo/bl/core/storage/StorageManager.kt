package com.fireblocks.sdkdemo.bl.core.storage

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.base.fingerprint.FingerPrintPasswordCryptoPreference
import com.fireblocks.sdkdemo.bl.core.mpc.MPCSecretPreference
import com.fireblocks.sdkdemo.bl.core.server.HeaderProvider
import com.fireblocks.sdkdemo.bl.fingerprint.FingerPrintErrorHandlerDialogImpl
import com.fireblocks.sdkdemo.prefs.base.json.PreferenceFactory
import com.fireblocks.sdkdemo.prefs.base.password.PasswordCryptoPreference
import com.fireblocks.sdkdemo.prefs.preferences.StringPreference
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 18/09/2023
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

    private fun getGroup(): String {
        return "$PREFIX$deviceId"
    }

    val walletId = StringPreference(context, getGroup(), "walletId", "")
    val passphraseId = StringPreference(context, getGroup(), "passphraseId", "")

    val mpcSecret = MPCSecretPreference(context, deviceId, object : PreferenceFactory {
        override fun getPreference(context: Context, group: String, key: String): PasswordCryptoPreference {

            return FingerPrintPasswordCryptoPreference(context, group, key, FingerPrintErrorHandlerDialogImpl(context))
        }
    })

    fun getKeyId(): String {
        return "MPCSecret-${deviceId}"
    }

    fun userState(): String {
        return """
            deviceId:$deviceId
            walletId:${walletId.value()}
        """.trimIndent()
    }

    fun clear() {
        walletId.remove()
        passphraseId.remove()
    }

    companion object {
        const val PREFIX = "DEMO_"

        fun get(context: Context, deviceId: String): StorageManager {
            return instances[deviceId] ?: synchronized(this) {
                instances[deviceId] ?: StorageManager(context.applicationContext, deviceId).also {
                    Timber.d("$deviceId - Created new storage manager")
                    instances[deviceId] = it
                }
            }
        }

        private val instances: HashMap<String, StorageManager> = hashMapOf()
    }
}