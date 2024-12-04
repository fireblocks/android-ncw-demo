package com.fireblocks.sdkdemo

import android.content.Context
import android.widget.Toast
import com.fireblocks.sdk.Environment
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.FireblocksOptions
import com.fireblocks.sdk.adddevice.FireblocksJoinWalletHandler
import com.fireblocks.sdk.adddevice.JoinWalletDescriptor
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksEventHandler
import com.fireblocks.sdk.ew.AuthTokenRetriever
import com.fireblocks.sdk.ew.EmbeddedWallet
import com.fireblocks.sdk.ew.EmbeddedWalletOptions
import com.fireblocks.sdk.ew.models.Account
import com.fireblocks.sdk.ew.models.Asset
import com.fireblocks.sdk.ew.models.AssetAddress
import com.fireblocks.sdk.ew.models.AssetBalance
import com.fireblocks.sdk.ew.models.AssignResponse
import com.fireblocks.sdk.ew.models.CreateTransactionResponse
import com.fireblocks.sdk.ew.models.DestinationTransferPeerPath
import com.fireblocks.sdk.ew.models.EstimatedTransactionFeeResponse
import com.fireblocks.sdk.ew.models.FeeLevel
import com.fireblocks.sdk.ew.models.LatestBackupResponse
import com.fireblocks.sdk.ew.models.OneTimeAddress
import com.fireblocks.sdk.ew.models.PaginatedResponse
import com.fireblocks.sdk.ew.models.SourceTransferPeerPath
import com.fireblocks.sdk.ew.models.TransactionRequest
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdk.ew.models.TransferPeerPathType
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.DerivationParams
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyBackup
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyRecovery
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentInitializer
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.extensions.getNCWLogLevel
import com.fireblocks.sdkdemo.bl.core.extensions.getWIFFromPrivateKey
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.polling.DataRepository
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingTransactionsManager
import com.fireblocks.sdkdemo.bl.core.storage.KeyStorageManager
import com.fireblocks.sdkdemo.bl.core.storage.PreferencesManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private var authClientId: String = ""
    private var embeddedWallet: EmbeddedWallet? = null

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

    /*************************
     * Embedded Wallet methods *
     * ***********************
     */

    private fun getEmbeddedWallet(viewModel: BaseViewModel? = null): EmbeddedWallet? {
        return embeddedWallet ?: run {
            viewModel?.snackBar?.postValue(ObservedData("Embedded Wallet is not initialized"))
            null
        }
    }

    private fun <T> getEWResultFailure(): Result<T> = Result.failure(Exception("Failed to get embeddedWallet instance"))

    fun createEmbeddedWallet(context: Context, viewModel: BaseViewModel): EmbeddedWallet? {
        runCatching {
            val authClientId = when (BuildConfig.FLAVOR_server) {
                "dev" -> "6303105e-38ac-4a21-8909-2b1f7f205fd1"
                "production" -> ""
                "sandbox" -> ""
                else -> ""
            }
            embeddedWallet = EmbeddedWallet(
                context,
                authClientId = authClientId,
                authTokenRetriever = object : AuthTokenRetriever {
                    override suspend fun getAuthToken(): Result<String> {
                        val idToken = runBlocking {
                            SignInUtil.getInstance().getIdTokenBlocking(context)
                        }
                        return idToken?.let {
                            Result.success(it)
                        } ?: Result.failure(Exception("Failed to get auth token"))
                    }
                },
                options = EmbeddedWalletOptions.Builder()
                    .setLogLevel(getNCWLogLevel())
                    .setLogToConsole(true)
                    .setLogNetwork(true)
                    .build())
            this.authClientId = authClientId
            Timber.i("$authClientId - embeddedWallet initialized")
            Toast.makeText(context, "Embedded Wallet initialized", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Timber.e(it, "Failed to initialize embeddedWallet")
            viewModel.snackBar.postValue(ObservedData("${it.message}"))
        }
        return embeddedWallet
    }

    fun createAccountIfNeeded(context: Context, viewModel: BaseViewModel) {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val account = preferencesManager.account.value() //TODO clear this on signout
        if (account == null) {
            launch {
                withContext(coroutineContext) {
                    runCatching {
                        //TODO first call get accounts and check if there is an account
                        val result : Result<Account> = createAccount(viewModel)
                        if (result.isSuccess) {
                            result.getOrNull()?.let {
                                preferencesManager.account.set(it)
                            } ?: {
                                Timber.e("Failed to create account")
                            }
                        }
                    }.onFailure {
                        Timber.e(it, "Failed to create account")
                    }
                }
            }
        } else {
            Timber.d("Account $account already exists")
        }
    }

    private fun getAccountId(): Int {
        return 0 //TODO implement
//        val preferencesManager = PreferencesManager.get(context, authClientId)
//        val account = preferencesManager.account.value()
//        return PreferencesManager.get().account.value()?.accountId ?: 0
    }

    suspend fun assignWallet(viewModel: BaseViewModel): Result<AssignResponse> {
        return getEmbeddedWallet(viewModel)?.assignWallet() ?: return getEWResultFailure()
    }

    suspend fun createAccount(viewModel: BaseViewModel): Result<Account> {
        return getEmbeddedWallet(viewModel)?.createAccount() ?: return getEWResultFailure()
    }

    suspend fun getAccounts(viewModel: BaseViewModel): Result<PaginatedResponse<Account>> {
        return getEmbeddedWallet(viewModel)?.getAccounts() ?: return getEWResultFailure()
    }

    suspend fun getLatestBackup(viewModel: BaseViewModel): Result<LatestBackupResponse> {
        return getEmbeddedWallet(viewModel)?.getLatestBackup() ?: return getEWResultFailure()
    }

    suspend fun getAssets(accountId: Int = 0, viewModel: BaseViewModel): Result<PaginatedResponse<Asset>> {
        return getEmbeddedWallet(viewModel)?.getAssets(accountId = accountId) ?: return getEWResultFailure()
    }

    suspend fun getAssetsSummary(context: Context, accountId: Int = 0, viewModel: BaseViewModel): List<SupportedAsset> {
        val supportedAssets = ArrayList<SupportedAsset>()
        getAssets(viewModel = viewModel).onSuccess { paginatedResponse ->
            Timber.i("Assets loaded: $paginatedResponse")
            val assets = paginatedResponse.data
            assets?.let {
                coroutineScope {
                    val deferredList = assets.map { asset ->
                        async {
                            val supportedAsset = SupportedAsset(asset = asset)
                            supportedAssets.add(supportedAsset)
                            val addressesDeferred = async {
                                getAssetAddresses(context, assetId = asset.id, accountId = accountId, viewModel = viewModel).onSuccess { paginatedResponse ->
                                    Timber.d("Asset addresses loaded: $paginatedResponse")
                                    val assetAddress = paginatedResponse.data?.firstOrNull()
                                    supportedAsset.assetAddress = assetAddress?.address
                                    supportedAsset.accountId = assetAddress?.accountId
                                    supportedAsset.addressIndex = assetAddress?.addressIndex
                                    assetAddress?.let { saveAssetAddress(context, assetId = asset.id, assetAddress = it) }
                                }
                            }
                            val balanceDeferred = async {
                                getAssetBalance(assetId = asset.id, accountId = accountId, viewModel = viewModel).onSuccess { balance ->
                                    Timber.d("Asset balance loaded: $balance")
                                    supportedAsset.balance = balance.available
                                    val price = (supportedAsset.balance.toDouble() * supportedAsset.rate).roundToDecimalFormat()
                                    supportedAsset.price = price
                                }
                            }
                            addressesDeferred.await()
                            balanceDeferred.await()
                        }
                    }
                    deferredList.awaitAll()
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to getAssets")
        }
        return supportedAssets
    }

    suspend fun getSupportedAssets(viewModel: BaseViewModel, getAllPages: Boolean? = true): Result<PaginatedResponse<Asset>> {
        return getEmbeddedWallet(viewModel)?.getSupportedAssets(getAllPages = getAllPages) ?: return getEWResultFailure()
    }

    suspend fun addAsset(assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<AssetAddress> {
        return getEmbeddedWallet(viewModel)?.addAsset(assetId = assetId, accountId = accountId) ?: return getEWResultFailure()
    }

    suspend fun getAsset(accountId: Int = 0, assetId: String, viewModel: BaseViewModel): Result<Asset> {
        return getEmbeddedWallet(viewModel)?.getAsset(assetId, accountId) ?: return getEWResultFailure()
    }

    suspend fun getAssetAddresses(context: Context, assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<PaginatedResponse<AssetAddress>> {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val addressHashMap = preferencesManager.assetsAddress.value()
        if (addressHashMap.containsKey(assetId) && addressHashMap[assetId] != null) {
            val assetAddress: AssetAddress = addressHashMap[assetId]!!
            return Result.success(PaginatedResponse(data = listOf(assetAddress)))
        }
        return getEmbeddedWallet(viewModel)?.getAssetAddresses(assetId, accountId) ?: return getEWResultFailure()
    }

    fun saveAssetAddress(context: Context, assetId: String, assetAddress: AssetAddress) {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val addressHashMap = preferencesManager.assetsAddress.value()
        addressHashMap[assetId] = assetAddress
        preferencesManager.assetsAddress.set(addressHashMap)
    }

    suspend fun getAssetBalance(assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<AssetBalance> {
        return getEmbeddedWallet(viewModel)?.getAssetBalance(assetId, accountId) ?: return getEWResultFailure()
    }

    suspend fun getTransactionById(viewModel: BaseViewModel, transactionId: String): Result<TransactionResponse> {
        getEmbeddedWallet(viewModel)?.let {
            val repository = DataRepository(accountId = getAccountId(), it)
            return repository.getTransactionById(transactionId)
        } ?: run {
            return getEWResultFailure()
        }
    }
    
    suspend fun estimateTransactionFee(assetId: String, destAddress: String, amount: String, viewModel: BaseViewModel): Result<EstimatedTransactionFeeResponse> {
        val transactionRequest = TransactionRequest(
            assetId = assetId,
            sourcePath = SourceTransferPeerPath(id = getAccountId().toString()),
            destination = DestinationTransferPeerPath(type = TransferPeerPathType.ONE_TIME_ADDRESS, oneTimeAddress = OneTimeAddress(address = destAddress)),
            amount = amount)
        return getEmbeddedWallet(viewModel)?.estimateTransactionFee(transactionRequest) ?: return getEWResultFailure()
    }

    fun init(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, joinWallet: Boolean = false) {
        val deviceId = when (joinWallet) {
            true -> getJoinWalletDeviceId()
            else -> getDeviceId(context)
        }
        if (deviceId.isEmpty()) {
            Timber.e("Failed to init, no deviceId")
            viewModel.snackBar.postValue(ObservedData("Failed to init, no deviceId"))
            viewModel.passLogin.postValue(ObservedData(false))
            return
        }
        launch {
            withContext(coroutineContext) {
                viewModel.showProgress(true)
                if (SignInUtil.getInstance().isSignedIn(context)) {
                        if (joinWallet) {
                            initFireblocks(context, viewModel, forceInit, startPollingTransactions = false, deviceId = deviceId)
                        } else {
                            val assignSuccess = runBlocking {
                                val assignWalletResult = assignWallet(viewModel).onSuccess {
                                    it.walletId?.let {
                                        walletId -> StorageManager.get(context, deviceId).walletId.set(walletId)
                                    }
                                }
                                assignWalletResult.isSuccess
                            }
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
                    Timber.e("Failed to login. there is no signed in user")
                    viewModel.showProgress(false)
                    viewModel.snackBar.postValue(ObservedData("Failed to login"))
                    viewModel.passLogin.postValue(ObservedData(false))
                }
            }
        }
    }

    fun getDeviceId(context: Context? = null): String {
        return MultiDeviceManager.instance.lastUsedDeviceId(context)
    }

    fun getPassphraseLocation(context: Context, passphraseId: String, callback: (PassphraseInfo?) -> Unit) {
        return callback.invoke(PassphraseInfo(passphraseId = passphraseId, location = PassphraseLocation.GoogleDrive)) //TODO is this ok?
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                var passphraseInfo: PassphraseInfo? = null
//                runCatching {
//                    val deviceId = getDeviceId(context)
//                    val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfo(passphraseId, getHeaders(context, deviceId)).execute()
//                    passphraseInfo = response.body()
//                    logResponse("getPassphraseInfo", response)
//                }.onFailure {
//                    Timber.e(it, "Failed to call getPassphraseInfos API")
//                }
//                callback.invoke(passphraseInfo)
//            }
//        }
    }

    private fun initFireblocks(context: Context, viewModel: BaseViewModel, forceInit: Boolean = false, startPollingTransactions: Boolean = true, deviceId: String = getDeviceId(context)) {
        if (forceInit) {
            initializedFireblocks = false
        }
        val storageManager = StorageManager.get(context, deviceId)
        val env = storageManager.environment().env()
        val environment = Environment.from(env) ?: Environment.DEFAULT
        Timber.i("$deviceId - using environment: $environment according to env: $env")
        val fireblocksOptions = FireblocksOptions.Builder()
            .setEventHandler(object : FireblocksEventHandler { //TODO use it
                override fun onEvent(event: Event) {
                    if (event.error != null){
                        Timber.e("onEvent - $event")
                    } else if (isDebugLog()) {
                        Timber.d("onEvent - $event")
                    }
                    fireEvent(event)
                }
            })
            .build()

        val fireblocksSdk = if (initializedFireblocks) {
            Fireblocks.getInstance(deviceId)
        } else {
            val sdk = initialize(context, deviceId, fireblocksOptions, viewModel)
            if (sdk != null && startPollingTransactions) {
                startPollingTransactions(context, deviceId)
            }
            sdk
        }
        val initializeSuccess = fireblocksSdk != null
        Timber.i("$deviceId - initializeSuccess: $initializeSuccess")
        if (isDebugLog()) {
            Timber.d("$deviceId - getCurrentStatus: ${fireblocksSdk?.getCurrentStatus()}")
        }

        if (initializeSuccess) {
            viewModel.passLogin.postValue(ObservedData(true))
        }
        viewModel.showProgress(false)
    }

    fun startPollingTransactions(context: Context, deviceId: String = getDeviceId(context)) {
        if (hasKeys(context, deviceId)) {
            embeddedWallet?.let {
                PollingTransactionsManager.startPollingTransactions(context = context, deviceId = deviceId, accountId = getAccountId(), getAllTransactions = true, embeddedWallet = it)
            }
        }
    }

    fun hasKeys(context: Context, deviceId: String = getDeviceId(context)): Boolean {
        val status = getKeyCreationStatus(context, deviceId)
        return generatedSuccessfully(status)
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

    private fun fireEvent(event: Event) {
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

    fun stopPollingTransactions(){
        MultiDeviceManager.instance.allDeviceIds().iterator().forEach { deviceId ->
            PollingTransactionsManager.stopPolling(deviceId)
        }
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
            runCatching {
                if (isDebugLog()) {
                    Timber.d("fireTransaction: $transactionWrapper")
                }
                // use a for loop instead of forEach to avoid ConcurrentModificationException
                transactionListeners.toList().forEach { //TODO why the same model is listed 3 times?
                    it.fireTransaction(context, transactionWrapper, count)
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

    fun getTransactions(): HashSet<TransactionWrapper> {
        synchronized(this) {
            val deviceId = getDeviceId()
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
                           viewModel: BaseViewModel): Fireblocks? {
        return try {
            val keyStorage = FireblocksKeyStorageImpl(context, deviceId)
            KeyStorageManager.setKeyStorage(deviceId, keyStorage)

            val fireblocks = getEmbeddedWallet(viewModel)?.initializeCore(deviceId, keyStorage, fireblocksOptions)
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
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Fireblocks")
            runBlocking(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
            return null
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
            startPollingTransactions(context, deviceId)
        }
        Timber.i("called generateMPCKeys")
    }

    fun backupKeys(context: Context, passphrase: String, passphraseId: String, callback: ((result: Set<KeyBackup>) -> Unit)) {
        val deviceId = getDeviceId(context)
        val fireblocks = Fireblocks.getInstance(deviceId)

        fireblocks.backupKeys(passphrase, passphraseId) {
            if (isDebugLog()) {
                Timber.d("Backup keys result: $it")
            }
            callback.invoke(it)
        }
    }

    fun recoverKeys(context: Context, passphraseResolver: FireblocksPassphraseResolver, callback: (result: Set<KeyRecovery>) -> Unit) {
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).recoverKeys(passphraseResolver = passphraseResolver) {
            if (isDebugLog()) {
                Timber.d("Recover keys result: $it")
            }
            callback.invoke(it)
            startPollingTransactions(context, deviceId)
        }
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
           Timber.e(it, "Failed to getKeyCreationStatus")
        }
        return keysStatus
    }

    //TODO should this be a suspend function?
    fun cancelTransaction(context: Context, deviceId: String, txId: String): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                embeddedWallet?.let {
                    val repository = DataRepository(accountId = getAccountId(), it)
                    success = repository.cancelTransaction(txId = txId)
                }
            }
        }
        return success
    }

//    fun createTransaction(context: Context, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel, callback: (response: CreateTransactionResponse?) -> Unit){
//        launch {
//            val deviceId = getDeviceId(context)
//            val response = PollingTransactionsManager.createTransaction(context, deviceId, assetId, destAddress, amount, feeLevel)
//            callback(response)
//        }
//    }

    suspend fun createOneTimeAddressTransaction(assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel, viewModel: BaseViewModel): Result<CreateTransactionResponse> {
        getEmbeddedWallet(viewModel)?.let {
            val repository = DataRepository(accountId = getAccountId(), it)
            return repository.createOneTimeAddressTransaction(assetId = assetId, destAddress = destAddress, amount = amount, feeLevel = feeLevel)
        } ?: run {
            return getEWResultFailure()
        }
    }


    fun getOrCreatePassphraseId(context: Context, passphraseLocation: PassphraseLocation, callback: (String?) -> Unit){
//        launch {
//            withContext(Dispatchers.IO) {
//                getPassphraseId(context) { passphraseId ->
//                    if (passphraseId.isNullOrEmpty()) {
                        val generatedPassphraseId  = Fireblocks.generatePassphraseId() //TODO consider putting it in StorageManager
//                        createPassphraseInfo(context, generatedPassphraseId, passphraseLocation) { success ->
//                            if (success) {
                                callback(generatedPassphraseId)
//                            } else {
//                                Timber.e("Failed to createPassphraseInfo")
//                                callback(null)
//                            }
//                        }
//                    } else {
//                        callback(passphraseId)
//                    }
//                }
//            }
//        }
    }

//    private fun getPassphraseId(context: Context, callback: (String?) -> Unit) {
//        var passphraseId: String? = null
//        runCatching {
//            val deviceId = getDeviceId(context)
//            val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfos(getHeaders(context, deviceId)).execute()
//            logResponse("getPassphraseInfos", response)
//            passphraseId = response.body()?.let { passphraseInfos ->
//                if (!passphraseInfos.passphrases.isNullOrEmpty()) {
//                    passphraseInfos.passphrases.last().passphraseId
//                } else {
//                    null
//                }
//            }
//        }.onFailure {
//            Timber.e(it, "Failed to call getPassphraseInfos API")
//        }
//        callback.invoke(passphraseId)
//    }

    private fun createPassphraseInfo(context: Context, passphraseId: String, passphraseLocation: PassphraseLocation, callback: (success: Boolean) -> Unit) {
//        var success = false
//        runCatching {
//            val deviceId = getDeviceId(context)
//            val response = Api.with(StorageManager.get(context, deviceId)).createPassphraseInfo(passphraseId, body = PassphraseInfo(location = passphraseLocation), getHeaders(context, deviceId)).execute()
//            success = response.isSuccessful
//            logResponse("createPassphraseInfo", response)
//        }.onFailure {
//            Timber.w(it, "Failed to call createPassphraseInfo API")
//        }
//        callback.invoke(success)
    }

    private fun logResponse(apiName: String, response: Response<*>) {
        if (isDebugLog()) {
            Timber.d("got response from $apiName rest API code:${response.code()}, isSuccessful:${response.isSuccessful} body: ${response.body()}")
        }
    }

    fun takeover(context: Context, callback: (result: Set<FullKey>) -> Unit) {
        val deviceId = getDeviceId(context)
        Fireblocks.getInstance(deviceId).takeover {
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
        val deviceId = getJoinWalletDeviceId()
        if (deviceId.isEmpty()) {
            Timber.e("Failed to requestJoinExistingWallet, deviceId is null or empty")
            callback(setOf())
            return
        }
        Fireblocks.getInstance(deviceId).requestJoinExistingWallet(joinWalletHandler) { result ->
            Timber.i("joinExistingWallet result: $result")
            callback(result)
        }
        Timber.i("called joinExistingWallet")
    }

    fun getJoinWalletDeviceId() = MultiDeviceManager.instance.getJoinWalletDeviceId()

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
            true -> getJoinWalletDeviceId()
            else -> getDeviceId(context)
        }
        Fireblocks.getInstance(deviceId).stopJoinWallet()
        Timber.i("$deviceId - called stopJoinWallet")
    }

    fun persistJoinWalletDeviceId(context: Context) {
        StorageManager.get(context, getJoinWalletDeviceId()).apply {
            MultiDeviceManager.instance.addDeviceId(context, deviceId)
        }
        MultiDeviceManager.instance.clearJoinWalletDeviceId()
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