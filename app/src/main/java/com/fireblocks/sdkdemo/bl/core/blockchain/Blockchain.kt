package com.fireblocks.sdkdemo.bl.core.blockchain

data class Blockchain(
    val descriptor: String,
    val displayName: String,
    val blockchainProtocolId: String,
    val nativeAsset: String,
)
