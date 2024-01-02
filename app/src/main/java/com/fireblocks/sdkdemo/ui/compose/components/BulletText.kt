package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme

/**
 * Created by Fireblocks Ltd. on 24/12/2023.
 */

@Composable
fun BulletText(modifier: Modifier = Modifier,
               text: String,
               textStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,) {
    val annotatedString = buildAnnotatedString {
        appendInlineContent(id = "imageId")
        append(" ")
        append(text)
    }
    val inlineContentMap = mapOf(
        "imageId" to InlineTextContent(
            Placeholder(4.sp, 4.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_white_dot),
                modifier = Modifier.fillMaxSize(),
                contentDescription = ""
            )
        }
    )
    FireblocksText(
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_extra_small_1)),
        annotatedString = annotatedString,
        inlineContent = inlineContentMap,
        textStyle = textStyle,
    )
}