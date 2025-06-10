package com.fireblocks.sdkdemo.bl.core.server


import com.fireblocks.sdkdemo.bl.core.server.models.RegisterTokenBody
import com.fireblocks.sdkdemo.bl.core.server.models.RegisterTokenResponse
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 27/05/2025
 */
interface MobileBackendService {

    @RequestTimeout(connectTimeout = 30, readTimeout = 30, unit = TimeUnit.SECONDS)
    @POST("/api/notifications/register-token")
    fun registerToken(@Body body: RegisterTokenBody, @HeaderMap headers: Map<String, String>): Call<RegisterTokenResponse>
}

