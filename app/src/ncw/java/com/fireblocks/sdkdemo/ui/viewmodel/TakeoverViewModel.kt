package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.DerivationParams
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdkdemo.FireblocksManager
import java.util.concurrent.CountDownLatch

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
class TakeoverViewModel : BaseTakeoverViewModel() {

    override fun loadAssets(context: Context, takeoverResult: Set<FullKey>) {
        showProgress(true)
        runCatching {
            val fireblocksManager = FireblocksManager.getInstance()
            fireblocksManager.getAssetsSummary(context) { assets ->
                takeoverResult.forEach { fullKey ->
                    if (fullKey.algorithm == Algorithm.MPC_ECDSA_SECP256K1) {
                        fullKey.privateKey?.let { privateKey ->
                            assets.forEach { asset ->
                                if (asset.algorithm == fullKey.algorithm) {
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
                onAssets(assets = assets)
            }
        }.onFailure {
            onError(true)
        }
    }
}