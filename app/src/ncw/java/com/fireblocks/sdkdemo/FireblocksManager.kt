package com.fireblocks.sdkdemo

import BaseFireblocksManager
import android.content.Context
import android.widget.Toast
import com.fireblocks.sdk.Environment
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdk.FireblocksOptions
import com.fireblocks.sdk.events.Event
import com.fireblocks.sdk.events.FireblocksEventHandler
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.environment.EnvironmentProvider
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.extensions.EXTENDED_PATTERN
import com.fireblocks.sdkdemo.bl.core.extensions.getNCWLogLevel
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.server.Api
import com.fireblocks.sdkdemo.bl.core.server.EstimatedFeeRequestBody
import com.fireblocks.sdkdemo.bl.core.server.FireblocksMessageHandlerImpl
import com.fireblocks.sdkdemo.bl.core.server.HeaderInterceptor.Companion.getHeaders
import com.fireblocks.sdkdemo.bl.core.server.HeaderProvider
import com.fireblocks.sdkdemo.bl.core.server.JoinWalletBody
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.FireblocksDevice
import com.fireblocks.sdkdemo.bl.core.server.models.ResponseErrorUtil
import com.fireblocks.sdkdemo.bl.core.server.polling.PollingTransactionsManager
import com.fireblocks.sdkdemo.bl.core.storage.KeyStorageManager
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.AssetsSummary
import com.fireblocks.sdkdemo.bl.core.storage.models.BackupInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.EstimatedFeeResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseInfo
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.fingerprint.FireblocksKeyStorageImpl
import com.fireblocks.sdkdemo.ui.main.BaseViewModel
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 06/03/2023.
 */
class FireblocksManager : BaseFireblocksManager() {

    companion object {
        private var instance: FireblocksManager? = null
        const val DEFAULT_DEVICE_ID = "default"
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: FireblocksManager().also { instance = it }
                }
    }

    fun init(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, deviceId: String, joinWallet: Boolean = false, recoverWallet: Boolean = false, walletId: String? = null) {
        if (deviceId.isEmpty()) {
            viewModel.onError(context, message = "Failed to init, no deviceId")
            return
        }
        launch {
            withContext(coroutineContext) {
                viewModel.showProgress(true)
                if (SignInUtil.getInstance().isSignedIn(context)) {
                    val loginSuccess = login(context, deviceId)
                    Timber.d("loginSuccess: $loginSuccess")
                    if (loginSuccess) {
                        if (joinWallet) {
                            if (walletId.isNullOrEmpty()) {
                                Timber.e("Failed to join wallet, walletId is null or empty")
                                viewModel.onError(context, message = "Failed to join wallet, walletId is null or empty")
                                return@withContext
                            }
                            val joinSuccess = joinWallet(context, walletId, deviceId)
                            if (joinSuccess) {
                                initFireblocks(context, viewModel, forceInit, startPollingTransactions = false, deviceId = deviceId)
                                viewModel.onPassedInitForJoinWallet(true)
                            } else {
                                viewModel.onError(context, message = "Failed to join wallet")
                                return@withContext
                            }
                        } else {
                            val assignSuccess = assign(context, deviceId)
                            Timber.d("assignSuccess: $assignSuccess")
                            if (assignSuccess) {
                                initFireblocks(context, viewModel, forceInit, deviceId = deviceId)
                                if (recoverWallet) {
                                    viewModel.onPassedInitForRecover(true)
                                }
                            } else {
                                viewModel.onError(context, message = "Failed to assign")
                            }
                        }
                    } else {
                        signOut(context)
                        viewModel.onError(context, message = "Failed to login")
                    }
                } else {
                    viewModel.onError(context, message = "Failed to login. there is no signed in user")
                }
            }
        }
    }

    private fun initFireblocks(context: Context, viewModel: LoginViewModel, forceInit: Boolean = false, startPollingTransactions: Boolean = true, deviceId: String) {
        if (forceInit) {
            initializedFireblocks = false
        }
        val storageManager = StorageManager.get(context, deviceId)
        val env = storageManager.environment().env()
        val environment = Environment.from(env) ?: Environment.DEFAULT
        Timber.i("$deviceId - using environment: $environment according to env: $env")
        val fireblocksOptions = FireblocksOptions.Builder()
            .setLogLevel(getNCWLogLevel())
            .setLogToConsole(true)
            .setEventHandler(object : FireblocksEventHandler {
                override fun onEvent(event: Event) {
                    if (event.error != null){
                        Timber.e("onEvent - $event")
                    } else if (isDebugLog()) {
                        Timber.d("onEvent - $event")
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
            viewModel.onPassedLogin(true)
        }
        viewModel.showProgress(false)
    }

    private fun initialize(context: Context,
                           deviceId: String,
                           fireblocksOptions: FireblocksOptions,
                           viewModel: BaseViewModel): Fireblocks? {
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

    private fun login(context: Context, deviceId: String): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = Api.with(StorageManager.get(context, deviceId)).login(getHeaders(context, deviceId)).execute()
                    success = response.isSuccessful
                    logResponse("login", response)
                }.onFailure {
                    Timber.e(it, "Failed to call login API")
                }
            }
        }
        return success
    }

    private fun assign(context: Context, deviceId: String): Boolean {
        var success = false
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = Api.with(StorageManager.get(context, deviceId)).assign(deviceId, getHeaders(context, deviceId)).execute()
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
                    val response = Api.with(StorageManager.get(context, deviceId)).joinWallet(deviceId, JoinWalletBody(walletId), getHeaders(context, deviceId)).execute()
                    logResponse("joinWallet", response)
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

    fun getPassphraseLocation(context: Context, passphraseId: String, callback: (PassphraseInfo?) -> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                var passphraseInfo: PassphraseInfo? = null
                runCatching {
                    val deviceId = getDeviceId(context)
                    val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfo(passphraseId, getHeaders(context, deviceId)).execute()
                    passphraseInfo = response.body()
                    logResponse("getPassphraseInfo", response)
                }.onFailure {
                    Timber.e(it, "Failed to call getPassphraseInfos API")
                }
                callback.invoke(passphraseInfo)
            }
        }
    }

    override fun startPollingTransactions(context: Context) {
        val deviceId = getDeviceId(context)
        if (hasKeys(context, deviceId)) {
            PollingTransactionsManager.startPollingTransactions(context, deviceId, true)
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

    fun cancelTransaction(context: Context, deviceId: String, txId: String): Boolean {
        return PollingTransactionsManager.cancelTransaction(context, deviceId, txId)
    }

    fun createTransaction(context: Context, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel, callback: (response: CreateTransactionResponse?) -> Unit){
        launch {
            val deviceId = getDeviceId(context)
            val response = PollingTransactionsManager.createTransaction(context, deviceId, assetId, destAddress, amount, feeLevel)
            callback(response)
        }
    }

    fun getEstimatedFee(context: Context, assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel? = null, callback: (response: EstimatedFeeResponse?) -> Unit){
        launch {
            val deviceId = getDeviceId(context)
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
                            ),
                            getHeaders(context, deviceId)
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

    fun createAsset(context: Context, assetId: String, callback: (success: Boolean, message: String?) -> Unit) {
    Timber.i("creating $assetId asset")
        var success = false
        var message: String? = null
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val deviceId = getDeviceId(context)
                        val response = Api.with(StorageManager.get(context, deviceId)).createAsset(deviceId, assetId, getHeaders(context, deviceId)).execute()
                        logResponse("createAsset", response)
                        success = response.isSuccessful
                        if (!success) {
                            message = ResponseErrorUtil.parseErrorMessage(response.errorBody()?.string())
                        }
                    }.onFailure {
                        Timber.e(it, "Failed to call createAsset API")
                    }
                    callback(success, message)
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
                        val deviceId = getDeviceId(context)
                        val response = Api.with(StorageManager.get(context, deviceId)).getAssetsSummary(deviceId, getHeaders(context, deviceId)).execute()
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
                                    asset.assetAddress = assetAddress.address
                                    asset.accountId = assetAddress.accountId
                                    asset.addressIndex = assetAddress.addressIndex
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
                        val deviceId = getDeviceId(context)
                        val response = Api.with(StorageManager.get(context, deviceId)).getSupportedAssets(deviceId, getHeaders(context, deviceId)).execute()
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

    fun getLatestBackupInfo(context: Context, deviceId: String, walletId: String, useDefaultEnv: Boolean = false, callback: ((result: BackupInfo?) -> Unit)) {
        var backupInfo: BackupInfo? = null
        launch {
            runBlocking {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val headerProvider = when(useDefaultEnv) {
                            true -> getDefaultHeaderProvider(context)
                            else -> StorageManager.get(context, deviceId)
                        }
                        val response = Api.with(headerProvider).getLatestBackupInfo(walletId, getHeaders(context)).execute()
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
                        val response = Api.with(getDefaultHeaderProvider(context)).getDevices(getHeaders(context)).execute()
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
            val deviceId = getDeviceId(context)
            val response = Api.with(StorageManager.get(context, deviceId)).getPassphraseInfos(getHeaders(context, deviceId)).execute()
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
            val deviceId = getDeviceId(context)
            val response = Api.with(StorageManager.get(context, deviceId)).createPassphraseInfo(passphraseId, body = PassphraseInfo(location = passphraseLocation), getHeaders(context, deviceId)).execute()
            success = response.isSuccessful
            logResponse("createPassphraseInfo", response)
        }.onFailure {
            Timber.w(it, "Failed to call createPassphraseInfo API")
        }
        callback.invoke(success)
    }

    private fun logResponse(apiName: String, response: Response<*>) {
        if (isDebugLog()) {
            Timber.d("got response from $apiName rest API code:${response.code()}, isSuccessful:${response.isSuccessful} body: ${response.body()}")
        }
    }

    fun deleteWallet(context: Context) {
        MultiDeviceManager.instance.lastUsedDeviceId(context)?.let {
            StorageManager.get(context, it).clear()
        }
        MultiDeviceManager.instance.deleteLastUsedDevice(context)
        getTempDeviceId().let {
            StorageManager.get(context, it).clear()
        }
        MultiDeviceManager.instance.clearTempDeviceId()
    }
}