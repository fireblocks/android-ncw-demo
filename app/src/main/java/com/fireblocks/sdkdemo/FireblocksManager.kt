package com.fireblocks.sdkdemo

import android.content.Context
import android.widget.Toast
import com.fireblocks.sdk.Environment
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.FireblocksOptions
import com.fireblocks.sdk.Status
import com.fireblocks.sdk.adddevice.JoinWalletDescriptor
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksEventHandler
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.DerivationParams
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyBackup
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyRecovery
import com.fireblocks.sdk.logger.Level
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentInitializer
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.extensions.EXTENDED_PATTERN
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.Api
import com.fireblocks.sdkdemo.bl.core.server.EstimatedFeeRequestBody
import com.fireblocks.sdkdemo.bl.core.server.FireblocksMessageHandlerImpl
import com.fireblocks.sdkdemo.bl.core.server.HeaderProvider
import com.fireblocks.sdkdemo.bl.core.server.JoinWalletBody
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.models.FireblocksDevice
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingTransactionsManager
import com.fireblocks.sdkdemo.bl.core.storage.KeyStorageManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.AssetsSummary
import com.fireblocks.sdkdemo.bl.core.storage.models.BackupInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.EstimatedFeeResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.bl.fingerprint.FireblocksKeyStorageImpl
import com.fireblocks.sdkdemo.ui.events.EventListener
import com.fireblocks.sdkdemo.ui.events.EventWrapper
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.transactions.TransactionListener
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.util.Collections.synchronizedSet
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 06/03/2023.
 */
class FireblocksManager : CoroutineScope {

    private var transactionListeners = synchronizedSet(hashSetOf<TransactionListener>())
    private val transactionList: HashSet<TransactionWrapper> = hashSetOf()
    private var eventListeners: HashSet<EventListener> = hashSetOf()
    private val eventList: ArrayList<EventWrapper> = arrayListOf()
    private var counter = 0

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var initializedFireblocks = false

    companion object {
        private var instance: FireblocksManager? = null
        const val DEFAULT_DEVICE_ID = "default"
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: FireblocksManager().also { instance = it }
                }
    }

    fun setupEnvironmentsAndDevice(context: Context) {
        EnvironmentInitializer.initialize(context)
        MultiDeviceManager.initialize(context)
    }

    fun init(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, joinWallet: Boolean = false, walletId: String? = null) {
        if (MultiDeviceManager.instance.lastUsedDeviceId().isEmpty()) {
            Timber.e("Failed to init, no deviceId")
            viewModel.snackBar.postValue(ObservedData("Failed to init, no deviceId"))
            viewModel.passLogin.postValue(ObservedData(false))
            return
        }
        launch {
            withContext(coroutineContext) {
                viewModel.showProgress(true)
                if (SignInUtil.getInstance().isSignedIn(context)) {
                    val loginSuccess = login(context)
                    Timber.d("loginSuccess: $loginSuccess")
                    if (loginSuccess) {
                        if (joinWallet) {
                            if (walletId.isNullOrEmpty()) {
                                Timber.e("Failed to join wallet, walletId is null or empty")
                                viewModel.snackBar.postValue(ObservedData("Failed to join wallet, walletId is null or empty"))
                                viewModel.passLogin.postValue(ObservedData(false))
                                return@withContext
                            }
                            val joinSuccess = joinWallet(context, walletId, getDeviceId())
                            if (joinSuccess) {
                                initFireblocks(context, viewModel, forceInit, startPollingTransactions = false)
                            } else {
                                Timber.e("Failed to join wallet")
                                viewModel.snackBar.postValue(ObservedData("Failed to join wallet"))
                                viewModel.passLogin.postValue(ObservedData(false))
                                return@withContext
                            }
                        } else {
                            val assignSuccess = assign(context)
                            Timber.d("assignSuccess: $assignSuccess")
                            if (assignSuccess) {
                                initFireblocks(context, viewModel, forceInit)
                            } else {
                                Timber.e("Failed to assign")
                                viewModel.showProgress(false)
                                viewModel.snackBar.postValue(ObservedData("Failed to assign"))
                                viewModel.passLogin.postValue(ObservedData(false))
                            }
                        }
                    } else {
                        Timber.e("Failed to login")
                        SignInUtil.getInstance().signOut(context) {
                            stopPolling()
                        }
                        viewModel.showProgress(false)
                        viewModel.snackBar.postValue(ObservedData("Failed to login"))
                        viewModel.passLogin.postValue(ObservedData(false))
                    }
                } else {
                    Timber.e("Failed to login. there is no signed in user")
                    viewModel.showProgress(false)
                    viewModel.snackBar.postValue(ObservedData("Failed to login"))
                    viewModel.passLogin.postValue(ObservedData(false))
                }
            }
        }
    }

    private fun login(context: Context): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val deviceId = getDeviceId()
                    val response = Api.with(StorageManager.get(context, deviceId)).login().execute()
                    success = response.isSuccessful
                    logResponse("login", response)
                }.onFailure {
                    Timber.e(it, "Failed to call login API")
                }
            }
        }
        return success
    }

    private fun assign(context: Context): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                val deviceId = getDeviceId()
                runCatching {
                    val response = Api.with(StorageManager.get(context, deviceId)).assign(deviceId).execute()
                    success = response.isSuccessful
                    logResponse("assign", response)
                    if (success){
                        response.body()?.walletId?.let {
                            StorageManager.get(context, deviceId).walletId.set(it)
                        }
                    }
                }.onFailure {
                    Timber.e(it, "Failed to call assign API")
                }
            }
        }
        return success
    }

    private fun joinWallet(context: Context, walletId: String, deviceId: String): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = Api.with(StorageManager.get(context, deviceId)).joinWallet(deviceId, JoinWalletBody(walletId)).execute()
                    Timber.d("API response joinWallet $response")
                    Timber.d("API response joinWallet body ${response.body()}")
                    success = response.isSuccessful
                    if (success){
                        StorageManager.get(context, deviceId).walletId.set(walletId)
                    }
                }.onFailure {
                    Timber.e(it, "Failed to call joinWallet API")
                }
            }
        }
        return success
    }

    private fun getDeviceId(): String {
        Timber.d("All deviceIds: ${MultiDeviceManager.instance.allDeviceIds()}")
        val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
        Timber.i("Latest device id: $deviceId")
        return deviceId
    }

    fun getPassphraseLocation(context: Context, passphraseId: String, callback: (PassphraseInfo?) -> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                var passphraseInfo: PassphraseInfo? = null
                runCatching {
                    val deviceId = getDeviceId()
                    val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfo(passphraseId).execute()
                    passphraseInfo = response.body()
                    logResponse("getPassphraseInfo", response)
                }.onFailure {
                    Timber.e(it, "Failed to call getPassphraseInfos API")
                }
                callback.invoke(passphraseInfo)
            }
        }
    }

    private fun initFireblocks(context: Context, viewModel: BaseViewModel, forceInit: Boolean = false, startPollingTransactions: Boolean = true) {
        if (forceInit) {
            initializedFireblocks = false
        }
        val deviceId = getDeviceId()
        val storageManager = StorageManager.get(context, deviceId)
        val env = storageManager.environment().env()
        val environment = Environment.from(env) ?: Environment.DEFAULT
        Timber.i("$deviceId - using environment: $environment according to env: $env")
        val fireblocksOptions = FireblocksOptions.Builder()
            .setLogLevel(Level.DEBUG)
            .setEventHandler(object : FireblocksEventHandler {
                override fun onEvent(event: Event) {
                    if (event.error != null){
                        Timber.e("onEvent - $event")
                    } else {
                        Timber.i("onEvent - $event")
                    }
                    fireEvent(event)
                }
            })
            .setEnv(environment)
            .build()

        val fireblocksSdk = if (initializedFireblocks) {
            Fireblocks.getInstance(deviceId)
        } else {
            val sdk = initialize(context, deviceId, fireblocksOptions, viewModel, storageManager)
            if (sdk != null && startPollingTransactions) {
                Timber.d("$deviceId - startingPolling")
                startPollingTransactions(context, deviceId)
            }
            sdk
        }
        val initializeSuccess = fireblocksSdk != null
        Timber.d("$deviceId - initializeSuccess: $initializeSuccess")

        Timber.d("$deviceId - getCurrentStatus: ${fireblocksSdk?.getCurrentStatus()}")

        if (initializeSuccess) {
            viewModel.passLogin.postValue(ObservedData(true))
        }
        viewModel.showProgress(false)
    }

    fun startPollingTransactions(context: Context, deviceId: String = getDeviceId()) {
        PollingTransactionsManager.startPollingTransactions(context, deviceId, true)
    }

    fun addEventsListener(eventListener: EventListener) {
        Timber.v("addEventsListener $eventListener")
        eventListeners.add(eventListener)
    }

    fun removeEventsListener(eventListener: EventListener) {
        Timber.v("removeEventsListener $eventListener")
        eventListeners.remove(eventListener)
    }

    private fun fireEvent(event: Event) {
        val eventWrapper = EventWrapper(event, counter++, System.currentTimeMillis())
        val eventsCount = addEvent(eventWrapper)
        eventListeners.forEach {
            Timber.v("fireEvent: $eventWrapper")
            it.fireEvent(eventWrapper, eventsCount)
        }
    }

    private fun addEvent(eventWrapper: EventWrapper): Int {
        eventList.add(eventWrapper)
        return eventList.count()
    }

    fun getEvents(): ArrayList<EventWrapper> {
        Timber.v("getEvents: $eventList")
        return eventList
    }

    fun clearEvents() {
        eventList.clear()
        counter = 0
    }

    fun stopPolling(){
        MultiDeviceManager.instance.allDeviceIds().iterator().forEach { deviceId ->
            PollingTransactionsManager.stopPollingTransactions(deviceId)
        }
    }

    fun addTransactionListener(transactionListener: TransactionListener) {
        Timber.v("addTransactionListener $transactionListener")
        synchronized(this) {
            transactionListeners.add(transactionListener)
        }
    }

    fun removeTransactionListener(transactionListener: TransactionListener) {
        Timber.v("removeTransactionListener $transactionListener")
        synchronized(this) {
            transactionListeners.remove(transactionListener)
        }
    }

    fun fireTransaction(transactionWrapper: TransactionWrapper) {
        synchronized(this) {
            val count = addTransaction(transactionWrapper)
            runCatching {
                Timber.v("fireTransaction: $transactionWrapper")
                // use a for loop instead of forEach to avoid ConcurrentModificationException
                for (transactionListener in transactionListeners) {
                    transactionListener.fireTransaction(transactionWrapper, count)
                }
            }.onFailure {
                Timber.e(it, "Failed to fireTransaction")
            }
        }
    }

    fun updateTransaction(transactionWrapper: TransactionWrapper) {
        addTransaction(transactionWrapper)
    }

    private fun addTransaction(transactionWrapper: TransactionWrapper): Int {
        synchronized(this) {
            Timber.d("addTransaction started")
            runCatching {
                val existingWrapper = transactionList.find { it.transaction.id == transactionWrapper.transaction.id }
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

    fun getTransactions(): HashSet<TransactionWrapper> {
        synchronized(this) {
            val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
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

    private fun initialize(context: Context,
                           deviceId: String,
                           fireblocksOptions: FireblocksOptions,
                           viewModel: BaseViewModel,
                           storageManager: StorageManager): Fireblocks? {
        return try {

            val keyStorage = FireblocksKeyStorageImpl(context, deviceId)
            KeyStorageManager.setKeyStorage(deviceId, keyStorage)

            val fireblocks = Fireblocks.initialize(
                context = context,
                deviceId = deviceId,
                messageHandler = FireblocksMessageHandlerImpl(deviceId, storageManager),
                keyStorage = keyStorage,
                fireblocksOptions = fireblocksOptions,
            )
            initializedFireblocks = true
            val message = "Fireblocks SDK Initialized successfully"
            Timber.i("$deviceId - $message")
            viewModel.snackBar.postValue(ObservedData(message))
            fireblocks
        } catch (e: IllegalStateException) {
            Timber.e(e)
            runBlocking(Dispatchers.Main) {
                val message = "Fireblocks SDK was already initialized. Using getInstance instead"
                Timber.i("$deviceId - $message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            Fireblocks.getInstance(deviceId)
        } catch (e: RuntimeException) {
            Timber.e(e, "Failed to initialize Fireblocks")
            runBlocking(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
            return null
        }
    }

    fun initEnvironments(context: Context, deviceId: String, envParameter: String) {
        if (deviceId.isNotNullAndNotEmpty()){
            val env = EnvironmentProvider.availableEnvironments().firstOrNull {
                it.envIndicator().equals(envParameter, ignoreCase = true)
            }

            val defaultEnv = EnvironmentProvider.availableEnvironments().firstOrNull {
                it.isDefault()
            }

            StorageManager.get(context, deviceId).apply {
                when (env) {
                    null -> {
                        defaultEnv?.apply {
                            EnvironmentProvider.getInstance().setEnvironment(context, deviceId, defaultEnv)
                            Timber.i("$deviceId - set environment to default env:${environment().env()}")
                        }
                    }
                    else -> {
                        EnvironmentProvider.getInstance().setEnvironment(context, deviceId, env)
                        Timber.i("set environment to:${environment().env()}")
                    }
                }
            }
        }
    }

    fun generateMpcKeys(algorithms: Set<Algorithm> = setOf(Algorithm.MPC_ECDSA_SECP256K1/*, Algorithm.MPC_EDDSA_ED25519*/),
                        callback: (result: Set<KeyDescriptor>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).generateMPCKeys(algorithms = algorithms) { result ->
            Timber.i("generateMPCKeys result: $result")
            callback(result)
        }
        Timber.i("called generateMPCKeys")
    }

    fun backupKeys(passphrase: String, passphraseId: String, callback: ((result: Set<KeyBackup>) -> Unit)) {
        val deviceId = getDeviceId()
        val fireblocks = Fireblocks.getInstance(deviceId)

        fireblocks.backupKeys(passphrase, passphraseId) {
            Timber.d("Backup keys result: $it")
            callback.invoke(it)
        }
    }

    fun recoverKeys(passphraseResolver: FireblocksPassphraseResolver, callback: (result: Set<KeyRecovery>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).recoverKeys(passphraseResolver = passphraseResolver) {
            Timber.d("Recover keys result: $it")
            callback.invoke(it)
        }
    }

    fun getKeyCreationStatus(context: Context, showDialog: Boolean = false): Set<KeyDescriptor> {
        var keysStatus: Set<KeyDescriptor> = setOf()
        runCatching {
            val deviceId = getDeviceId()
            val fireblocks = Fireblocks.getInstance(deviceId)
            keysStatus = fireblocks.getKeysStatus()
            Timber.d("key creation status: $keysStatus")
            if (showDialog) {
                DialogUtil.getInstance().start("Key creation status", "keys: $keysStatus", buttonText = context.getString(R.string.OK))
            }
        }.onFailure {
           Timber.e(it, "Failed to getKeyCreationStatus")
        }
        return keysStatus
    }

    fun deleteKeys(context: Context) {
        val deviceId = getDeviceId()
        val fireblocks = Fireblocks.getInstance(deviceId)
        val keysStatus: Set<KeyDescriptor> = fireblocks.getKeysStatus()
        val keyStorageImpl = KeyStorageManager.getKeyStorage(deviceId)

        if (keyStorageImpl == null) {
            DialogUtil.getInstance().start("Delete keys", "No keyStorageImpl", buttonText = context.getString(R.string.OK))
        } else if (keysStatus.isEmpty()) {
            DialogUtil.getInstance().start("Delete keys", "No keys to delete", buttonText = context.getString(R.string.OK))
        } else {
            val keyIds = hashSetOf<String>()
            keysStatus.forEach { keyCreation ->
                keyCreation.keyId?.let { keyId ->
                    keyIds.add(keyId)
                 }
            }
            launch {
                keyStorageImpl.remove(keyIds) {
                    Timber.d("Remove keys result: $it")
                    runBlocking(Dispatchers.Main) {
                        DialogUtil.getInstance().start("Remove keys result", "removed keys:\n $it", buttonText = context.getString(R.string.OK))
                    }
                }
            }
        }
    }

    fun getDeviceStatus(): Status {
        return Fireblocks.getInstance(getDeviceId()).getCurrentStatus()
    }

    fun cancelTransaction(context: Context, deviceId: String, txId: String): Boolean {
        return PollingTransactionsManager.cancelTransaction(context, deviceId, txId)
    }

    fun createTransaction(context: Context, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel, callback: (response: CreateTransactionResponse?) -> Unit){
        launch {
            val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
            val response = PollingTransactionsManager.createTransaction(context, deviceId, assetId, destAddress, amount, feeLevel)
            callback(response)
        }
    }

    fun getEstimatedFee(context: Context, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel? = null, callback: (response: EstimatedFeeResponse?) -> Unit){
        launch {
            val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
            var estimatedFeeResponse: EstimatedFeeResponse? = null
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val response = Api.with(StorageManager.get(context, deviceId)).getEstimatedFee(
                            deviceId = deviceId,
                            body = EstimatedFeeRequestBody(
                                assetId = assetId,
                                destAddress = destAddress,
                                amount = amount,
                                feeLevel = feeLevel
                            )
                        ).execute()
                        logResponse("getEstimatedFee", response)
                        estimatedFeeResponse = response.body()
                    }.onFailure {
                        Timber.e(it, "Failed to call getEstimatedFee API")
                    }
                }
            }
            callback(estimatedFeeResponse)
        }
    }

    fun getAllTransactionsFromServer(context: Context) {
        val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
        PollingTransactionsManager.getAllTransactionsFromServer(context, deviceId)
    }

    fun createAsset(context: Context, assetId: String, callback: (success: Boolean) -> Unit) {
        Timber.i("creating $assetId asset")
        var success = false
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val deviceId = getDeviceId()
                        val response = Api.with(StorageManager.get(context, deviceId)).createAsset(deviceId, assetId).execute()
                        logResponse("createAsset", response)
                        success = response.isSuccessful
                    }.onFailure {
                        Timber.e(it, "Failed to call createAsset API")
                    }
                    callback(success)
                }
            }
        }
    }

    fun getAssetsSummary(context: Context, callback: ((result: List<SupportedAsset>) -> Unit)) {
        val assets: ArrayList<SupportedAsset> = arrayListOf()
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val deviceId = getDeviceId()
                        val response = Api.with(StorageManager.get(context, deviceId)).getAssetsSummary(deviceId).execute()
                        logResponse("getAssetsSummary", response)
                        val assetsSummaryMap: Map<String, AssetsSummary>? = response.body()
                        assetsSummaryMap?.forEach { (_, summary) ->
                            summary.asset?.let { asset ->
                                asset.fee?.apply {
                                    low?.feeLevel = FeeLevel.LOW
                                    medium?.feeLevel = FeeLevel.MEDIUM
                                    high?.feeLevel = FeeLevel.HIGH
                                }
                                summary.balance?.let {  assetBalance ->
                                    asset.balance = assetBalance.total.toDouble().roundToDecimalFormat(EXTENDED_PATTERN)
                                    val price = (asset.balance.toDouble() * asset.rate).roundToDecimalFormat()
                                    asset.price = price
                                }
                                summary.address?.let { assetAddress ->
                                    asset.assetAddress = assetAddress
                                }
                                assets.add(asset)
                            }
                        }
                    }.onFailure {
                        Timber.e(it, "Failed to call getAssetsSummary API")
                    }
                }
            }
            callback(assets)
        }
    }

    fun getSupportedAssets(context: Context, callback: ((result: List<SupportedAsset>) -> Unit)) {
        var supportedAssets: List<SupportedAsset>? = null
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val deviceId = getDeviceId()
                        val response = Api.with(StorageManager.get(context, deviceId)).getSupportedAssets(deviceId).execute()
                        logResponse("getSupportedAssets", response)
                        supportedAssets = response.body()
                    }.onFailure {
                        Timber.e(it, "Failed to call getSupportedAssets API")
                    }
                }
            }
            callback(supportedAssets?: listOf())
        }
    }

    fun getLatestBackupInfo(context: Context,  walletId: String, useDefaultEnv: Boolean = false, callback: ((result: BackupInfo?) -> Unit)) {
        var backupInfo: BackupInfo? = null
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val headerProvider = when(useDefaultEnv) {
                            true -> getDefaultHeaderProvider(context)
                            else -> StorageManager.get(context, getDeviceId())
                        }
                        val response = Api.with(headerProvider).getLatestBackupInfo(walletId).execute()
                        logResponse("getLatestBackupInfo", response)
                        backupInfo = response.body()
                    }.onFailure {
                        Timber.e(it, "Failed to call getBackupInfo API")
                    }
                }
            }
            callback(backupInfo)
        }
    }

    private fun getDefaultHeaderProvider(context: Context): HeaderProvider {
        return object : HeaderProvider {
            override fun context(): Context {
                return context
            }

            override fun deviceId(): String {
                return DEFAULT_DEVICE_ID
            }
        }
    }


    fun getLatestDevice(context: Context, callback: (FireblocksDevice?) -> Unit) {
        var device: FireblocksDevice? = null
        launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val availableEnvironments = EnvironmentProvider.availableEnvironments()
                    val items = hashSetOf<String>()
                    availableEnvironments.forEach { environment ->
                        items.add(environment.env())
                    }
                    val defaultEnv = availableEnvironments.firstOrNull {
                        it.isDefault()
                    }
                    defaultEnv?.let {
                        initEnvironments(context, DEFAULT_DEVICE_ID, defaultEnv.env())
                        val response = Api.with(getDefaultHeaderProvider(context)).getDevices().execute()
                        logResponse("getDevices", response)

                        device = response.body()?.let {
                            if (it.devices?.isNotEmpty() == true) {
                                it.devices.last()
                            } else {
                                null
                            }
                        }
                    }
                }.onFailure {
                    Timber.w(it, "Failed to call getDevices API")
                }
                callback.invoke(device)
            }
        }
    }

    fun getOrCreatePassphraseId(context: Context, passphraseLocation: PassphraseLocation, callback: (String?) -> Unit){
        launch {
            withContext(Dispatchers.IO) {
                getPassphraseId(context) { passphraseId ->
                    if (passphraseId.isNullOrEmpty()) {
                        val generatedPassphraseId  = Fireblocks.generatePassphraseId()
                        createPassphraseInfo(context, generatedPassphraseId, passphraseLocation) { success ->
                            if (success) {
                                callback(generatedPassphraseId)
                            } else {
                                Timber.e("Failed to createPassphraseInfo")
                                callback(null)
                            }
                        }
                    } else {
                        callback(passphraseId)
                    }
                }
            }
        }
    }

    private fun getPassphraseId(context: Context, callback: (String?) -> Unit) {
        var passphraseId: String? = null
        runCatching {
            val deviceId = getDeviceId()
            val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfos().execute()
            logResponse("getPassphraseInfos", response)
            passphraseId = response.body()?.let { passphraseInfos ->
                if (!passphraseInfos.passphrases.isNullOrEmpty()) {
                    passphraseInfos.passphrases.last().passphraseId
                } else {
                    null
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to call getPassphraseInfos API")
        }
        callback.invoke(passphraseId)
    }

    private fun createPassphraseInfo(context: Context, passphraseId: String, passphraseLocation: PassphraseLocation, callback: (success: Boolean) -> Unit) {
        var success = false
        runCatching {
            val deviceId = getDeviceId()
            val response = Api.with(StorageManager.get(context, deviceId)).createPassphraseInfo(passphraseId, body = PassphraseInfo(location = passphraseLocation)).execute()
            success = response.isSuccessful
            logResponse("createPassphraseInfo", response)
        }.onFailure {
            Timber.w(it, "Failed to call createPassphraseInfo API")
        }
        callback.invoke(success)
    }

    private fun logResponse(apiName: String, response: Response<*>) {
        Timber.d("got response from $apiName rest API code:${response.code()}, isSuccessful:${response.isSuccessful} body: ${response.body()}")
    }

    fun takeover(callback: (result: Set<FullKey>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).takeover {
            Timber.d("takeover keys result: $it")
            callback.invoke(it)
        }
    }

    fun deriveAssetKey(extendedPrivateKey: String, bip44DerivationParams: DerivationParams, callback: (KeyData) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).deriveAssetKey(extendedPrivateKey = extendedPrivateKey, bip44DerivationParams = bip44DerivationParams) { keyData ->
            Timber.d("deriveAssetKey result: $keyData")
            callback.invoke(keyData)
        }
    }

    fun requestJoinExistingWallet(callback: (result: Set<KeyDescriptor>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).requestJoinExistingWallet { result ->
            Timber.i("joinExistingWallet result: $result")
            callback(result)
        }
        Timber.i("called joinExistingWallet")
    }

    fun approveJoinWalletRequest(requestId: String, callback: (result: Set<JoinWalletDescriptor>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).approveJoinWalletRequest(requestId) { result ->
            Timber.i("approveJoinWallet result: $result")
            callback(result)
        }
        Timber.i("called approveJoinWallet")
    }

    fun stopJoinWallet() {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).stopJoinWallet()
        Timber.i("called stopJoinWallet")
    }
}