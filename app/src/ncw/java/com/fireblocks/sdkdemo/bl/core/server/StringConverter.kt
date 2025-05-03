package com.fireblocks.sdkdemo.bl.core.server

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
class StringConverter : Converter<ResponseBody,String> {

    override fun convert(value: ResponseBody): String? {
        return value.string()
    }
}
class StringConverterFactory: Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return StringConverter()
    }
}
