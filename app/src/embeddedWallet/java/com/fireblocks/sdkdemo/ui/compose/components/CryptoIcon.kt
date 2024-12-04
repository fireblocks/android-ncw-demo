package com.fireblocks.sdkdemo.ui.compose.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun CryptoIcon(context: Context, supportedAsset: SupportedAsset, imageSizeResId: Int = R.dimen.image_size, paddingResId: Int = R.dimen.padding_small_1) {
    val cleanedSymbol = supportedAsset.symbol.replace(Regex("_TEST\\d*$"), "")
    val symbol = cleanedSymbol.lowercase() // Ensure the symbol is lowercase
    // we use https://api.coincap.io/v2/assets to get the iconUrl
    val iconUrl = "https://assets.coincap.io/assets/icons/${symbol}@2x.png"
    val imageSize = dimensionResource(id = imageSizeResId)

    AsyncImage(
        modifier = Modifier
            .padding(dimensionResource(id = paddingResId))
            .height(imageSize)
            .width(imageSize)
            .let { if (supportedAsset.isBackgroundTransparent()) it.background(Color.White) else it },
        model = ImageRequest.Builder(context)
            .data(iconUrl)
            .crossfade(true)
            .placeholder(R.drawable.ic_default_asset)
            .error(R.drawable.ic_default_asset)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build(),
        placeholder = painterResource(R.drawable.ic_default_asset),
        contentDescription = "asset icon",
    )
}

@Preview
@Composable
fun CryptoIconPreview() {
    val supportedAsset = SupportedAsset(
        id = "BTC_TEST",
        symbol = "BTC_TEST",
        name = "Bitcoin Test",
        type = "BASE_ASSET",
        blockchain = "BTC_TEST",
        balance = "0.00001",
        price = "0.29",
    )
    CryptoIcon(context = LocalContext.current, supportedAsset = supportedAsset)
}
