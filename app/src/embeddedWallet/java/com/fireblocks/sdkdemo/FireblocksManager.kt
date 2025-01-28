package com.fireblocks.sdkdemo

import BaseFireblocksManager
import android.content.Context
import android.widget.Toast
import com.fireblocks.sdk.Environment
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksEventHandler
import com.fireblocks.sdk.ew.AuthTokenRetriever
import com.fireblocks.sdk.ew.CoreOptions
import com.fireblocks.sdk.ew.EmbeddedWallet
import com.fireblocks.sdk.ew.EmbeddedWalletOptions
import com.fireblocks.sdk.ew.models.Account
import com.fireblocks.sdk.ew.models.AddressDetails
import com.fireblocks.sdk.ew.models.Asset
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
import com.fireblocks.sdk.ew.models.SuccessResponse
import com.fireblocks.sdk.ew.models.TransactionRequest
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdk.ew.models.TransferPeerPathType
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.cryptocurrency.CryptoCurrencyProvider
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.extensions.getNCWLogLevel
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
import com.fireblocks.sdkdemo.bl.fingerprint.FireblocksKeyStorageImpl
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.viewmodel.BaseLoginViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 06/03/2023.
 */
class FireblocksManager : BaseFireblocksManager() {

    private var authClientId: String = ""
    private var embeddedWallet: EmbeddedWallet? = null

    companion object {
        private var instance: FireblocksManager? = null
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: FireblocksManager().also { instance = it }
                }
    }

    override fun setupEnvironmentsAndDevice(context: Context) {
        super.setupEnvironmentsAndDevice(context)
        CryptoCurrencyProvider.loadCryptoCurrencyData(context)
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

    fun createEmbeddedWallet(context: Context): EmbeddedWallet? {
        runCatching {

            val authClientId = MultiDeviceManager.instance.getAuthClientId()
            Timber.i("using authClientId $authClientId")
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
        }.onFailure {
            Timber.e(it, "Failed to initialize embeddedWallet")
        }
        return embeddedWallet
    }

    private fun createAccountIfNeeded(context: Context, viewModel: BaseViewModel) {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val account = preferencesManager.account.value() //TODO clear this on sign out
        if (account == null) {
            launch {
                withContext(coroutineContext) {
                    getAccounts(viewModel).onSuccess { getAccountsResponse ->
                        if (getAccountsResponse.data.isNullOrEmpty()) {
                            Timber.i("createAccountIfNeeded - No account found, creating account")
                            createAccount(viewModel).onSuccess {
                                Timber.i("createAccountIfNeeded - Account created: $it")
                                preferencesManager.account.set(it)
                            }.onFailure {
                                Timber.e(it, "Failed to create account")
                            }
                        } else {
                            Timber.i("createAccountIfNeeded - Account already exists - ${getAccountsResponse.data}")
                            // set the first account as the current account
                            getAccountsResponse.data?.firstOrNull()?.let {
                             preferencesManager.account.set(it)
                            }
                        }
                    }.onFailure {
                        Timber.e(it, "Failed to getAccounts")
                    }
                }
            }
        } else {
            Timber.d("createAccountIfNeeded - $account already exists")
        }
    }

    private fun getAccountId(): Int {
        return 0 //TODO implement
//        val preferencesManager = PreferencesManager.get(context, authClientId)
//        val account = preferencesManager.account.value()
//        return PreferencesManager.get(context, authClientId).account.value()?.accountId ?: 0
    }

    private suspend fun assignWallet(viewModel: BaseViewModel): Result<AssignResponse> {
        return getEmbeddedWallet(viewModel)?.assignWallet() ?: return getEWResultFailure()
    }

    private suspend fun createAccount(viewModel: BaseViewModel): Result<Account> {
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
            val assets = paginatedResponse.data
            assets?.let {
                coroutineScope {
                    val deferredList = assets.map { asset ->
                        async {
                            val supportedAsset = SupportedAsset(asset = asset)
                            asset.id?.let { assetId ->
                                supportedAssets.add(supportedAsset)
                                val addressesDeferred = async {
                                    getAssetAddresses(context, assetId = assetId, accountId = accountId, viewModel = viewModel).onSuccess { paginatedResponse ->
                                        Timber.d("Asset addresses loaded: $paginatedResponse")
                                        val assetAddress = paginatedResponse.data?.firstOrNull()
                                        supportedAsset.assetAddress = assetAddress?.address
                                        supportedAsset.accountId = assetAddress?.accountId
                                        supportedAsset.addressIndex = assetAddress?.addressIndex
                                        assetAddress?.let { saveAssetAddress(context, assetId = assetId, assetAddress = it) }
                                    }
                                }
                                val balanceDeferred = async {
                                    getAssetBalance(assetId = assetId, accountId = accountId, viewModel = viewModel).onSuccess { balance ->
                                        Timber.d("Asset balance loaded: $balance")
                                        supportedAsset.balance = balance.available
                                        supportedAsset.rate = CryptoCurrencyProvider.getCryptoCurrencyPrice(asset.symbol) ?: 1.0
                                        supportedAsset.balance?.let { balance ->
                                            supportedAsset.price = (balance.toDouble() * supportedAsset.rate).roundToDecimalFormat()
                                        }
                                    }
                                }
                                addressesDeferred.await()
                                balanceDeferred.await()
                            }
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

    suspend fun getSupportedAssets(viewModel: BaseViewModel, allPages: Boolean? = true): Result<PaginatedResponse<Asset>> {
        return getEmbeddedWallet(viewModel)?.getSupportedAssets(allPages = allPages) ?: return getEWResultFailure()
    }

    suspend fun addAsset(assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<AddressDetails> {
        return getEmbeddedWallet(viewModel)?.addAsset(assetId = assetId, accountId = accountId) ?: return getEWResultFailure()
    }

    suspend fun getAsset(accountId: Int = 0, assetId: String, viewModel: BaseViewModel): Result<Asset> {
        return getEmbeddedWallet(viewModel)?.getAsset(accountId, assetId) ?: return getEWResultFailure()
    }

    suspend fun getAssetAddresses(context: Context, assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<PaginatedResponse<AddressDetails>> {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val addressHashMap = preferencesManager.assetsAddress.value()
        if (addressHashMap.containsKey(assetId) && addressHashMap[assetId] != null) {
            val assetAddress: AddressDetails = addressHashMap[assetId]!!
            return Result.success(PaginatedResponse(data = listOf(assetAddress)))
        }
        return getEmbeddedWallet(viewModel)?.getAddresses(accountId, assetId) ?: return getEWResultFailure()
    }

    fun saveAssetAddress(context: Context, assetId: String, assetAddress: AddressDetails) {
        val preferencesManager = PreferencesManager.get(context, authClientId)
        val addressHashMap = preferencesManager.assetsAddress.value()
        addressHashMap[assetId] = assetAddress
        preferencesManager.assetsAddress.set(addressHashMap)
    }

    private suspend fun getAssetBalance(assetId: String, accountId: Int = 0, viewModel: BaseViewModel): Result<AssetBalance> {
        return getEmbeddedWallet(viewModel)?.getBalance(accountId, assetId) ?: return getEWResultFailure()
    }

    suspend fun getTransactionById(viewModel: BaseViewModel, transactionId: String): Result<TransactionResponse> {
        return getEmbeddedWallet(viewModel)?.let {
            val repository = DataRepository(accountId = getAccountId(), it)
            repository.getTransactionById(transactionId)
        } ?: return getEWResultFailure()
    }
    
    suspend fun estimateTransactionFee(assetId: String, destAddress: String, amount: String, viewModel: BaseViewModel): Result<EstimatedTransactionFeeResponse> {
        val transactionRequest = TransactionRequest(
            assetId = assetId,
            source = SourceTransferPeerPath(id = getAccountId().toString()),
            destination = DestinationTransferPeerPath(type = TransferPeerPathType.ONE_TIME_ADDRESS, oneTimeAddress = OneTimeAddress(address = destAddress)),
            amount = amount)
        return getEmbeddedWallet(viewModel)?.estimateTransactionFee(transactionRequest) ?: return getEWResultFailure()
    }

    suspend fun cancelTransaction(viewModel: BaseViewModel, txId: String): Result<SuccessResponse> {
        return getEmbeddedWallet(viewModel)?.let {
            val repository = DataRepository(accountId = getAccountId(), it)
            repository.cancelTransaction(txId)
        } ?: return getEWResultFailure()
    }

    suspend fun createOneTimeAddressTransaction(assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel, viewModel: BaseViewModel): Result<CreateTransactionResponse> {
        return getEmbeddedWallet(viewModel)?.let {
            val repository = DataRepository(accountId = getAccountId(), it)
            repository.createOneTimeAddressTransaction(assetId = assetId, destAddress = destAddress, amount = amount, feeLevel = feeLevel)
        } ?: return getEWResultFailure()
    }

    fun init(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, deviceId: String, loginFlow: BaseLoginViewModel.LoginFlow? = null, joinWallet: Boolean = false, recoverWallet: Boolean = false) {
        if (deviceId.isEmpty()) {
            viewModel.onError(context, message = "Failed to init, no deviceId")
            return
        }
        launch {
            withContext(coroutineContext) {
                viewModel.showProgress(true)
                if (SignInUtil.getInstance().isSignedIn(context)) {
                    runBlocking {
                        assignWallet(viewModel).onSuccess {
                            Timber.i("assignWalletResult: $it")
                            it.walletId?.let { walletId ->
                                StorageManager.get(context, deviceId).walletId.set(walletId)
                                createAccountIfNeeded(context, viewModel)
                            }
                            initFireblocks(context, viewModel, forceInit, deviceId = deviceId, joinWallet = joinWallet, recoverWallet = recoverWallet)
                        }.onFailure {
                            viewModel.onError(context, throwable = it)
                        }
                    }
                } else {
                    viewModel.onError(context, message = "Failed to login. there is no signed in user")
                }
            }
        }
    }

    fun initFireblocks(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, startPollingTransactions: Boolean = true, deviceId: String = getDeviceId(context), joinWallet: Boolean = false, recoverWallet: Boolean = false) {
        if (forceInit) {
            initializedFireblocks = false
        }
        val storageManager = StorageManager.get(context, deviceId)
        val env = storageManager.environment().env()
        val environment = Environment.from(env) ?: Environment.DEFAULT
        Timber.i("$deviceId - using environment: $environment according to env: $env")
        val coreOptions = CoreOptions.Builder()
            .setEventHandler(object : FireblocksEventHandler {
                override fun onEvent(event: Event) {
                    if (event.error != null){
                        Timber.e("onEvent - $event")
                    } else if (isDebugLog()) {
                        Timber.d("onEvent - $event")
                    }
                    fireEvent(event)
                }
            }).build()

        val fireblocksSdk = if (initializedFireblocks) {
            Fireblocks.getInstance(deviceId)
        } else {
            val sdk = initialize(context, deviceId, coreOptions, viewModel)
            if (sdk != null && startPollingTransactions) {
                startPollingTransactions(context)
            }
            sdk
        }
        val initializeSuccess = fireblocksSdk != null
        Timber.i("$deviceId - initializeSuccess: $initializeSuccess")
        if (isDebugLog()) {
            Timber.d("$deviceId - getCurrentStatus: ${fireblocksSdk?.getCurrentStatus()}")
        }

        if (initializeSuccess) {
            if (joinWallet) {
                viewModel.onPassedInitForJoinWallet(true)
            } else if (recoverWallet) {
                viewModel.onPassedInitForRecover(true)
            } else {
                viewModel.onPassedLogin(true)
            }
        }
        viewModel.showProgress(false)
    }

    private fun initialize(context: Context,
                           deviceId: String,
                           coreOptions: CoreOptions,
                           viewModel: BaseViewModel): Fireblocks? {
        return try {
            val keyStorage = FireblocksKeyStorageImpl(context, deviceId)
            KeyStorageManager.setKeyStorage(deviceId, keyStorage)

            val fireblocks = getEmbeddedWallet(viewModel)?.initializeCore(deviceId, keyStorage, coreOptions)
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

    fun getPassphraseLocation(context: Context, passphraseId: String, callback: (PassphraseInfo?) -> Unit) {
        return callback.invoke(PassphraseInfo(passphraseId = passphraseId, location = PassphraseLocation.GoogleDrive))
    }

    override fun startPollingTransactions(context: Context) {
        val deviceId = getDeviceId(context)
        if (hasKeys(context, deviceId)) {
            embeddedWallet?.let {
                PollingTransactionsManager.startPollingTransactions(context = context, deviceId = deviceId, accountId = getAccountId(), getAllTransactions = true, embeddedWallet = it)
            }
        }
    }

    fun getOrCreatePassphraseId(context: Context, passphraseLocation: PassphraseLocation, callback: (String?) -> Unit){
        var passphraseId = StorageManager.get(context, getDeviceId(context)).passphraseId.value()
        if (passphraseId.isEmpty()) {
            passphraseId  = Fireblocks.generatePassphraseId()
        }
        callback(passphraseId)
    }

    fun deleteWallet(context: Context) {
        stopPollingTransactions()
        getDeviceId(context).let {
            MultiDeviceManager.instance.deleteAllUsers()
            StorageManager.get(context, it).clear()
        }
    }
}