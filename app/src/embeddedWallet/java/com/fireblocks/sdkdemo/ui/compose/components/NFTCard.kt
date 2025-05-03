package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.grey_1

@Composable
fun NFTCard(iconUrl: String?, blockchain: String? = null, blockchainSymbol: String? = null, nftName: String, balance: String, standard: String, onClick: (() -> Unit)? = null) {
    val clickable = onClick != null
    Row(
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item)),
                color = grey_1
            )
            .padding(dimensionResource(id = R.dimen.padding_default))
            .clickable(
                enabled = clickable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick ?: {}
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NFTCardDetails(iconUrl = iconUrl, blockchain = blockchain, blockchainSymbol = blockchainSymbol, nftName = nftName, balance = balance, standard = standard)
    }
}

@Composable
fun NFTWrapperCard(nftWrapper: NFTWrapper) {
    NFTCard(
        iconUrl = nftWrapper.iconUrl,
        blockchain = nftWrapper.blockchain,
        blockchainSymbol = nftWrapper.blockchainSymbol,
        nftName = nftWrapper.name ?: "",
        balance = "",
        standard = nftWrapper.standard ?: "",
    )
}

@Composable
fun NFTCardDetails(iconUrl: String?, blockchain: String? = null, blockchainSymbol: String? = null, nftName: String, balance: String, standard: String){
    Row(verticalAlignment = Alignment.CenterVertically) {
        NFTIconCard(iconUrl = iconUrl, blockchain = blockchainSymbol)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.padding_default), end = dimensionResource(R.dimen.padding_small))
        ) {
            FireblocksText(
                text = nftName,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
            )
            BulletSentence(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
                textList = listOf(blockchain, standard)
            )
        }
        BalanceView(balance)
    }
}

@Preview
@Composable
fun NFTCardPreview() {
    FireblocksNCWDemoTheme {
        NFTCard(
            iconUrl = null,
            nftName = "NFT Name",
            balance = "1",
            blockchain = "ETH",
            standard = "ERC721",
            onClick = {}
        )
    }
}

@Composable
fun BalanceView(balance: String?) {
    if (!balance.isNullOrEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = black),
            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_counter))
        ) {
            FireblocksText(
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.padding_small_1),
                    vertical = dimensionResource(R.dimen.padding_extra_small)
                ),
                text = balance,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textAlign = TextAlign.End
            )
        }
    }
}