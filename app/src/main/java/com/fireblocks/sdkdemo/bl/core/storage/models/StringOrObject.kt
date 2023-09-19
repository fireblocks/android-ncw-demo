package com.fireblocks.sdkdemo.bl.core.storage.models

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 10/19/21
 */
data class StringOrObject(private val string: String? = null, private val jsonObject: JsonObject? = null): java.io.Serializable {

    private val gson = GsonBuilder().setPrettyPrinting().create()


    fun getAttribute(path: String?): String? {
        if(string != null) {
            return string
        }
        return gson.toJson(jsonObject?.get(path))
    }

    override fun toString(): String {
        if (string != null) {
            return string
        }

        return jsonObject.toString()
    }

    companion object {
        val gsonTypeadapter = object : TypeAdapter<StringOrObject>() {
            override fun write(out: JsonWriter?, value: StringOrObject?) {
                out?.value(value?.string)
            }

            override fun read(reader: JsonReader?): StringOrObject {
                val path = reader?.path
                return try {
                    val parser = JsonParser.parseReader(reader)
                    if (parser.isJsonObject) {
                        val obj = parser.asJsonObject
                        return StringOrObject(jsonObject = obj)
                    }

                    runCatching {
                        val string = parser.asString
                        if (string != null) {
                            StringOrObject(string)
                        } else {
                            StringOrObject()
                        }
                    }.getOrElse {
                        StringOrObject()
                    }
                } catch (e: Exception) {
                    Timber.w("Expected string or object but got something else on path: $path ")
                    StringOrObject()
                }
            }
        }

    }
}