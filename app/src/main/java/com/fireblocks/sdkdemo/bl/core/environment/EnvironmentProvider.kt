package com.fireblocks.sdkdemo.bl.core.environment

import android.content.Context
import com.fireblocks.sdkdemo.prefs.base.Preference
import com.fireblocks.sdkdemo.prefs.preferences.StringPreference

/**
 * Created by Fireblocks ltd. on 18/09/2023
 */
class EnvironmentProvider private constructor() {

    fun environment(context: Context, deviceId: String): Environment {
        val env = getPref(context, getKey(deviceId)).value()
        return environments.firstOrNull { it.env() == env } ?: EMPTY
    }

    fun setEnvironment(context: Context, deviceId: String, environment: Environment) {
        getPref(context, getKey(deviceId)).set(environment.env())
    }

    fun removeEnvironment(context: Context, deviceId: String) {
        getPref(context, deviceId).remove()
    }

    private fun getKey(deviceId: String) = "$deviceId-environment"

    private fun getPref(context: Context, key: String): Preference<String> {
        return StringPreference(context, Environment, getKey(key), "")
    }

    companion object {
        const val Environment = "Environment"
        @Volatile
        private var instance: EnvironmentProvider? = null
        private val environments = arrayListOf<Environment>()
        @JvmStatic
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: EnvironmentProvider().also { instance = it }
                }

        fun setAvailableEnvironments(envs: ArrayList<Environment>) {
            environments.apply {
                clear()
                addAll(envs)
            }
        }

        fun availableEnvironments(): ArrayList<Environment> {
            return environments
        }

        val EMPTY = object : Environment {
            override fun env(): String {
                return ""
            }

            override fun host(): String {
                  return ""
            }

            override fun getLogTag(): String {
                return "EMPTY"
            }

            override fun envIndicator(): String {
                return "empty"
            }

            override fun isDefault(): Boolean {
                return false
            }
        }
    }
}