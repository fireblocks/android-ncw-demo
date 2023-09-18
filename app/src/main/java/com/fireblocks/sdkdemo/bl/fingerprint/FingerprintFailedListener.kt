package com.fireblocks.sdkdemo.bl.fingerprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fireblocks.sdkdemo.bl.core.base.Event

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class FingerprintFailedListener {
    private val update = MutableLiveData<Event<Boolean>>()

    fun update(): LiveData<Event<Boolean>> = update

    fun postUpdate() {
        update.value = Event(true)
    }

    companion object {
        val instance = FingerprintFailedListener()
    }
}