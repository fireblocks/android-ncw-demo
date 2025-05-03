package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_2

/**
 * Created by Fireblocks Ltd. on 31/12/2024.
 */
@Composable
fun SDKVersionsLabel(modifier: Modifier = Modifier, backgroundColor: Color = grey_2, borderColor: Color = background, ncwVersion: String) {
    val annotatedString = buildAnnotatedString {
        append(stringResource(id = R.string.ncw_version, ncwVersion))
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