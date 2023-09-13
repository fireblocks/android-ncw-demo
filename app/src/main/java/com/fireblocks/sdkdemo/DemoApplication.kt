package com.fireblocks.sdkdemo

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fireblocks.sdkdemo.bl.core.base.ApplicationForegroundListener
import com.fireblocks.sdkdemo.bl.core.base.ApplicationStateListener
import com.fireblocks.sdkdemo.bl.core.base.OnActivityAction
import com.fireblocks.sdkdemo.bl.core.base.postOnActivity
import com.fireblocks.sdkdemo.bl.useraction.ApplicationPaused
import com.fireblocks.sdkdemo.bl.useraction.ApplicationResumed
import com.fireblocks.sdkdemo.log.TimberLogTree
import com.fireblocks.sdkdemo.log.filelogger.CommaFormatter
import com.fireblocks.sdkdemo.log.filelogger.FileLoggerTree
import timber.log.Timber

/**
 *
 */
class DemoApplication : Application(), ApplicationStateListener {


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ApplicationForegroundListener())
        ApplicationForegroundListener.addApplicationlistener(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true) // recommended my the Auth library

        initTimber()
        FireblocksManager.getInstance().setupEnvironmentsAndDevice(applicationContext)
    }

    private fun initTimber() {
        Timber.plant(TimberLogTree())
        Timber.plant(FileLoggerTree.Builder() //
            .appendToFile(true) //
            .file(filesDir) //
            .filename("demo_log%g.log") //
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