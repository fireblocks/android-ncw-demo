
import android.content.Context
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.adddevice.FireblocksJoinWalletHandler
import com.fireblocks.sdk.adddevice.JoinWalletDescriptor
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksError
import com.fireblocks.sdk.events.FireblocksError.IncompleteBackup
import com.fireblocks.sdk.events.FireblocksError.IncompleteDeviceSetup
import com.fireblocks.sdk.events.FireblocksError.InvalidPhysicalDeviceId
import com.fireblocks.sdk.events.FireblocksError.MaxDevicesPerWalletReached
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.DerivationParams
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyBackup
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyRecovery
import com.fireblocks.sdk.keys.KeyRecoveryStatus
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentInitializer
import com.fireblocks.sdkdemo.bl.core.extensions.getWIFFromPrivateKey
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingTransactionsManager
import com.fireblocks.sdkdemo.bl.core.storage.KeyStorageManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.bl.fingerprint.FireblocksKeyStorageImpl
import com.fireblocks.sdkdemo.ui.events.EventListener
import com.fireblocks.sdkdemo.ui.events.EventWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.Collections.synchronizedSet
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 07/01/2025.
 */
abstract class BaseFireblocksManager: CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var transactionListeners = synchronizedSet(hashSetOf<TransactionListener>())
    private val transactionList: HashSet<TransactionWrapper> = hashSetOf()
    private var counter = 0
    private var eventListeners: HashSet<EventListener> = hashSetOf()
    private val eventList: ArrayList<EventWrapper> = arrayListOf()

    protected var initializedFireblocks = false

    fun <T : Event> getLatestEventErrorByType(eventType: Class<T>): FireblocksError? {
        val uniqueErrors = arrayListOf(InvalidPhysicalDeviceId, IncompleteDeviceSetup, IncompleteBackup, MaxDevicesPerWalletReached)
        val error = eventList.lastOrNull { eventType.isInstance(it.event) }?.event?.error.takeIf { it in uniqueErrors }
        return error
    }

    open fun setupEnvironmentsAndDevice(context: Context) {
        EnvironmentInitializer.initialize(context)
        MultiDeviceManager.initialize(context)
    }

    fun getDeviceId(context: Context): String {
        val deviceId = getTempDeviceId().takeIf { it.isNotEmpty() }
            ?:
            MultiDeviceManager.instance.lastUsedDeviceId(context) ?: ""
        return deviceId
    }

    fun getTempDeviceId() = MultiDeviceManager.instance.getTempDeviceId()

    fun addTempDeviceId(deviceId: String) = MultiDeviceManager.instance.addTempDeviceId(deviceId)

    fun addLatestBackupDeviceId(deviceId: String) = MultiDeviceManager.instance.addLatestBackupDeviceId(deviceId)

    fun getLatestBackupDeviceId() = MultiDeviceManager.instance.getLatestBackupDeviceId()

    fun clearLatestBackupDeviceId() = MultiDeviceManager.instance.clearLatestBackupDeviceId()

    fun clearTempDeviceId() = MultiDeviceManager.instance.clearTempDeviceId()

    fun addTempWalletId(walletId: String) = MultiDeviceManager.instance.addTempWalletId(walletId)

    fun persistTempDeviceId(context: Context) {
        val tempDeviceId = getTempDeviceId()
        if (tempDeviceId.isNotEmpty()) {
            StorageManager.get(context, tempDeviceId).apply {
                MultiDeviceManager.instance.addDeviceId(context, tempDeviceId)
            }
        }
        MultiDeviceManager.instance.clearTempDeviceId()
    }

    open fun updateWalletIdAfterJoinWallet(context: Context, deviceId: String, viewModel: BaseViewModel) {}

    fun hasKeys(context: Context, deviceId: String = getDeviceId(context)): Boolean {
        val status = getKeyCreationStatus(context, deviceId)
        return generatedSuccessfully(status)
    }

    fun getKeyCreationStatus(context: Context, deviceId: String = getDeviceId(context)): Set<KeyDescriptor> {
        var keysStatus: Set<KeyDescriptor> = setOf()
        runCatching {
            val fireblocks = Fireblocks.getInstance(deviceId)
            keysStatus = fireblocks.getKeysStatus()
            if (isDebugLog()) {
                Timber.d("key creation status: $keysStatus")
            }
        }.onFailure {
            Timber.w( "Failed to getKeyCreationStatus: ${it.message}")
        }
        return keysStatus
    }

    private fun generatedSuccessfully(keyDescriptors: Set<KeyDescriptor>): Boolean {
        var generatedKeys = keyDescriptors.isNotEmpty()
        keyDescriptors.forEach {
            if (it.keyStatus != KeyStatus.READY) {
                generatedKeys = false
            }
        }
        return generatedKeys
    }

    fun fireEvent(event: Event) {
        val eventWrapper = EventWrapper(event, counter++, System.currentTimeMillis())
        val eventsCount = addEvent(eventWrapper)
        eventListeners.forEach {
            it.fireEvent(eventWrapper, eventsCount)
        }
    }

    private fun addEvent(eventWrapper: EventWrapper): Int {
        eventList.add(eventWrapper)
        return eventList.count()
    }

    fun addTransactionListener(transactionListener: TransactionListener) {
        synchronized(this) {
            transactionListeners.add(transactionListener)
        }
    }

    fun removeTransactionListener(transactionListener: TransactionListener) {
        synchronized(this) {
            transactionListeners.remove(transactionListener)
        }
    }

    fun fireTransaction(context: Context, transactionWrapper: TransactionWrapper) {
        synchronized(this) {
            val count = addTransaction(transactionWrapper)
            addAdditionalTransactionData(context, transactionWrapper)
            runCatching {
                // use a for loop instead of forEach to avoid ConcurrentModificationException
                transactionListeners.toList().forEach {
                    it.fireTransaction(context, transactionWrapper, count)
                }
            }.onFailure {
                Timber.e(it, "Failed to fireTransaction")
            }
        }
    }

    open fun addAdditionalTransactionData(context: Context, transactionWrapper: TransactionWrapper) {}

    fun updateTransaction(transactionWrapper: TransactionWrapper) {
        addTransaction(transactionWrapper)
    }

    private fun addTransaction(transactionWrapper: TransactionWrapper): Int {
        synchronized(this) {
            runCatching {
                val existingWrapper = transactionList.find { it.id == transactionWrapper.id }
                if (existingWrapper != null) {
                    existingWrapper.transaction = transactionWrapper.transaction
                } else {
                    transactionList.add(transactionWrapper)
                }
                return transactionList.count()
            }.onFailure {
                Timber.e(it, "Failed to remove and add transaction")
            }
            Timber.d("addTransaction finished")
            return 0
        }
    }

    fun getTransactions(context: Context): HashSet<TransactionWrapper> {
        synchronized(this) {
            val deviceId = getDeviceId(context)
            return transactionList.filter { it.deviceId == deviceId }.toHashSet()
        }
    }

    fun clearTransactions() {
        synchronized(this) {
            transactionList.clear()
            transactionListeners.forEach {
                it.clearTransactionsCount()
            }
        }
    }

    fun updateKeyStorageViewModel(deviceId: String, viewModel: BaseViewModel) {
        val keyStorage = KeyStorageManager.getKeyStorage(deviceId) as? FireblocksKeyStorageImpl
        keyStorage?.viewModel = viewModel
    }

    abstract fun startPollingTransactions(context: Context)

    fun stopPollingTransactions(){
        MultiDeviceManager.instance.allDeviceIds().iterator().forEach { deviceId ->
            PollingTransactionsManager.stopPollingTransactions(deviceId)
        }
    }

    /*************************
     * Fireblocks Core methods *
     * ***********************
     */

    /**
     * By default, workspaces are not enabled with EdDSA so you may remove [Algorithm.MPC_EDDSA_ED25519] when calling generateMPCKeys.
     * You may read more about the usage of EdDSA in the following article: [multiple-algorithms](https://ncw-developers.fireblocks.com/docs/multiple-algorithms) if you wish to support EdDSA.
     */
    fun generateMpcKeys(context: Context,
                        algorithms: Set<Algorithm> = setOf(Algorithm.MPC_ECDSA_SECP256K1, Algorithm.MPC_EDDSA_ED25519),
                        callback: (result: Set<KeyDescriptor>) -> Unit) {
        val start = System.currentTimeMillis()
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).generateMPCKeys(algorithms = algorithms) { result ->
            val timeInMillis = System.currentTimeMillis() - start
            Timber.w("Demo The operation 'generateMPCKeys' took $timeInMillis milliseconds")
            Timber.i("generateMPCKeys result: $result")
            callback(result)
            startPollingTransactions(context)
        }
        Timber.i("called generateMPCKeys")
    }

    fun backupKeys(context: Context, passphrase: String, passphraseId: String, callback: ((result: Set<KeyBackup>) -> Unit)) {
        val start = System.currentTimeMillis()
        val deviceId = getDeviceId(context)
        val fireblocks = Fireblocks.getInstance(deviceId)

        fireblocks.backupKeys(passphrase, passphraseId) {
            val timeInMillis = System.currentTimeMillis() - start
            Timber.w("Demo The operation 'backupKeys' took $timeInMillis milliseconds")
            if (isDebugLog()) {
                Timber.d("Backup keys result: $it")
            }
            callback.invoke(it)
        }
    }

    fun recoverKeys(context: Context, passphraseResolver: FireblocksPassphraseResolver, callback: (result: Set<KeyRecovery>) -> Unit) {
        val start = System.currentTimeMillis()
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).recoverKeys(passphraseResolver = passphraseResolver) {
            val timeInMillis = System.currentTimeMillis() - start
            Timber.w("Demo The operation 'recoverKeys' took $timeInMillis milliseconds")
            if (isDebugLog()) {
                Timber.d("Recover keys result: $it")
            }
            callback.invoke(it)
            if (isRecoveredSuccessfully(it)) {
                persistTempDeviceId(context)
                startPollingTransactions(context)
            }
        }
    }

    fun isRecoveredSuccessfully(keyRecoverSet: Set<KeyRecovery>): Boolean {
        val backupError = keyRecoverSet.firstOrNull {
            it.keyRecoveryStatus != KeyRecoveryStatus.SUCCESS
        }
        val success = backupError == null
        return success
    }

    fun takeover(context: Context, callback: (result: Set<FullKey>) -> Unit) {
        val start = System.currentTimeMillis()
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).takeover {
            val timeInMillis = System.currentTimeMillis() - start
            Timber.w("Demo The operation 'takeover' took $timeInMillis milliseconds")
            if (isDebugLog()) {
                Timber.d("takeover keys result: $it")
            }
            callback.invoke(it)
        }
    }

    fun deriveAssetKey(context: Context, extendedPrivateKey: String, bip44DerivationParams: DerivationParams, callback: (KeyData) -> Unit) {
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).deriveAssetKey(extendedPrivateKey = extendedPrivateKey, bip44DerivationParams = bip44DerivationParams) { keyData ->
            if (isDebugLog()) {
                Timber.d("deriveAssetKey result: $keyData")
            }
            callback.invoke(keyData)
        }
    }

    fun requestJoinExistingWallet(joinWalletHandler: FireblocksJoinWalletHandler, callback: (result: Set<KeyDescriptor>) -> Unit) {
        val deviceId = getTempDeviceId()
        if (deviceId.isEmpty()) {
            Timber.e("Failed to requestJoinExistingWallet, temp deviceId is empty")
            callback(setOf())
            return
        }
        Fireblocks.getInstance(deviceId).requestJoinExistingWallet(joinWalletHandler) { result ->
            Timber.i("joinExistingWallet result: $result")
            callback(result)
        }
        Timber.i("called joinExistingWallet")
    }

    fun approveJoinWalletRequest(context: Context, requestId: String, callback: (result: Set<JoinWalletDescriptor>) -> Unit) {
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).approveJoinWalletRequest(requestId) { result ->
            Timber.i("$deviceId - approveJoinWallet result: $result")
            callback(result)
        }
        Timber.i("$deviceId - called approveJoinWallet")
    }

    fun stopJoinWallet(context: Context, requestJoinWalletFlow: Boolean = false) {
        val deviceId = when(requestJoinWalletFlow){
            true -> getTempDeviceId()
            else -> getDeviceId(context)
        }
        Fireblocks.getInstance(deviceId).stopJoinWallet()
        Timber.i("$deviceId - called stopJoinWallet")
    }

    open fun signOut(context: Context, callback: (() -> Unit)? = null) {
        SignInUtil.getInstance().signOut(context) {
            stopPollingTransactions()
            clearTransactions()
            MultiDeviceManager.instance.clearTempDeviceId()
            MultiDeviceManager.instance.clearTempWalletId()
            MultiDeviceManager.instance.setSplashScreenSeen(false)
            callback?.invoke()
        }
    }

    fun getWif(privateKey: String, isMainNet: Boolean = false): String? {
        Timber.d("getWif privateKey: $privateKey")
        var wifSegwit: String? = null

        if (privateKey.isNotEmpty()) {
            val wifBase58Legacy = privateKey.getWIFFromPrivateKey(isMainNet)
            wifSegwit = "p2wpkh:${wifBase58Legacy}"
        }
        Timber.d("getWif wifSegwit: $wifSegwit")
        return wifSegwit
    }
}