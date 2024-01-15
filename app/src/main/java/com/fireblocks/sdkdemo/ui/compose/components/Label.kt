package com.fireblocks.sdkdemo.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.error_bg
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.semiTransparentBlue
import com.fireblocks.sdkdemo.ui.theme.success
import com.fireblocks.sdkdemo.ui.theme.transparent
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
                  borderWidth: Dp = 1.dp,
) {
    OutlinedCard(
        modifier = modifier,
        border = BorderStroke(borderWidth, color = borderColor),
        shape = RoundedCornerShape(size = 6.dp)
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
                Column {
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
        textColor = color,
        borderWidth = 0.5.dp
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
    text: String? = null,
    textColor: Color = grey_4,
    backgroundColor: Color = grey_2,
    borderColor: Color = black,
    shape: Shape = CardDefaults.shape,
    annotatedString: AnnotatedString? = null,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
    Column(modifier = modifier) {
        FireblocksText(
            modifier = Modifier
                .border(width = 0.dp, color = borderColor, shape = RoundedCornerShape(size = 68.dp))
                .background(color = backgroundColor, shape = shape)
                .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 3.dp),
            text = text,
            annotatedString = annotatedString,
            inlineContent = inlineContent,
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

@Composable
fun VersionAndEnvironmentLabel(modifier: Modifier = Modifier, backgroundColor: Color = grey_2, borderColor: Color = black) {
    val annotatedString = buildAnnotatedString {
        append(stringResource(id = R.string.version, BuildConfig.VERSION_NAME))
        append(" ")
        appendInlineContent(id = "imageId")
        append(" ")
        append(stringResource(id = R.string.build, BuildConfig.VERSION_CODE))
        append(" ")
        appendInlineContent(id = "imageId")
        append(" ${BuildConfig.FLAVOR.capitalizeFirstLetter()}")
    }
    val inlineContentMap = mapOf(
        "imageId" to InlineTextContent(
            Placeholder(4.sp, 4.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_dot),
                modifier = Modifier.fillMaxSize(),
                contentDescription = ""
            )
        }
    )
    Label(
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_extra_small_1)),
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        annotatedString = annotatedString,
        inlineContent = inlineContentMap
    )
}

@Preview
@Composable
fun BuildAndEnvLabelPreview() {
    FireblocksNCWDemoTheme {
        VersionAndEnvironmentLabel()
    }
}

@Preview
@Composable
fun BuildAndEnvSemiTransparentLabelPreview() {
    FireblocksNCWDemoTheme {
        VersionAndEnvironmentLabel(backgroundColor = semiTransparentBlue, borderColor = transparent)
    }
}

