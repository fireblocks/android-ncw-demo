package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.transparent

@Composable
fun CryptoIconCard(iconUrl: String? = null, symbol: String, blockchain: String? = null,
                   imageSizeResId: Int = R.dimen.image_size,
                   blockchainImageSizeResId: Int = R.dimen.image_size_very_small,) {
    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_tiny))
            .height(dimensionResource(id = imageSizeResId))
            .width(dimensionResource(id = imageSizeResId))
        ,
        colors = CardDefaults.cardColors(containerColor = transparent),
        shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_medium))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CryptoIcon(
                imageSizeResId = imageSizeResId,
                iconUrl = iconUrl,
                symbol = symbol,
                paddingResId = R.dimen.padding_tiny,
            )

            if (blockchain != null) {
                Box(modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .border(1.dp, grey_1, CircleShape)
                    .clip(CircleShape)
                ) {
                    CryptoIcon(
                        symbol = blockchain,
                        imageSizeResId = blockchainImageSizeResId,
                        paddingResId = R.dimen.padding_none,
                        placeholderResId = R.drawable.ic_blockchain_placeholder
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CryptoIconCardPreview() {
    FireblocksNCWDemoTheme {
        CryptoIconCard(symbol = "", blockchain = "ETH_TEST5")
    }
}