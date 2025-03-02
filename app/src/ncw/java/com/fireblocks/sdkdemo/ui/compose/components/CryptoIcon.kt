package com.fireblocks.sdkdemo.ui.compose.components

import android.content.Context
import androidx.compose.foundation.Image
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
import coil.request.ImageRequest
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun CryptoIcon(context: Context, supportedAsset: SupportedAsset, symbol: String, imageSizeResId: Int = R.dimen.image_size, paddingResId:  Int = R.dimen.padding_small_1) {
    val iconUrl = supportedAsset.iconUrl
    Timber.d("iconUrl: $iconUrl")
    val imageSize = dimensionResource(id = imageSizeResId)
    if (iconUrl.isNullOrEmpty()){
        Image(
            modifier = Modifier
                .padding(dimensionResource(id = paddingResId))
                .height(imageSize).width(imageSize),
            painter = painterResource(id = supportedAsset.getIcon(context)),
            contentDescription = "asset icon"
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .padding(dimensionResource(id = paddingResId))
                .height(imageSize).width(imageSize)
                .let { if (supportedAsset.isBackgroundTransparent()) it.background(Color.White) else it },
        model = ImageRequest.Builder(LocalContext.current)
                .data(iconUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_default_asset)
                .error(R.drawable.ic_default_asset)
                .build(),
            placeholder = painterResource(R.drawable.ic_default_asset),
            contentDescription = "asset icon",
        )
    }
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
    CryptoIcon(context = LocalContext.current, supportedAsset = supportedAsset, symbol = "BTC_TEST")
}
