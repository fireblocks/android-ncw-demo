package com.fireblocks.sdkdemo.bl.core.server

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import timber.log.Timber
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequestTimeout(val readTimeout: Int = 0,
                                val connectTimeout: Int = 0,
                                val unit: TimeUnit = TimeUnit.SECONDS)


class TimeoutInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tag: Invocation? = request.tag(Invocation::class.java)
        val method: Method? = tag?.method()
        val timeout: RequestTimeout? = method?.getAnnotation(RequestTimeout::class.java)
        timeout?.apply {
            Timber.d("setting timeout: connectTimeout: ${timeout.connectTimeout} ${timeout.unit.name} , readTimeout: ${timeout.readTimeout} ${timeout.unit.name}  for ${request.url} ")
            return chain.withReadTimeout(timeout.readTimeout, timeout.unit)//
                    .withConnectTimeout(timeout.connectTimeout, timeout.unit)//
                    .proceed(request);
        }
        return chain.proceed(request)
    }

}