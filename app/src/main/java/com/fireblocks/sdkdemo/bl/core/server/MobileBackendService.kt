package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.bl.core.server.models.AssignResponse
import com.fireblocks.sdkdemo.bl.core.server.models.CreateTransactionResponse
import com.fireblocks.sdkdemo.bl.core.server.models.GetDevicesResponse
import com.fireblocks.sdkdemo.bl.core.server.models.MessageResponse
import com.fireblocks.sdkdemo.bl.core.server.models.TransactionResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.AssetAddress
import com.fireblocks.sdkdemo.bl.core.storage.models.AssetBalance
import com.fireblocks.sdkdemo.bl.core.storage.models.AssetsSummary
import com.fireblocks.sdkdemo.bl.core.storage.models.EstimatedFeeResponse
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
interface MobileBackendService {

    @POST("/api/login")
    fun login(): Call<String>

    @POST("/api/devices/{deviceId}/assign")
    fun assign(@Path("deviceId") deviceId: String): Call<AssignResponse>

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/devices/{deviceId}/rpc")
    fun invoke(@Path("deviceId") deviceId: String, @Body body: MessageBody): Call<String>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/messages")
    fun getMessages(@Path("deviceId") deviceId: String, @Query("physicalDeviceId") physicalDeviceId: String): Call<ArrayList<MessageResponse>>

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @DELETE("/api/devices/{deviceId}/messages/{messageId}")
    fun deleteMessage(@Path("deviceId") deviceId: String, @Path("messageId") messageId: String): Call<String>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/transactions")
    fun getTransactions(@Path("deviceId") deviceId: String,
                        @Query("startDate") startDate: Long,
                        @Query("status") statuses: List<String>,
                        @Query("poll") poll: Boolean = true,
                        @Query("details") details: Boolean = true): Call<ArrayList<TransactionResponse>>

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/devices/{deviceId}/transactions/{txId}/cancel")
    fun cancelTransaction(@Path("deviceId") deviceId: String, @Path("txId") txId: String): Call<String>

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/devices/{deviceId}/transactions") // pass estimateFee = true when you only want to get the Fee
    fun createTransaction(@Path("deviceId") deviceId: String, @Body body: CreateTransactionRequestBody): Call<CreateTransactionResponse>

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/devices/{deviceId}/transactions") // pass estimateFee = true when you only want to get the Fee
    fun getEstimatedFee(@Path("deviceId") deviceId: String, @Body body: EstimatedFeeRequestBody): Call<EstimatedFeeResponse>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/devices/{deviceId}/accounts/0/assets/{assetId}")
    fun createAsset(@Path("deviceId") deviceId: String, @Path("assetId") assetId: String): Call<String>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/accounts/0/assets")
    fun getAssets(@Path("deviceId") deviceId: String): Call<ArrayList<SupportedAsset>>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/accounts/0/assets/supported_assets")
    fun getSupportedAssets(@Path("deviceId") deviceId: String): Call<ArrayList<SupportedAsset>>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/accounts/0/assets/{assetId}/balance")
    fun getAssetBalance(@Path("deviceId") deviceId: String, @Path("assetId") assetId: String): Call<AssetBalance>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/accounts/0/assets/{assetId}/address")
    fun getAssetAddress(@Path("deviceId") deviceId: String, @Path("assetId") assetId: String): Call<AssetAddress>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices")
    fun getDevices(): Call<GetDevicesResponse>

    @RequestTimeout(readTimeout = 30, unit = TimeUnit.SECONDS)
    @GET("/api/devices/{deviceId}/accounts/0/assets/summary")
    fun getAssetsSummary(@Path("deviceId") deviceId: String): Call<Map<String, AssetsSummary>>

}

