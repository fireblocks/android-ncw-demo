package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_2

@Composable
fun NFTCard(iconUrl: String?) {
    val context = LocalContext.current
    val cardBackgroundColor: MutableState<Color> = remember { mutableStateOf(grey_2) }
    Card(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default))
            .height(dimensionResource(id = R.dimen.nft_card_size_list))
            .width(dimensionResource(id = R.dimen.nft_card_size_list)),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor.value),
        shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_small)
        )

    ) {
        val contentAlignment = iconUrl?.let { Alignment.BottomCenter } ?: Alignment.Center
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = contentAlignment
        ) {
            NFTIcon(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                context = context,
                iconUrl = iconUrl,
                onDominantColorExtracted = { color ->
                    cardBackgroundColor.value = color
                })
        }
    }
}

@Preview
@Composable
fun NFTCardPreview() {
    FireblocksNCWDemoTheme {
        NFTCard(iconUrl = "https://fireblocks-sdk-demo.s3.amazonaws.com/nft/1.png")
    }
}