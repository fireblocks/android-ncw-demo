package com.fireblocks.sdkdemo.bl.useraction

import com.google.gson.Gson
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
abstract class UserActionImpl : UserAction {
    @Transient
    val gson: Gson = Gson()

    private fun commandString() = gson.toJson(this)

    @Transient
    open val showLog = false

    @Transient
    open val logForCrash = true

    override fun execute() {
//        commandString().forTests()
        if (showLog) {
            Timber.d("$command: $data")
        }
//        if (logForCrash) {
//            crashlyticsLog("$command: $data")
//        }
    }

}

fun String.forTests() {
    Timber.v(this)
}