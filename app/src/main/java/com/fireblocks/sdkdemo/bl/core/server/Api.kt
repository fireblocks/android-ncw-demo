package com.fireblocks.sdkdemo.bl.core.server

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.bl.core.environment.environment
import com.fireblocks.sdkdemo.log.HttpLoggingInterceptor
import com.fireblocks.sdkdemo.log.TimberLogTree
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
object Api {


    @VisibleForTesting
    @Keep
    var test_Interceptor: Interceptor? = null

    fun with(headerProvider: HeaderProvider): MobileBackendService {
        val host = headerProvider.environment().host()
        return getService(headerProvider, host)
    }

    fun with(headerProvider: HeaderProvider, host: String): MobileBackendService {
        return getService(headerProvider, host)
    }

    private fun getService(headerProvider: HeaderProvider, host: String): MobileBackendService {
        if (host.isEmpty()){
            throw RuntimeException("Missing Host url")
        }

        val clientBuilder = OkHttpClient.Builder() //
        clientBuilder.apply {
            addInterceptor(TimeoutInterceptor())
            addInterceptor(HeaderInterceptor(headerProvider)) //
            if (test_Interceptor == null) {
                if(FireblocksManager.getInstance().isDebugLog()) {
                    val loggingInterceptor = HttpLoggingInterceptor(headerProvider.deviceId(), TimberLogTree())
                    addInterceptor(loggingInterceptor) //
                    addInterceptor(ResponseInterceptor())
                }
            } else {
                addNetworkInterceptor(test_Interceptor!!)
                addInterceptor(test_Interceptor!!)
            }
        }

        val client = clientBuilder.build()
        val retrofit = Retrofit.Builder().baseUrl(host) //
                .addConverterFactory(CompositeConverterFactory()) //
                .client(client)
                .build()

        return retrofit.create(MobileBackendService::class.java)
    }

}