package com.fireblocks.sdkdemo.ui.compose.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
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
    paddingResId: Int = R.dimen.padding_small_1,
    onDominantColorExtracted: (Color) -> Unit = {}
) {
    val imageSize = dimensionResource(id = imageSizeResId)
    val imageModifier = modifier ?: Modifier
        .padding(vertical = dimensionResource(id = paddingResId))
        .height(imageSize)
        .width(imageSize)

    var dominantColor by remember { mutableStateOf(Color.Transparent) }

    AsyncImage(
        modifier = imageModifier,
        model = ImageRequest.Builder(context)
            .data(iconUrl)
            .crossfade(true)
            .placeholder(R.drawable.ic_default_asset)
            .error(R.drawable.ic_default_asset)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .listener { request, result ->
                if (result is SuccessResult) {
                    val originalBitmap = (result.drawable).toBitmap()
//                    val originalBitmap = (result.drawable as BitmapDrawable).bitmap
//                    val originalBitmap = (successResult.drawable as BitmapDrawable).bitmap
                    val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
//                    val bitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, true)
                    Palette.from(bitmap).generate { palette: Palette? ->
                        palette?.dominantSwatch?.rgb?.let { colorValue: Int ->
                            dominantColor = Color(colorValue)
                            onDominantColorExtracted(dominantColor)
                        }
                    }
                }
            }
            .build(),
        placeholder = painterResource(R.drawable.ic_default_asset),
        contentDescription = "asset icon",
    )
}

@Preview
@Composable
fun NFTIconIconPreview() {
    val iconUrl =
        "https://stage-static.fireblocks.io/dev9/nft/media/aXBmczovL2JhZnliZWloamNuYXFrd3lucG9kaW5taW54dXdiZ3VucWNxYnNlMmxwb2kzazJibnIyempneXhyaHV1LzE"
    NFTIcon(context = LocalContext.current, iconUrl = iconUrl)
}
