package com.fireblocks.sdkdemo.ui.screens.adddevice

import android.util.Base64
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 26/12/2023.
 */
data class JoinRequestData(@SerializedName("requestId") val requestId: String? = "",
                           @SerializedName("platform") var platform: AddDeviceViewModel.Platform? = AddDeviceViewModel.Platform.UNKNOWN,
                           @SerializedName("email") val email: String? = "") {


    fun encode(): String {
        return Base64.encodeToString(toJson().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
    private fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun decode(encodedData: String): JoinRequestData? {
            var data:JoinRequestData? = null
            runCatching {
                val decodedData = Base64.decode(encodedData, Base64.NO_WRAP).toString(Charsets.UTF_8)
                data = Gson().fromJson(decodedData, JoinRequestData::class.java)
            }.onFailure {
                Timber.e(it)
            }
            return data
        }
    }
}