package com.fireblocks.sdkdemo.bl.core.server

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.log.HttpLoggingInterceptor
import com.fireblocks.sdkdemo.log.TimberLogTree
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
object Api {


    @VisibleForTesting
    @Keep
    var test_Interceptor: Interceptor? = null
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
            addInterceptor(HeaderInterceptor(headerProvider)) //
            if (test_Interceptor == null) {
                if (FireblocksManager.getInstance().isDebugLog()) {
                    val loggingInterceptor = HttpLoggingInterceptor(headerProvider.deviceId(), TimberLogTree())
                    addInterceptor(loggingInterceptor) //
                    addInterceptor(ResponseInterceptor())
                }
            } else {
                addNetworkInterceptor(test_Interceptor!!)
                addInterceptor(test_Interceptor!!)
            }
        }
        clientBuilder.connectionPool(connectionPool)

        val client = clientBuilder.build()
        val retrofit = Retrofit.Builder().baseUrl(host) //
            .addConverterFactory(CompositeConverterFactory()) //
            .client(client)
            .build()

        mobileBackendService = retrofit.create(MobileBackendService::class.java)
        services[deviceId] = mobileBackendService
        return mobileBackendService
    }
}