package com.fireblocks.sdkdemo.ui.compose.components

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white

/**
 * Created by Fireblocks Ltd. on 26/12/2023.
 */
@Composable
fun TitleContentView(@StringRes titleResId: Int? = null,
                     titleText: String? = null,
                     titleColor: Color? = grey_4,
                     titleTextAlign: TextAlign = TextAlign.Start,
                     contentText: String? = null,
                     contentTextStyle: androidx.compose.ui.text.TextStyle = FireblocksNCWDemoTheme.typography.b1,
                     contentTextAlign: TextAlign = TextAlign.Start,
                     contentColor: Color? = white,
                     @DrawableRes contentDrawableRes: Int? = null,
                     onContentButtonClick: () -> Unit = {},
                     @DimenRes topPadding: Int? = R.dimen.padding_default,
                     contentDescriptionText: String = "",
) {
    // Title
    topPadding?.let {
        Spacer(modifier = Modifier.height(dimensionResource(id = topPadding)))
    }
    val title = titleResId?.let { stringResource(id = it) } ?: titleText
    FireblocksText(
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_small)),
        text = title,
        textColor = titleColor ?: grey_4,
        textAlign = titleTextAlign
    )

    // Content
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        FireblocksText(
            modifier = Modifier.weight(1f).semantics { contentDescription = contentDescriptionText },
            text = contentText,
            textColor = contentColor ?: white,
            textStyle = contentTextStyle,
            textAlign = contentTextAlign
        )
        contentDrawableRes?.let {//TODO Icon should be separated and default 48x48 centered to the entire view
            Image(modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.padding_default))
                .clickable { onContentButtonClick() },
                painter = painterResource(id = it), contentDescription = null)
        }
    }
}

@Preview
@Composable
fun TitleContentViewPreview() {
    val content = "{\"email\":\"ofiremulator@gmail.com\",\"platform\":\"ANDROID\",\"requestId\":\"8bcc27a9-6646-4300-86d1-62815ebe9e7a\"}"
    val context = LocalContext.current
    FireblocksNCWDemoTheme {
        Column(

        ) {
            TitleContentView(
                titleText = stringResource(id = R.string.qr_code_link),
                titleColor = white,
                titleTextAlign = TextAlign.Center,
                contentTextAlign = TextAlign.Center,
                contentText = content,
                contentColor = grey_4,
                contentDrawableRes = R.drawable.ic_copy,
                onContentButtonClick = { copyToClipboard(context, content) },
                topPadding = null,
                contentDescriptionText = stringResource(id = R.string.qr_code_link_value_desc),
            )
        }
    }
}