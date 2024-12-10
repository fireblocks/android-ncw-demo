package com.fireblocks.sdkdemo.bl.core.extensions

import com.fireblocks.sdk.ew.models.EstimatedTransactionFeeResponse
import com.fireblocks.sdk.ew.models.FeeLevel
import com.fireblocks.sdkdemo.bl.core.storage.models.Fee
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData


/**
 * Created by Fireblocks Ltd. on 27/11/2024.
 */
fun convertToFeeLevel(value: String?): FeeLevel {
    return value?.let {
        try {
            FeeLevel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FeeLevel.MEDIUM
        }
    } ?: FeeLevel.MEDIUM
}

fun EstimatedTransactionFeeResponse.toFee(): Fee {
    return Fee(
        low = FeeData(low?.networkFee, feeLevel = com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel.LOW),
        medium = FeeData(medium?.networkFee, feeLevel = com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel.MEDIUM),
        high = FeeData(high?.networkFee, feeLevel = com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel.HIGH))
}