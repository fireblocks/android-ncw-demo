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
fun NFTIcon(
    modifier: Modifier? = null,
    context: Context,
    iconUrl: String? = null,
    imageSizeResId: Int = R.dimen.image_size,
    paddingResId: Int = R.dimen.padding_small_1
) {
    val imageSize = dimensionResource(id = imageSizeResId)
    val imageModifier = modifier ?: Modifier
        .padding(vertical = dimensionResource(id = paddingResId))
        .height(imageSize)
        .width(imageSize)
    AsyncImage(
        modifier = imageModifier,
        model = ImageRequest.Builder(context)
            .data(iconUrl)
            .crossfade(true)
            .placeholder(R.drawable.ic_default_asset) //TODO change to a default NFT icon placeholder
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
fun NFTIconIconPreview() {
    val iconUrl = "https://stage-static.fireblocks.io/dev9/nft/media/aXBmczovL2JhZnliZWloamNuYXFrd3lucG9kaW5taW54dXdiZ3VucWNxYnNlMmxwb2kzazJibnIyempneXhyaHV1LzE"
    NFTIcon(context = LocalContext.current, iconUrl = iconUrl)
}
