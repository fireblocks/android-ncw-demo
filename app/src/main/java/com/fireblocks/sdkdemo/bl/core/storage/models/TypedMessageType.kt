package com.fireblocks.sdkdemo.bl.core.storage.models

import android.content.Context
import com.fireblocks.sdkdemo.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by Fireblocks Ltd. on 10/20/21
 */
enum class TypedMessageType {
    @SerializedName("EIP712")
    EIP712 {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.eip712)
        }
    },

    @SerializedName("ETH_MESSAGE")
    ETHMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.eth_message)
        }
    },

    @SerializedName("BTC_MESSAGE")
    BTCMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.btc_message)
        }
    },

    @SerializedName("LTC_MESSAGE")
    LTCMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.ltc_message)
        }
    },

    @SerializedName("DASH_MESSAGE")
    DASHMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.dash_message)
        }
    },

    @SerializedName("LUNA_MESSAGE")
    LUNAMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.terra_message)
        }
    },

    @SerializedName("LUNA_TEST_MESSAGE")
    LUNATESTMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.terra_message)
        }
    },

    @SerializedName("ALGO_MESSAGE")
    ALGOMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.algo_message)
        }
    },

    @SerializedName("SOLANA_MESSAGE")
    SOLANAMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.solana_message)
        }
    },
    @SerializedName("NEAR_MESSAGE")
    NEARMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.near_message)
        }
    },
    @SerializedName("ATOM_MESSAGE")
    ATOMMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.atom_message)
        }
    },
    @SerializedName("POLKADOT_MESSAGE")
    POLKADOTMessage {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.polkadot_message)
        }
    },
    @SerializedName("TRON_MESSAGE")
    TRONMESSAGE {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.tron_message)
        }
    },
    @SerializedName("STELLAR_MESSAGE")
    STELLARMESSAGE {
        override fun getDisplayName(context: Context): String {
            return context.getString(R.string.stellar_message)
        }
    },
    ;

    abstract fun getDisplayName(context: Context): String

    companion object {
        private val gson = Gson()
        fun ofType(name: String): TypedMessageType? {
            val json = "{\"type\":\"$name\"}"
            return kotlin.runCatching {
                gson.fromJson(json, MessageType::class.java).type
            }.getOrNull()
        }
    }
}

private data class MessageType(@SerializedName("type") val type: TypedMessageType? = null)