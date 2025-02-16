package com.fireblocks.sdkdemo.ui.compose.components

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white


@Composable
fun TitleContentHorizontalView(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int? = null,
    titleText: String? = null,
    titleColor: Color? = grey_4,
    titleTextStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
    titleTextAlign: TextAlign = TextAlign.Start,
    contentText: String? = null,
    contentTextStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
    contentTextAlign: TextAlign = TextAlign.End,
    contentColor: Color? = white,
    contentMaxLines: Int = 1,
    @DrawableRes contentDrawableRes: Int? = null,
    onContentButtonClick: () -> Unit = {},
    @DimenRes topPadding: Int? = R.dimen.padding_default,
    @DimenRes bottomPadding: Int? = null,
    contentDescriptionText: String = "",
) {
    // Title
    topPadding?.let {
        Spacer(modifier = Modifier.height(dimensionResource(id = it)))
    }
    val title = titleResId?.let { stringResource(id = it) } ?: titleText
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FireblocksText(
            modifier = Modifier.weight(1f),
            text = title,
            textColor = titleColor ?: grey_4,
            textStyle = titleTextStyle,
            textAlign = titleTextAlign
        )
        FireblocksText(
            modifier = Modifier.weight(1f).semantics { contentDescription = contentDescriptionText },
            text = contentText,
            textColor = contentColor ?: white,
            textStyle = contentTextStyle,
            textAlign = contentTextAlign,
            maxLines = contentMaxLines
        )
        contentDrawableRes?.let {
            Image(
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.padding_default))
                    .clickable { onContentButtonClick() },
                painter = painterResource(id = it),
                contentDescription = null
            )
        }
    }
    bottomPadding?.let {
        Spacer(modifier = Modifier.height(dimensionResource(id = it)))
    }
}