package com.fireblocks.sdkdemo.bl.fingerprint

import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.keys.FireblocksKeyStorage
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.log.logDebug
import com.fireblocks.sdkdemo.prefs.base.PasswordError
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks ltd. on 14/03/2023.
 */
class FireblocksKeyStorageImpl(val context: Context, val deviceId: String) : FireblocksKeyStorage, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    internal var viewModel: BaseViewModel? = null

    override fun store(keys: Map<String, ByteArray>, callback: (result: Map<String, Boolean>) -> Unit) {
        Timber.i("$deviceId - begin store data")
        val storageManager = StorageManager.get(context, deviceId)
        val pinCode = deviceId.toCharArray()

        Timber.i("$deviceId - start store")
        val mpcSecretKeys = hashMapOf<String, ByteArray>()
        keys.forEach {
            mpcSecretKeys[it.key] = it.value
        }
        val keysToLoad = hashSetOf<String>()
        Fireblocks.getInstance(deviceId).getKeysStatus().filter { it.keyStatus == KeyStatus.READY }.forEach {
            it.keyId?.let { keyId ->
                if (!mpcSecretKeys.containsKey(keyId)){
                    keysToLoad.add(keyId)
                }
            }
        }
        if (keysToLoad.isNotEmpty()) {
            Timber.i("$deviceId - need to load keys $keysToLoad before we store")
            load(keysToLoad) { loadedKeysMap ->
                loadedKeysMap.forEach {
                    mpcSecretKeys[it.key] = it.value
                }
            }
        }

        suspend fun store(): Boolean {

            val stored = storageManager.mpcSecret.setKeys(mpcSecretKeys, storageManager.getKeyId(), pinCode)
            val canStore = stored.result == true && stored.error == PasswordError.NoError

            if (canStore) {
                Timber.i("$deviceId - Stored ${keys.keys} in main")
                return true
            } else {
                Timber.i("$deviceId - failed to store ${keys.keys} in main")
                if (stored.error == PasswordError.UserCancelled) {
                    Timber.i("$deviceId - user cancelled, try again")
                    viewModel?.let {
                        val shouldTryAgain = it.onFingerprintCancelled(context, false)
                        if (shouldTryAgain) {
                            return store()
                        }
                    }
                }
            }
            return false
        }
        runBlocking {
            withContext(coroutineContext) {
                val stored = store()
                val result: HashMap<String, Boolean> = hashMapOf()
                keys.keys.forEach {
                    result[it] = stored
                }
                callback.invoke(result)
            }
        }
    }

    override fun load(keyIds: Set<String>, callback: (result: Map<String, ByteArray>) -> Unit) {
        Timber.i("$deviceId - begin load data")
        val storageManager = StorageManager.get(context, deviceId)
        val pinCode = deviceId.toCharArray()

        suspend fun load(): HashMap<String, ByteArray>? {
            val loadedDataFromMPCSecret = storageManager.mpcSecret.getKeysOrNull(storageManager.getKeyId(), pinCode)
            val result = loadedDataFromMPCSecret.result
            return if (result != null) {
                logDebug("$deviceId - loaded $keyIds from mpcSecret")
                result
            } else {
                Timber.v("$deviceId - no $keyIds to load from main")
                null
            }
        }
            runBlocking {
                withContext(coroutineContext) {
                    val loaded: HashMap<String, ByteArray>? = load()
                    val result = hashMapOf<String, ByteArray>()
                    if (!loaded.isNullOrEmpty()) {
                        keyIds.forEach {
                            val loadedKey = loaded[it]
                            if (loadedKey != null) {
                                result[it] = loadedKey
                            }
                        }
                    }
                    callback.invoke(result)
                    //TODO make sure I nullify the keys
                }
            }

    }

    override fun remove(keyIds: Set<String>, callback: (result: Map<String, Boolean>) -> Unit) {
        val newKeysToStore = hashMapOf<String, ByteArray>()
        val keysToRemove = hashMapOf<String, ByteArray>()
        load(keyIds) { loadedKeys ->

            loadedKeys.forEach {
                if (keyIds.contains(it.key)) {
                    keysToRemove[it.key] = it.value
                } else {
                    newKeysToStore[it.key] = it.value
                }
            }
            if (newKeysToStore.isEmpty()) {
                val storageManager = StorageManager.get(context, deviceId)
                storageManager.mpcSecret.remove(storageManager.getKeyId())
                val result = hashMapOf<String, Boolean>()
                keyIds.forEach { key ->
                    result[key] = true
                }
                callback.invoke(result)
            } else {
                store(newKeysToStore) {
                    callback.invoke(it)
                }
            }
        }
    }

    override fun contains(keyIds: Set<String>, callback: (result: Map<String, Boolean>) -> Unit) {
        val result = hashMapOf<String, Boolean>()
        load(keyIds) { loadedKeys ->
            keyIds.forEach { keyId ->
                result[keyId] = loadedKeys.containsKey(keyId)
            }
            callback.invoke(result)
        }
    }
}