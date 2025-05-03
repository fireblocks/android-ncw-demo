package com.fireblocks.sdkdemo.ui.compose.utils

import android.content.Context
import android.content.res.Resources
import com.fireblocks.sdkdemo.R
import java.util.Locale

object ImageUtils {

    fun getIcon(context: Context, id: String, placeholderResId: Int): Int {
        return when (id) {
            "btc" -> R.drawable.ic_btc
            "ada" -> R.drawable.ic_ada
            "avax" -> R.drawable.ic_avax
            "matic" -> R.drawable.ic_matic
            "usdt" -> R.drawable.ic_usdt
            "usdc" -> R.drawable.ic_usdc
            "dai" -> R.drawable.ic_dai
            "shib" -> R.drawable.ic_shib
            "uni" -> R.drawable.ic_uni
            "eth", "weth" -> R.drawable.ic_eth
            "xrp" -> R.drawable.ic_xrp
            "dot" -> R.drawable.ic_dot
            "sol" -> R.drawable.ic_sol
            "celo" -> R.drawable.ic_celo
            "basechain" -> R.drawable.ic_base
            "etherlink" -> R.drawable.ic_etherlink
            else -> run {
                val resources: Resources = context.resources
                val resourceId: Int = resources.getIdentifier("ic_${id.lowercase(Locale.ENGLISH)}", null, context.packageName)
                if (resourceId > 0) {
                    return resourceId
                }
                return placeholderResId
            }
        }
    }
}