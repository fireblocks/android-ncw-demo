package com.fireblocks.sdkdemo.bl.core.server

import com.fireblocks.sdkdemo.prefs.preferences.type
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class CompositeConverterFactory : Converter.Factory() {
    private val gsonConverterFactory = GsonConverterFactory.create()
    private val stringConverterFactory = StringConverterFactory()

    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        if ("".type() == type) {
            return stringConverterFactory.responseBodyConverter(type,annotations,retrofit)
        }
        return gsonConverterFactory.responseBodyConverter(type, annotations,retrofit)
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<out Annotation>,
                                      methodAnnotations: Array<out Annotation>,
                                      retrofit: Retrofit): Converter<*, RequestBody>? {
        return gsonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    override fun stringConverter(type: Type,
                                 annotations: Array<Annotation>,
                                 retrofit: Retrofit): Converter<*, String>? {
        return gsonConverterFactory.stringConverter(type, annotations, retrofit)
    }


}