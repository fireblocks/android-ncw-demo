package com.fireblocks.sdkdemo.bl.core.server

import android.content.Context
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
        requestBuilder
            .header("Content-Type", "application/json")
            .header("Connection", "Keep-Alive")

        val context = headerProvider.context().applicationContext

        runCatching {
            val idToken = runBlocking {
                SignInUtil.getInstance().getIdTokenBlocking(context)
            }
            requestBuilder.header("Authorization", "Bearer $idToken")
        }.onFailure {
            Timber.w("${headerProvider.deviceId()} - ${it.cause} - failed to add idToken")
        }

        return chain.proceed(requestBuilder.build())
    }

    companion object {
        private var headers: HashMap<String, String>? = null
        fun getHeaders(context: Context, deviceId: String? = null): HashMap<String, String> {
            if (headers == null) {
                headers = hashMapOf<String, String>().apply {
                    put("Content-Type", "application/json")
                    put("Connection", "Keep-Alive")
                }
            }
            runCatching {
                val idToken = runBlocking {
                    SignInUtil.getInstance().getIdTokenBlocking(context)
                }
                headers?.set("Authorization", "Bearer $idToken")
            }.onFailure {
                Timber.w("$deviceId - ${it.cause} - failed to add idToken")
            }
            return headers as HashMap<String, String>
        }
    }
}