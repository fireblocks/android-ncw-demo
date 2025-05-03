package com.fireblocks.sdkdemo.ui.compose.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.white

/**
 * Created by Fireblocks Ltd. on 26/12/2023.
 */
@Composable
fun TitleContentLinkView(modifier: Modifier = Modifier,
                         @StringRes titleResId: Int? = null,
                         titleText: String? = null,
                         titleColor: Color? = text_secondary,
                         titleTextStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
                         titleTextAlign: TextAlign = TextAlign.Start,
                         contentText: String,
                         contentTextStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
                         contentTextAlign: TextAlign = TextAlign.Start,
                         contentColor: Color? = white,
                         contentMaxLines: Int = 50,
                         @DrawableRes contentDrawableRes: Int? = null,
                         onContentButtonClick: () -> Unit = {},
                         @DimenRes topPadding: Int? = R.dimen.padding_default,
                         @DimenRes bottomPadding: Int? = null,
                         contentDescriptionText: String = "",
) {
    val context = LocalContext.current
    // Title
    topPadding?.let {
        Spacer(modifier = Modifier.height(dimensionResource(id = it)))
    }
    val title = titleResId?.let { stringResource(id = it) } ?: titleText
    FireblocksText(
        modifier = modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_small)),
        text = title,
        textColor = titleColor ?: text_secondary,
        textStyle = titleTextStyle,
        textAlign = titleTextAlign
    )

    // Content
    Row(modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        val annotatedString = buildAnnotatedString {
            pushStringAnnotation(tag = "URL", annotation = contentText)
            withStyle(style = SpanStyle(color = white, textDecoration = TextDecoration.Underline)) {
                append(contentText)
            }
            pop()
        }
        ClickableText(
            modifier = Modifier.weight(1f).semantics { contentDescription = contentDescriptionText },
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        startActivity(context, intent, null)
                    }
            }
        )


        contentDrawableRes?.let {//TODO Icon should be separated and default 48x48 centered to the entire view
            Image(modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.padding_default))
                .clickable { onContentButtonClick() },
                painter = painterResource(id = it), contentDescription = null)
        }
    }
    bottomPadding?.let {
        Spacer(modifier = Modifier.height(dimensionResource(id = it)))
    }
}

@Preview
@Composable
fun TitleContentLinkViewPreview() {
    val content = "https://uniswap.org/"
    val context = LocalContext.current
    FireblocksNCWDemoTheme {
        Column(

        ) {
            TitleContentLinkView(
                titleText = stringResource(id = R.string.enter_qr_code_link),
                titleColor = white,
                titleTextAlign = TextAlign.Center,
                contentTextAlign = TextAlign.Center,
                contentText = content,
                contentColor = text_secondary,
                contentDrawableRes = R.drawable.ic_copy,
                onContentButtonClick = { copyToClipboard(context, content) },
                topPadding = null,
                contentDescriptionText = stringResource(id = R.string.qr_code_link_value_desc),
            )
        }
    }
}