package com.fireblocks.sdkdemo.ui.compose.components

import android.content.Context
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fireblocks.sdkdemo.R

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun Web3Icon(context: Context, iconUrl: String? = null, imageSizeResId: Int = R.dimen.image_size, paddingResId: Int = R.dimen.padding_small_1, placeholderResId: Int = R.drawable.ic_default_dapp) {
    val imageSize = dimensionResource(id = imageSizeResId)
    AsyncImage(
        modifier = Modifier
            .padding(dimensionResource(id = paddingResId))
            .height(imageSize)
            .width(imageSize),
//            .background(white),
        model = ImageRequest.Builder(context)
            .data(iconUrl)
            .crossfade(true)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build(),
        placeholder = painterResource(placeholderResId),
        contentDescription = "web3 connection icon",
    )
}

@Preview
@Composable
fun Web3IconPreview() {
    Web3Icon(context = LocalContext.current, iconUrl = "https://sandbox-static.fireblocks.io/wcs/dappIcon/1b7cc750f7c211f322425db166e386de4468cab5127d0a5a9c21ef3a637af853")
}
