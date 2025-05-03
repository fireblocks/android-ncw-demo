package com.fireblocks.sdkdemo.bl.core.base.fingerprint

import android.content.Context
import com.fireblocks.sdkdemo.prefs.base.PasswordPreferenceResult
import com.fireblocks.sdkdemo.prefs.base.Preference
import com.fireblocks.sdkdemo.prefs.base.password.PasswordCryptoPreference

/**
 * Created by Fireblocks Ltd. on 2019-12-29
 */
class FingerPrintPasswordCryptoPreference(private val context: Context,
                                          private val group: String,
                                          key: String,
                                          private val errorHandler: FingerPrintErrorHandler = FingerPrintErrorHandler.EMPTY) :
        PasswordCryptoPreference(context, group, key, {}) {

    override fun getPart2(keyId: String): Preference<ByteArray> {
        return FingerPrintPreference(context, group, "part2-$keyId", errorHandler = errorHandler)
    }

    override suspend fun setSync(value: ByteArray,
                                 keyId: String,
                                 password: CharArray,
                                 salt: ByteArray,
                                 iv: ByteArray?): PasswordPreferenceResult<Boolean> {
        return super.setSync(value, keyId, password, salt, iv)
    }

}