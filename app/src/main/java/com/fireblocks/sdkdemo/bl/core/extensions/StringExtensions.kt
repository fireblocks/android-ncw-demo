package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.bitcoinj.core.DumpedPrivateKey
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import java.util.Locale

/**
 * Created by Fireblocks Ltd. on 28/03/2023.
 */
fun String.copyToClipboard(context : Context, label : String = "copied_text") {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, this)
    clipboardManager.setPrimaryClip(clipData)
}

fun String?.isNotNullAndNotEmpty(): Boolean {
    return !isNullOrEmpty()
}

fun String.capitalizeFirstLetter(): String {
    if(this.isEmpty()) {
        return this
    }
    return this.capitalizeFirstChar()!!
}

fun String?.capitalizeFirstChar(): String? {
    if (this.isNullOrBlank()) {
        return this
    }
    return this.lowercase(Locale.getDefault()).replaceFirstChar(Char::titlecase)
}

fun String.roundToDecimalFormat(pattern: String = EXTENDED_PATTERN): String {
    return this.toDouble().roundToDecimalFormat(pattern)
}

fun String.getWIFFromPrivateKey(isMainNet: Boolean = false): String {
    val privateKeyHex = this
    val networkParameters: NetworkParameters = when(isMainNet) {
        true -> MainNetParams.get()
        false -> TestNet3Params.get()
    }

    // Parse the private key from hex
    val privateKey = ECKey.fromPrivate(privateKeyHex.hexToByteArray(), true)

    // Create a DumpedPrivateKey from the private key
    val dumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters, privateKey.getPrivateKeyEncoded(networkParameters).toString())

    // Get the Wallet Import Format (WIF)
    return dumpedPrivateKey.toBase58()
}

fun String.hexToByteArray(): ByteArray {
    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}