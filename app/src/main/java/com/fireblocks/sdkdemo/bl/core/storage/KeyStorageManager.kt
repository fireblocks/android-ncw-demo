package com.fireblocks.sdkdemo.bl.core.storage

import com.fireblocks.sdk.keys.FireblocksKeyStorage

/**
 * Created by Fireblocks Ltd. on 01/02/2023.
 */
object KeyStorageManager {
    private val keyStorageMap = hashMapOf<String, FireblocksKeyStorage>()

    fun setKeyStorage(deviceId: String, keyStorage: FireblocksKeyStorage) {
        keyStorageMap[deviceId] = keyStorage
    }

    fun getKeyStorage(deviceId: String): FireblocksKeyStorage? {
        return keyStorageMap[deviceId]
    }
}