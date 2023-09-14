package com.fireblocks.sdkdemo

import android.content.Context
import android.widget.Toast
import com.fireblocks.sdk.Environment
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.FireblocksOptions
import com.fireblocks.sdk.Status
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksEventHandler
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyBackup
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyRecovery
import com.fireblocks.sdk.logger.Level
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
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingMessagesManager
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingTransactionsManager
import com.fireblocks.sdkdemo.bl.core.storage.KeyStorageManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.EstimatedFeeResponse
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 06/03/2023.
 */
class FireblocksManager : CoroutineScope {

    private var transactionListeners: HashSet<TransactionListener> = hashSetOf()
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
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: FireblocksManager().also { instance = it }
                }
    }

    fun setupEnvironmentsAndDevice(context: Context) {
        EnvironmentInitializer.initialize(context)
        MultiDeviceManager.initialize(context)
    }

    fun init(context: Context, viewModel: BaseViewModel, forceInit: Boolean = false) {
        if (MultiDeviceManager.instance.lastUsedDeviceId().isEmpty()){
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
                        } else {
                            Timber.e("Failed to login")
                            SignInUtil.getInstance().signOut(context){}
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
                    Timber.d("API response login $response")
                    success = response.isSuccessful
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
                    Timber.d("API response assign $response")
                    Timber.d("API response assign body ${response.body()}")
                    success = response.isSuccessful
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

    private fun getDeviceId(): String {
        Timber.d("All deviceIds: ${MultiDeviceManager.instance.allDeviceIds()}")
        val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
        Timber.i("Latest device id: $deviceId")
        return deviceId
    }

    fun getPassphrase(context: Context, deviceId: String = getDeviceId()): String {
        var passphrase = StorageManager.get(context, deviceId).passphrase.value()
        if (passphrase.isEmpty()) {
            passphrase = Fireblocks.generateRandomPassphrase()
            StorageManager.get(context, deviceId).passphrase.set(passphrase)
        }
        return passphrase
    }

    private fun initFireblocks(context: Context, viewModel: BaseViewModel, forceInit: Boolean = false) {
        if (forceInit) {
            initializedFireblocks = false
        }
        val deviceId = getDeviceId()
        val env = StorageManager.get(context, deviceId).environment().env()
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
            val sdk = initialize(context, deviceId, fireblocksOptions, viewModel)
            if (sdk != null) {
                Timber.d("startingPolling")
                PollingMessagesManager.startPollingMessages(context, deviceId)
                PollingTransactionsManager.startPolling(context, deviceId, true)
            }
            sdk
        }
        Timber.d("initializeSuccess: true")

        Timber.d("${fireblocksSdk?.getCurrentStatus()}")

        viewModel.passLogin.postValue(ObservedData(true))
        viewModel.showProgress(false)
    }

    fun addEventsListener(eventListener: EventListener) {
        Timber.v("addEventsListener $eventListener")
        eventListeners.add(eventListener)
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
                transactionListeners.forEach {
                    Timber.v("fireTransaction: $transactionWrapper")
                    it.fireTransaction(transactionWrapper, count)
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
            Timber.i("addTransaction started")
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
            Timber.i("addTransaction finished")
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

    private fun initialize(context: Context, deviceId: String, fireblocksOptions: FireblocksOptions, viewModel: BaseViewModel): Fireblocks? {
        return try {

            val keyStorage = FireblocksKeyStorageImpl(context, deviceId)
            KeyStorageManager.setKeyStorage(deviceId, keyStorage)

            val fireblocks = Fireblocks.initialize(
                context = context,
                deviceId = deviceId,
                messageHandler = FireblocksMessageHandlerImpl(context, deviceId),
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

    fun backupKeys(passphrase: String? = null, callback: ((result: Set<KeyBackup>) -> Unit)? = null) {
        val deviceId = getDeviceId()
        val fireblocks = Fireblocks.getInstance(deviceId)
        if (passphrase.isNullOrEmpty()){
            throw Exception("passphrase is empty")
        }
        fireblocks.backupKeys(passphrase) {
            Timber.d("Backup keys result: $it")
            callback?.invoke(it)
        }
    }

    fun recoverKeys(context: Context, passphrase: String? = null, callback: ((result: Set<KeyRecovery>) -> Unit)) {
        val deviceId = getDeviceId()

        val validPassphrase = passphrase ?: getPassphrase(context, deviceId)
        Fireblocks.getInstance(deviceId).recoverKeys(validPassphrase) {
            Timber.d("Recover keys result: $it")
            callback.invoke(it)
        }
    }

    fun getKeyCreationStatus(context: Context, showDialog: Boolean = false): Set<KeyDescriptor> {
        val deviceId = getDeviceId()
        val fireblocks = Fireblocks.getInstance(deviceId)
        val keysStatus: Set<KeyDescriptor> = fireblocks.getKeysStatus()
        Timber.d("key creation status: $keysStatus")
        if (showDialog) {
            DialogUtil.getInstance().start("Key creation status", "keys: $keysStatus", buttonText = context.getString(R.string.OK))
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
            var estimatedFeeResponse: EstimatedFeeResponse?
            runBlocking {
                withContext(Dispatchers.IO) {
                    val response = Api.with(StorageManager.get(context, deviceId)).getEstimatedFee(
                        deviceId = deviceId,
                        body = EstimatedFeeRequestBody(
                            assetId = assetId,
                            destAddress = destAddress,
                            amount = amount,
                            feeLevel = feeLevel
                        )
                    ).execute()
                    Timber.d("got response from getEstimatedFee rest API code:${response.code()}, isSuccessful:${response.isSuccessful} response.body(): ${response.body()}",
                        response)
                    estimatedFeeResponse = response.body()
                }
            }
            callback(estimatedFeeResponse)
        }
    }

    fun getAllTransactionsFromServer(context: Context) {
        val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
        PollingTransactionsManager.getAllTransactionsFromServer(context, deviceId)
    }

    fun createAssets(context: Context) {
        Timber.i("creating ETH_TEST3 and BTC_TEST assets")
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val deviceId = getDeviceId()
                    var response = Api.with(StorageManager.get(context, deviceId)).createAsset(deviceId, "ETH_TEST3").execute()
                    Timber.d("API response createAsset ETH_TEST3:$response")
                    response = Api.with(StorageManager.get(context, deviceId)).createAsset(deviceId, "BTC_TEST").execute()
                    Timber.d("API response createAsset BTC_TEST:$response")
                }.onFailure {
                    Timber.e(it, "Failed to call createAsset API")
                }
            }
        }
    }
    fun getAssets(context: Context, callback: ((result: List<SupportedAsset>) -> Unit)) {
        var assets: List<SupportedAsset> = arrayListOf()
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val deviceId = getDeviceId()
                        val response = Api.with(StorageManager.get(context, deviceId)).getAssets(deviceId).execute()
                        Timber.d("API response getAssets $response")
                        val supportedAssets = response.body()

                        assets = updateAssetsBalanceAndAddress(supportedAssets, context, deviceId)
                    }.onFailure {
                        Timber.e(it, "Failed to call getAssets API")
                    }
                }
            }
            callback(assets)
        }
    }

    private fun updateAssetsBalanceAndAddress(supportedAssets: ArrayList<SupportedAsset>?,
                                              context: Context,
                                              deviceId: String): List<SupportedAsset> {
        var assets: List<SupportedAsset> = arrayListOf()
        supportedAssets?.let {
            assets = it
            assets.forEach { asset ->
                asset.fee?.apply {
                    low?.feeLevel = FeeLevel.LOW
                    medium?.feeLevel = FeeLevel.MEDIUM
                    high?.feeLevel = FeeLevel.HIGH
                }
                val balanceResponse = Api.with(StorageManager.get(context, deviceId)).getAssetBalance(deviceId, asset.id).execute()
                balanceResponse.body()?.let { assetBalance ->
                    asset.balance = assetBalance.total.toDouble().roundToDecimalFormat(EXTENDED_PATTERN)
                    val price = (asset.balance.toDouble() * asset.rate).roundToDecimalFormat()
                    asset.price = price

                }
                val addressResponse = Api.with(StorageManager.get(context, deviceId)).getAssetAddress(deviceId, asset.id).execute()
                addressResponse.body()?.let { assetAddress ->
                    asset.address = assetAddress.address
                }
            }
        }
        return assets
    }

    fun takeover(callback: (result: Set<FullKey>) -> Unit) {
        val deviceId = getDeviceId()
        Fireblocks.getInstance(deviceId).takeover {
            Timber.d("takeover keys result: $it")
            callback.invoke(it)
        }
    }
}