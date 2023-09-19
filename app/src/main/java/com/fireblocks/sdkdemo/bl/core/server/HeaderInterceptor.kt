package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class HeaderInterceptor(private val headerProvider: HeaderProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Content-Type", "application/json")
        val context = headerProvider.context().applicationContext

        runCatching {
            runBlocking {
                val idToken = SignInUtil.getInstance().getIdToken(context)
                requestBuilder.header("Authorization", "Bearer $idToken")
            }
        }.onFailure {
            Timber.w("${headerProvider.deviceId()} - ${it.cause} - failed to add idToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}