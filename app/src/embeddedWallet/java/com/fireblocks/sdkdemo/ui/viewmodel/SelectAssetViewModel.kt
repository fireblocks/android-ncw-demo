package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.main.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Created by Fireblocks Ltd. on 05/10/2023.
 */
class SelectAssetViewModel : BaseSelectAssetViewModel(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun loadAssets(context: Context, state: UiState) {
        updateUserFlow(state)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().getSupportedAssets(viewModel = this@SelectAssetViewModel).onSuccess { response ->
                    showProgress(false)
                    val assets = arrayListOf<SupportedAsset>()
                    response.data?.forEach {
                        assets.add(SupportedAsset(it))
                    }
                    onAssets(assets = assets)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }

    override fun addAssetToWallet(context: Context, assetId: String) {
        showProgress(true)
        launch {
            withContext(coroutineContext) {
                FireblocksManager.getInstance().addAsset(assetId = assetId, viewModel = this@SelectAssetViewModel).onSuccess { assetAddress ->
                    showProgress(false)
                    onAssetAdded(true)
                }.onFailure {
                    showError(it)
                }
            }
        }
    }
}
