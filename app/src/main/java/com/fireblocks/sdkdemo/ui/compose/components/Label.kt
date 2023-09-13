package com.fireblocks.sdkdemo.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.error_bg
import com.fireblocks.sdkdemo.ui.theme.success
import com.fireblocks.sdkdemo.ui.theme.text_grey
import com.fireblocks.sdkdemo.ui.theme.warning
import com.fireblocks.sdkdemo.ui.theme.warning_bg

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
@Composable
fun WarningView(modifier: Modifier = Modifier,
              message: String) {
    OutlinedLabel(
        modifier = modifier.fillMaxWidth(),
        innerModifier = Modifier
            .background(warning_bg)
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.padding_default)),
        message = message,
        borderColor = warning,
        imageResId = R.drawable.ic_warning,
        title = stringResource(id = R.string.warning_title)
    )
}

@Preview
@Composable
fun WarningViewPreview() {
    FireblocksNCWDemoTheme {
        WarningView(
            message = stringResource(id = R.string.takeover_warning_content),
        )
    }
}

@Composable
fun ErrorView(modifier: Modifier = Modifier,
              message: String) {
    OutlinedLabel(
        modifier = modifier.fillMaxWidth(),
        innerModifier = Modifier
            .background(error_bg)
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.padding_default)),
        message = message,
        borderColor = error,
        imageResId = R.drawable.ic_error
    )
}

@Preview
@Composable
fun ErrorViewPreview() {
    FireblocksNCWDemoTheme {
        ErrorView(
            message = stringResource(id = R.string.login_error, stringResource(R.string.sing_in)),
        )
    }
}

@Composable
fun OutlinedLabel(modifier: Modifier = Modifier,
                  innerModifier: Modifier = Modifier,
                  message: String,
                  borderColor: Color,
                  textColor: Color = Color.White,
                  @DrawableRes imageResId: Int? = null,
                  title: String? = null,
) {
    OutlinedCard(
        modifier = modifier,
        border = BorderStroke(1.dp, color = borderColor),
    ) {

            Row(
                modifier = innerModifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                imageResId?.let {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(width = dimensionResource(id = R.dimen.padding_small))) // gap between image and text
                }
                Column( ) {
                    if (title.isNotNullAndNotEmpty()) {
                        FireblocksText(
                            text = title,
                            textStyle = FireblocksNCWDemoTheme.typography.h4,
                            textColor = textColor
                        )
                    }
                        FireblocksText(
                            text = message,
                            textStyle = FireblocksNCWDemoTheme.typography.b3,
                            textColor = textColor
                        )
                }
            }
    }
}

@Preview
@Composable
fun OutlinedLabelPreview() {
    FireblocksNCWDemoTheme {
        OutlinedLabel(
            message = KeyStatus.READY.name.capitalizeFirstLetter(),
            borderColor = success,
            textColor = success
        )
    }
}

@Composable
fun StatusLabel(
    modifier: Modifier = Modifier,
    message: String,
    color: Color,
) {
    OutlinedLabel(
        modifier = modifier,
        innerModifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 10.dp, bottom = 2.dp),
        message = message,
        borderColor = color,
        textColor = color
    )
}

@Preview
@Composable
fun StatusLabelPreview() {
    FireblocksNCWDemoTheme {
        StatusLabel(
            message = KeyStatus.READY.name.capitalizeFirstLetter(),
            color = success,
        )
    }
}

@Composable
fun Label(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = text_grey,
    shape: Shape = CardDefaults.shape
) {
    Card(
        modifier = modifier,//TODO change background color to grey_1
        shape = shape,
    ) {
        FireblocksText(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            text = text,
            textStyle = FireblocksNCWDemoTheme.typography.b3,
            textColor = textColor,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
fun LabelPreview() {
    FireblocksNCWDemoTheme {
        Label(text = "Solana")
    }
}
