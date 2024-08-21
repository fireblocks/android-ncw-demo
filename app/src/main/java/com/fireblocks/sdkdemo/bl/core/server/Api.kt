package com.fireblocks.sdkdemo.bl.core.server

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.bl.core.extensions.isDebugLog
import com.fireblocks.sdkdemo.log.HttpLoggingInterceptor
import com.fireblocks.sdkdemo.log.TimberLogTree
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
object Api {

    private var connectionPool = ConnectionPool(4, 15, TimeUnit.SECONDS)
    private val services = hashMapOf<String, MobileBackendService>()

    fun with(headerProvider: HeaderProvider): MobileBackendService {
        val host = headerProvider.environment().host()
        return getService(headerProvider, host)
    }

    fun with(headerProvider: HeaderProvider, host: String): MobileBackendService {
        return getService(headerProvider, host)
    }

    private fun getService(headerProvider: HeaderProvider, host: String): MobileBackendService {
        if (host.isEmpty()) {
            throw RuntimeException("Missing Host url")
        }
        val deviceId = headerProvider.deviceId()
        var mobileBackendService = services[deviceId]
        if (mobileBackendService != null) {
            return mobileBackendService
        }

        val clientBuilder = OkHttpClient.Builder() //
        clientBuilder.apply {
            connectTimeout(30, TimeUnit.SECONDS) //
            readTimeout(30, TimeUnit.SECONDS) //
//            retryOnConnectionFailure(true) //
//            addInterceptor(TimeoutInterceptor())
//            addInterceptor(HeaderInterceptor(headerProvider)) //
            if (isDebugLog()) {
                val loggingInterceptor = HttpLoggingInterceptor(headerProvider.deviceId(), TimberLogTree())
                addInterceptor(loggingInterceptor) //
                addInterceptor(ResponseInterceptor())
            }
        }
        clientBuilder.connectionPool(connectionPool)

        val sSLSocketFactoryTcpNoDelay = SSLSocketFactoryTcpNoDelay()
        clientBuilder.sslSocketFactory(sSLSocketFactoryTcpNoDelay.sslSocketFactory, sSLSocketFactoryTcpNoDelay.trustManager)

        val client = clientBuilder.build()
        val retrofit = Retrofit.Builder().baseUrl(host) //
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        mobileBackendService = retrofit.create(MobileBackendService::class.java)
        services[deviceId] = mobileBackendService
        return mobileBackendService
    }
}