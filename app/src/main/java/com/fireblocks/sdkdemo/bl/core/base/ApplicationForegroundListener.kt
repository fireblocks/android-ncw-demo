package com.fireblocks.sdkdemo.bl.core.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Fireblocks Ltd. on 17/09/2020
 */
class ApplicationForegroundListener : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        activityCounter.incrementAndGet()
        if (isApplicationForeground()) {
            applicationListeners.forEach {
                it.onApplicationResumed()
            }
        }
        if (activity is ComponentActivity) {
            weakActivity = WeakReference(activity)
            postAllActions(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        activityCounter.decrementAndGet()
        if (isApplicationBackground()) {
            applicationListeners.forEach {
                it.onApplicationPaused()
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        private val activityCounter: AtomicInteger = AtomicInteger(0)
        internal val activityActions = ArrayList<OnActivityAction>()
        internal var weakActivity: WeakReference<ComponentActivity>? = null
        internal val applicationListeners = HashSet<ApplicationStateListener>()
        fun isApplicationForeground(): Boolean {
            return activityCounter.get() == 1
        }

        fun isApplicationBackground(): Boolean {
            return activityCounter.get() == 0
        }

        fun addApplicationListener(listener: ApplicationStateListener) {
            applicationListeners.add(listener)
        }

        fun removeApplicationListener(listener: ApplicationStateListener) {
            applicationListeners.remove(listener)
        }
    }

    private fun postAllActions(activity: Activity) {
        val iterator = activityActions.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            activity.postOnActivity(next)
            iterator.remove()
        }
    }
}

fun Activity.postOnActivity(activityAction: OnActivityAction) {
    if (this is ComponentActivity && ApplicationForegroundListener.isApplicationForeground()) {
        activityAction.onActivityAvailable(this)
    } else {
        ApplicationForegroundListener.activityActions.add(activityAction)
    }
}

fun postOnActivity(activityAction: OnActivityAction) {
    ApplicationForegroundListener.apply {
        weakActivity?.get()?.apply {
            this.postOnActivity(activityAction)
        } ?: run {
            activityActions.add(activityAction)
        }
    }
}

fun getActivity(): ComponentActivity? {
    ApplicationForegroundListener.apply {
        return weakActivity?.get()
    }
}