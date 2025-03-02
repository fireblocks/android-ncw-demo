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
fun CryptoIcon(context: Context, supportedAsset: SupportedAsset? = null, symbol: String, imageSizeResId: Int = R.dimen.image_size, paddingResId: Int = R.dimen.padding_small_1) {
    val assetSymbol = symbol.replace(Regex("_TEST\\d*$"), "").lowercase()
    // we use https://api.coincap.io/v2/assets to get the iconUrl
    val iconUrl = "https://assets.coincap.io/assets/icons/${assetSymbol}@2x.png"
    val imageSize = dimensionResource(id = imageSizeResId)

    AsyncImage(
        modifier = Modifier
            .padding(dimensionResource(id = paddingResId))
            .height(imageSize)
            .width(imageSize)
            .let { if (isBackgroundTransparent(assetSymbol)) it.background(Color.White) else it },
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

fun isBackgroundTransparent(symbol: String): Boolean {
    return symbol.startsWith("ALGO")
}

@Preview
@Composable
fun CryptoIconPreview() {
    CryptoIcon(context = LocalContext.current, symbol = "BTC_TEST")
}
