package com.fireblocks.sdkdemo.bl.core.server.polling

import com.fireblocks.sdk.ew.EmbeddedWallet
import com.fireblocks.sdk.ew.models.CreateTransactionResponse
import com.fireblocks.sdk.ew.models.DestinationTransferPeerPath
import com.fireblocks.sdk.ew.models.EstimatedTransactionFeeResponse
import com.fireblocks.sdk.ew.models.FeeLevel
import com.fireblocks.sdk.ew.models.OneTimeAddress
import com.fireblocks.sdk.ew.models.PaginatedResponse
import com.fireblocks.sdk.ew.models.SourceTransferPeerPath
import com.fireblocks.sdk.ew.models.Status
import com.fireblocks.sdk.ew.models.SuccessResponse
import com.fireblocks.sdk.ew.models.TransactionOperation
import com.fireblocks.sdk.ew.models.TransactionRequest
import com.fireblocks.sdk.ew.models.TransactionResponse
import com.fireblocks.sdk.ew.models.TransferPeerPathType
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import timber.log.Timber

/**
 * Created by Ofir Barzilay on 07/10/2024.
 */
class DataRepository(private val accountId: Int, private val embeddedWallet: EmbeddedWallet) {

    suspend fun getTransactions(incoming: Boolean? = null, outgoing: Boolean? = null, after: Long, status: Status? = null, limit: Int? = null, pageCursor: String? = null): PaginatedResponse<TransactionResponse>? {
        if (isDebugLog()) {
            Timber.d("calling getTransactions API startTimeInMillis: $after, status: $status, incoming: $incoming, outgoing: $outgoing")
        }
        val result = embeddedWallet.getTransactions(incoming = incoming, outgoing = outgoing, after = after.toString(), status = status, limit = limit, pageCursor = pageCursor)
        if (isDebugLog()) {
            Timber.d("got response from getTransactions, isSuccess:${result.isSuccess}")
        }
        result.onFailure {
            Timber.w(it, "Failed to call getTransactions")
        }
        return result.getOrNull()
    }

    suspend fun getAllTransactions(incoming: Boolean? = null, outgoing: Boolean? = null, after: Long, status: Status? = null, limit: Int? = null): PaginatedResponse<TransactionResponse>? {
        val allItems = mutableListOf<TransactionResponse>()
        var pageCursor: String? = null
        do {
            val response = getTransactions(incoming = incoming, outgoing = outgoing, after = after, status = status, limit = limit, pageCursor = pageCursor)
            if (response != null) {
                response.data?.let {
                    allItems.addAll(it)
                }
                pageCursor = response.paging?.next
            }
        } while (pageCursor != null)
        return PaginatedResponse(data = allItems)
    }

    suspend fun cancelTransaction(txId: String): Result<SuccessResponse> {
        return embeddedWallet.cancelTransaction(txId)
    }

    suspend fun getTransactionById(txId: String): Result<TransactionResponse> {
        return embeddedWallet.getTransaction(txId)
    }

    suspend fun createOneTimeAddressTransaction(assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel): Result<CreateTransactionResponse> {
            val transactionRequest = TransactionRequest(
                assetId = assetId,
                source = SourceTransferPeerPath(id = accountId.toString()),
                destination = DestinationTransferPeerPath(type = TransferPeerPathType.ONE_TIME_ADDRESS, oneTimeAddress = OneTimeAddress(address = destAddress)),
                amount = amount,
                feeLevel = feeLevel)
            return embeddedWallet.createTransaction(transactionRequest)
    }

    suspend fun createContractCallTransaction(assetId: String, contractCallData: String, feeLevel: FeeLevel): Result<CreateTransactionResponse> {
        val transactionRequest = TransactionRequest(
            assetId = assetId,
            source = SourceTransferPeerPath(id = accountId.toString()),
            operation = TransactionOperation.CONTRACT_CALL,
            extraParameters = mapOf("contractCallData" to contractCallData),
            feeLevel = feeLevel) // TODO fix server error - {"message":"Cannot read properties of undefined (reading 'type')","code":1404}
        return embeddedWallet.createTransaction(transactionRequest)
    }

    suspend fun createEndUserWalletTransaction(assetId: String, destWalletId: String, amount: String, feeLevel: FeeLevel, destinationAccountId: Int): Result<CreateTransactionResponse> {
        return embeddedWallet.createTransaction(
            TransactionRequest(
                assetId = assetId,
                source = SourceTransferPeerPath(id = accountId.toString()),
                destination = DestinationTransferPeerPath(type = TransferPeerPathType.END_USER_WALLET, walletId = destWalletId, id = destinationAccountId.toString()),
                amount = amount,
                feeLevel = feeLevel))

    }

    suspend fun createVaultTransaction(assetId: String, vaultAccountId: String, amount: String, feeLevel: FeeLevel): Result<CreateTransactionResponse> {
        return embeddedWallet.createTransaction(
            TransactionRequest(
                assetId = assetId,
                source = SourceTransferPeerPath(id = accountId.toString()),
                destination = DestinationTransferPeerPath(type = TransferPeerPathType.VAULT_ACCOUNT, id = vaultAccountId),
                amount = amount,
                feeLevel = feeLevel))
    }

    suspend fun estimateTransactionFee(assetId: String, destAddress: String, amount: String, feeLevel: FeeLevel): Result<EstimatedTransactionFeeResponse> {
        val transactionRequest = TransactionRequest(
            assetId = assetId,
            source = SourceTransferPeerPath(id = accountId.toString()),
            destination = DestinationTransferPeerPath(type = TransferPeerPathType.ONE_TIME_ADDRESS, oneTimeAddress = OneTimeAddress(address = destAddress)),
            amount = amount,
            feeLevel = feeLevel)
        return embeddedWallet.estimateTransactionFee(transactionRequest)
    }
}