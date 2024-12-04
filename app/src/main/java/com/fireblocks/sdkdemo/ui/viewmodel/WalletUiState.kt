package com.fireblocks.sdkdemo.ui.viewmodel

import com.fireblocks.sdk.transactions.TransactionSignature
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.bl.core.storage.models.TransactionWrapper

/**
 * Created by Fireblocks Ltd. on 26/11/2024.
 */
data class WalletUiState(
    val assets: List<SupportedAsset> = arrayListOf(),
    val balance: String = "0",
    val selectedAsset: SupportedAsset? = null,
    val assetAmount: String = "0",
    val assetUsdAmount: String = "0",
    val sendDestinationAddress: String = "",
    val selectedFeeData: FeeData? = null,
    val createdTransactionId: String? = null,
    val createdTransaction: Boolean = false,
    val transactionWrapper: TransactionWrapper? = null,
    val transactionSignature: TransactionSignature? = null,
    val sendFlow: Boolean = false,
    val closeWarningClicked: Boolean = false,
    val transactionCanceled: Boolean = false,
    val transactionCancelFailed: Boolean = false,
    val estimatedFee : Fee? = null,
    val showFeeError: Boolean = false,
    val showPendingSignatureError: Boolean = false,
    val createdTransactionStatus: SigningStatus? = null
)
