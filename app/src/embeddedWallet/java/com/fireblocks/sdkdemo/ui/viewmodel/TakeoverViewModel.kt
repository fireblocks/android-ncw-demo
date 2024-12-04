package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.DerivationParams
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
class TakeoverViewModel : BaseTakeoverViewModel(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun loadAssets(context: Context, takeoverResult: Set<FullKey>) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                runCatching {
                    val fireblocksManager = FireblocksManager.getInstance()
                    fireblocksManager.getAssets(viewModel = this@TakeoverViewModel).onSuccess { paginatedResponse ->
                        Timber.i("Assets loaded: $paginatedResponse")
                        val supportedAssets = paginatedResponse.data?.map { asset -> SupportedAsset(asset = asset) } ?: arrayListOf()
                        takeoverResult.forEach { fullKey ->
                            if (fullKey.algorithm == Algorithm.MPC_ECDSA_SECP256K1) {
                                fullKey.privateKey?.let { privateKey ->
                                    supportedAssets.forEach { asset ->
                                        if (asset.algorithm == fullKey.algorithm) {
                                            val addressesDeferred = async {
                                                fireblocksManager.getAssetAddresses(context, assetId = asset.id, viewModel = this@TakeoverViewModel).onSuccess { paginatedResponse ->
                                                    Timber.d("Asset addresses loaded: $paginatedResponse")
                                                    val assetAddress = paginatedResponse.data?.firstOrNull()
                                                    asset.assetAddress = assetAddress?.address
                                                    asset.accountId = assetAddress?.accountId
                                                    asset.addressIndex = assetAddress?.addressIndex
                                                    assetAddress?.let { fireblocksManager.saveAssetAddress(context, assetId = asset.id, assetAddress = it) }
                                                }
                                            }
                                            addressesDeferred.await()

                                            val derivationParams = DerivationParams(
                                                account = asset.accountId?.toInt() ?: 0,
                                                coinType = asset.coinType ?: 0,
                                                change = 0,
                                                index = asset.addressIndex?.toInt() ?: 0)

                                            val latch = CountDownLatch(1)
                                            fireblocksManager.deriveAssetKey(context, privateKey, derivationParams) { keyData ->
                                                asset.derivedAssetKey = keyData
                                                keyData.data?.let { privateKey ->
                                                    // check if the asset is BTC
                                                    if (asset.id.contains("BTC")) {
                                                        asset.wif = fireblocksManager.getWif(privateKey)
                                                    }
                                                }
                                                latch.countDown()
                                            }
                                            latch.await()
                                        }
                                    }
                                }
                            }
                        }

                        showProgress(false)
                        onAssets(assets = supportedAssets)
                    }
                }

            }.onFailure {
                onError(true)
            }
        }
    }
}