package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Network
import org.bitcoinj.crypto.ECKey
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

fun String.capitalizeFirstCharOnly(): String {
    if (this.isEmpty()) {
        return this
    }
    return this[0].uppercaseChar() + this.substring(1)
}

fun String.capitalizeFirstLetter(): String {
    if(this.isEmpty()) {
        return this
    }
    return this.capitalizeFirstChar()!!
}

fun String.beautifySigningStatus(): String {
    return this.lowercase()
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.capitalizeFirstCharOnly() }
}

fun String?.capitalizeFirstChar(): String? {
    if (this.isNullOrBlank()) {
        return this
    }
    return this.lowercase(Locale.ENGLISH).replaceFirstChar(Char::titlecase)
}

fun String.roundToDecimalFormat(pattern: String = EXTENDED_PATTERN): String {
    if (this.isEmpty()) {
        return this
    }
    return this.toDouble().roundToDecimalFormat(pattern)
}

/**
 * @see <a href="https://learnmeabitcoin.com/technical/keys/private-key/wif/">WIF Private Key</a>
 * @see <a href="https://bitcoinj.org/release-notes">bitcoinj</a>
 * On maintain, a WIF should start with a K, L, or 5.
 * On testnet, a WIF should start with a c or a 9.
 */
fun String.getWIFFromPrivateKey(isMainNet: Boolean = false): String {
    val network: Network = when(isMainNet) {
        true -> BitcoinNetwork.MAINNET
        false -> BitcoinNetwork.TESTNET
    }

    // Parse the private key from hex
    val privateKey = ECKey.fromPrivate(this.hexToByteArray(), true)
    return privateKey.getPrivateKeyAsWiF(network)
}

fun String.hexToByteArray(): ByteArray {
    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}