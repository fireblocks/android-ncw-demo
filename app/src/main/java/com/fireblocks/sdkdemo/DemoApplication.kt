package com.fireblocks.sdkdemo

import android.app.Application
import com.fireblocks.sdkdemo.bl.core.base.ApplicationForegroundListener
import com.fireblocks.sdkdemo.bl.core.base.ApplicationStateListener
import com.fireblocks.sdkdemo.bl.core.extensions.getLogLevel
import com.fireblocks.sdkdemo.bl.useraction.ApplicationPaused
import com.fireblocks.sdkdemo.bl.useraction.ApplicationResumed
import com.fireblocks.sdkdemo.log.TimberLogTree
import com.fireblocks.sdkdemo.log.filelogger.CommaFormatter
import com.fireblocks.sdkdemo.log.filelogger.FileLoggerTree
import com.fireblocks.sdkdemo.log.filelogger.PriorityFilter
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 18/09/2023.
 */
class DemoApplication : Application(), ApplicationStateListener {


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ApplicationForegroundListener())
        ApplicationForegroundListener.addApplicationlistener(this)

        initTimber()
        FireblocksManager.getInstance().setupEnvironmentsAndDevice(applicationContext)
    }

    private fun initTimber() {
        val logLevel = getLogLevel()
        Timber.plant(TimberLogTree(PriorityFilter(logLevel)))
        Timber.plant(FileLoggerTree.Builder() //
            .appendToFile(true) //
            .file(filesDir) //
            .filename("demo_log%g.log") //
            .filter(PriorityFilter(logLevel)) //
            .fileLimit(2) //
            .formatter(CommaFormatter.instance) //
            .build())
    }

    override fun onApplicationResumed() {
        ApplicationResumed().execute()
    }

    override fun onApplicationPaused() {
        ApplicationPaused().execute()
    }
}