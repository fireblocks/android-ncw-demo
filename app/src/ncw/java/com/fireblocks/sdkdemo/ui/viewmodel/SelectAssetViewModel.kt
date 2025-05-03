package com.fireblocks.sdkdemo.ui.viewmodel

import android.content.Context
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.ui.main.UiState

/**
 * Created by Fireblocks Ltd. on 05/10/2023.
 */
class SelectAssetViewModel: BaseSelectAssetViewModel() {

    override fun loadAssets(context: Context, state: UiState) {
        updateUserFlow(state)
        runCatching {
            FireblocksManager.getInstance().getSupportedAssets(context) { assets ->
                showProgress(false)
                onAssets(assets = assets)
            }
        }.onFailure {
            showProgress(false)
        }
    }

    override fun addAssetToWallet(context: Context, assetId: String) {
        showProgress(true)
        runCatching {
            FireblocksManager.getInstance().createAsset(context, assetId) { success, message ->
                showProgress(false)
                if (!success) {
                    if (message.isNullOrEmpty()) {
                        showError()
                    } else {
                        showError(message = message)
                    }
                }
                onAssetAdded(success)
            }
        }.onFailure {
            showError(it)
        }
    }
}