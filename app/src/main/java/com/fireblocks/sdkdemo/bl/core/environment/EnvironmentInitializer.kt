package com.fireblocks.sdkdemo.bl.core.environment

import android.content.Context
import android.content.res.AssetManager
import com.fireblocks.sdkdemo.bl.core.cryptocurrency.CryptoCurrencyProvider
import com.google.gson.Gson
import timber.log.Timber
import java.io.IOException

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
object EnvironmentInitializer {

    fun initialize(context: Context): Boolean {
        EnvironmentProvider.setAvailableEnvironments(loadEnvs(context))
        CryptoCurrencyProvider.loadCryptoCurrencyData(context)
        Timber.i("Environments initialized")
        return true
    }

    @Throws(IOException::class)
    private fun loadEnvs(context: Context): ArrayList<Environment> {
        val gson = Gson()
        val environments = arrayListOf<Environment>()
        val environmentsFolder = context.assets.list("envs")

        environmentsFolder?.forEach { environmentFolder ->
            val path = "envs/$environmentFolder"
            val resourcesInFolder = context.assets.list(path)
            val expectedFiles = arrayListOf("fireblocks_data.json")
            expectedFiles.forEach {
                if (!resourcesInFolder!!.contains(it)) {
                    throw RuntimeException("expected to find $it in $$path")
                }
            }

            context.assets?.apply {
                val fireblocksDataString = readTextFile(this, "$path/fireblocks_data.json")
                val fireblocksData = gson.fromJson(fireblocksDataString, FireblocksJsonFile::class.java)

                if (fireblocksData.host.isEmpty()) {
                    throw RuntimeException("expected to find host in $path/fireblocks_data.json file")
                }

                val env = EnvironmentImpl(
                    context,
                    environmentFolder,
                    fireblocksData.isDefault,
                    fireblocksData.envIndicator,
                    fireblocksData.host,
                    fireblocksData.logTag)
                environments.add(env)
            }
        }
        return environments
    }

    private fun readTextFile(assets: AssetManager, fileName: String): String {
        val retVal = StringBuilder()
        assets.open(fileName).use { content ->
            val contentText = content.readBytes().toString(Charsets.UTF_8)
            retVal.append(contentText)
        }
        return retVal.toString()
    }
}