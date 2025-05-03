package com.fireblocks.sdkdemo.bl.core.cryptocurrency

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson

/**
 * Created by Fireblocks Ltd. on 05/12/2024.
 */
object CryptoCurrencyProvider {

    private val cryptoCurrencyMap: MutableMap<String, Double> = mutableMapOf()

    data class CryptoCurrencyData(
        val data: List<CryptoCurrency>
    )

    data class CryptoCurrency(
        val symbol: String,
        val quote: Quote
    )

    data class Quote(
        val USD: USD
    )

    data class USD(
        val price: Double
    )

    fun loadCryptoCurrencyData(context: Context) {
        val gson = Gson()
        val jsonString = readTextFile(context.assets, "cryptocurrency_data.json")
        val cryptoCurrencyData = gson.fromJson(jsonString, CryptoCurrencyData::class.java)

        cryptoCurrencyData.data.forEach { cryptoCurrency ->
            val symbol = cryptoCurrency.symbol
            val price = cryptoCurrency.quote.USD.price
            cryptoCurrencyMap[symbol] = price
        }
    }

    private fun readTextFile(assets: AssetManager, fileName: String): String {
        val retVal = StringBuilder()
        assets.open(fileName).use { content ->
            val contentText = content.readBytes().toString(Charsets.UTF_8)
            retVal.append(contentText)
        }
        return retVal.toString()
    }

    fun getCryptoCurrencyPrice(symbol: String?): Double? {
        if (symbol == null) {
            return null
        }
        val formattedSymbol = symbol.replace(Regex("(?:_?TEST\\d*$)|(?:TEST\\d*$)"), "")
        return cryptoCurrencyMap[formattedSymbol]
    }
}