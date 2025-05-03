package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.utils.ImageUtils

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun CryptoIcon(
    modifier: Modifier? = Modifier,
    iconUrl: String? = null,
    symbol: String,
    imageSizeResId: Int = R.dimen.image_size,
    paddingResId: Int = R.dimen.padding_small_1,
    placeholderResId: Int = R.drawable.ic_asset_placeholder
) {
    val context = LocalContext.current
    var currentUrlIndex by remember { mutableIntStateOf(0) }
    var useLocalFallback by remember { mutableStateOf(false) }

    // Generate fallback URLs with improved regex to handle both SYMBOL_TEST and SYMBOLTEST patterns
    val assetSymbol = symbol.replace(Regex("(?:_?TEST\\d*$)|(?:TEST\\d*$)"), "").lowercase()

    // For compound network names (e.g. amoy_polygon -> polygon)
    val baseNetwork = if (assetSymbol.contains("_")) {
        assetSymbol.split("_").first()
    } else {
        assetSymbol
    }

    // Handle specific blockchain mappings
    val normalizedSymbol = when (assetSymbol) {
        "bsc" -> "bnb"
        "avalanche" -> "avax"
        "amoy_polygon" -> "matic"
        "arbitrum_rin" -> "arb"
        "algo_usdc" -> "algo"
        "optimistic_kov" -> "op"
        else -> assetSymbol
    }

    val normalizedBaseNetwork = when (baseNetwork) {
        "bsc" -> "bnb"
        "avalanche" -> "avax"
        "polygon" -> "matic"
        "amoy" -> "matic"
        "arbitrum" -> "arb"
        "optimistic", "opt" -> "op"
        "mantra" -> "om"
        else -> baseNetwork
    }

    // Priority order of URLs to try with additional fallbacks
    val urls = remember(iconUrl, symbol) {
        listOfNotNull(
            iconUrl,
            "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/32/color/${normalizedSymbol}.png",
            "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/32/color/${normalizedBaseNetwork}.png",
            "https://assets.coincap.io/assets/icons/${normalizedSymbol}@2x.png",
            if (normalizedSymbol != normalizedBaseNetwork) "https://assets.coincap.io/assets/icons/${normalizedBaseNetwork}@2x.png" else null,
            "https://cryptoicons.org/api/${normalizedSymbol}/200",
            "https://cryptologos.cc/logos/${normalizedSymbol}-${normalizedSymbol}-logo.png",
            // Additional fallbacks for common chains
            "https://cdn.jsdelivr.net/gh/atomiclabs/cryptocurrency-icons@master/32/color/${normalizedSymbol}.png",
            "https://cdn.jsdelivr.net/gh/atomiclabs/cryptocurrency-icons@master/32/color/${normalizedBaseNetwork}.png"
        )
    }

    val currentUrl = urls.getOrNull(currentUrlIndex)
    val imageSize = dimensionResource(id = imageSizeResId)
    val imageModifier = modifier ?: Modifier

    if (useLocalFallback) {
        // Final fallback: use local resources
        Image(
            modifier = imageModifier
                .padding(dimensionResource(id = paddingResId))
                .height(imageSize)
                .width(imageSize),
            painter = painterResource(id = ImageUtils.getIcon(context, baseNetwork, placeholderResId)),
            contentDescription = "asset icon"
        )
    } else {
        AsyncImage(
            modifier = imageModifier
                .padding(dimensionResource(id = paddingResId))
                .height(imageSize)
                .width(imageSize),
            model = ImageRequest.Builder(context)
                .data(currentUrl)
                .crossfade(true)
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .listener(
                    onError = { _, _ ->
                        if (currentUrlIndex < urls.size - 1) {
                            // Try the next URL in the list
                            currentUrlIndex++
                        } else {
                            // We've exhausted all URLs, switch to local fallback
                            useLocalFallback = true
                        }
                    }
                )
                .build(),
            placeholder = painterResource(placeholderResId),
            contentDescription = "asset icon",
        )
    }
}

@Preview
@Composable
fun CryptoIconPreview() {
    CryptoIcon(symbol = "BTC_TEST")
}
