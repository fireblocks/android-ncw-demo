import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun CryptoIcon(context: Context, supportedAsset: SupportedAsset) {
    val iconUrl = supportedAsset.getAssetIconUrl()
    if (iconUrl.isNullOrEmpty()){
        Image(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small_1)),
            painter = painterResource(id = supportedAsset.getIcon(context)),
            contentDescription = ""
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small_1))
                .height(36.dp).width(36.dp),
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
