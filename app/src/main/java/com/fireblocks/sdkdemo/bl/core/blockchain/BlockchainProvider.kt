package com.fireblocks.sdkdemo.bl.core.blockchain

import android.content.Context
import com.opencsv.CSVReader
import java.io.InputStreamReader

object BlockchainProvider {

    private var blockchains: List<Blockchain>? = null

    fun getBlockchains(context: Context): List<Blockchain>? {
        if (blockchains == null) {
            val blockchainList = loadCSVFromAssets(context)
            // convert to Blockchain objects
            blockchains = blockchainList.map {
                Blockchain(it[0], it[1], it[2], it[3])
            }
        }
        return blockchains
    }

    fun getBlockchain(context: Context, blockchainName: String): Blockchain? {
        val blockchainsList = getBlockchains(context)
        return blockchainsList?.find { it.descriptor == blockchainName }
    }

    fun getBlockchainDisplayName(context: Context, blockchainName: String?): String {
        return blockchainName?.let {
            val blockchain = getBlockchain(context, it)
            blockchain?.displayName ?: it
        } ?: ""
    }

    private fun loadCSVFromAssets(context: Context, fileName: String = "blockchain.csv"): List<Array<String>> {
        val csvData = mutableListOf<Array<String>>()
        context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).use { inputStreamReader ->
                CSVReader(inputStreamReader).use { csvReader ->
                    var nextLine: Array<String>?
                    csvReader.readNext() // Skip the first line (column names)
                    while (csvReader.readNext().also { nextLine = it } != null) {
                        nextLine?.let { csvData.add(it) }
                    }
                }
            }
        }
        return csvData
    }
}