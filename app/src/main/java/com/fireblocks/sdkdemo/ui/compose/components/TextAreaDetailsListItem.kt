package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.white

@Composable
fun TextAreaDetailsListItem(
    titleResId: Int? = null,
    titleAnnotatedString: AnnotatedString? = null,
    contentText: String? = null,
    subContent: String? = null,
    showCopyButton: Boolean? = false,
    contentColor: Color? = white,
    overflow: TextOverflow = TextOverflow.MiddleEllipsis,
) {
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
                .height(dimensionResource(R.dimen.details_list_item_height)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (titleAnnotatedString != null) {
                    FireblocksText(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = dimensionResource(R.dimen.padding_small_2)),
                        annotatedString = titleAnnotatedString,
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textColor = text_secondary,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                    )
                } else {
                    titleResId?.let {
                        FireblocksText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = dimensionResource(R.dimen.padding_small_2)),
                            text = stringResource(it),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textColor = text_secondary,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                        )
                    }
                }
            }

            if (showCopyButton == true) {
                Image(
                    modifier = Modifier
                        .clickable { copyToClipboard(context, contentText) },
                    painter = painterResource(id = R.drawable.ic_copy),
                    contentDescription = null
                )
            }
        }

        if (!contentText.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.padding_large),
                        vertical = dimensionResource(R.dimen.padding_small_2)
                    ),
                colors = CardDefaults.cardColors(containerColor = grey_2),
                shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default))
            ) {
                if (subContent.isNullOrEmpty()) {
                    FireblocksText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_small_2)),
                        text = contentText,
                        textColor = contentColor ?: white,
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Center,
                        overflow = overflow
                    )
                } else {
                    TitleContentView(
                        topPadding = null,
                        titleText = contentText,
                        titleColor = white,
                        contentText = subContent,
                        contentColor = text_secondary,
                        contentDescriptionText = stringResource(id = R.string.address_value_desc),
                    )
                }
            }
        }
    }
}


@Preview()
@Composable
fun PreviewTextAreaDetailsListItem() {
    val receiveAddress = "rDvWQVcUYuaQGiDC3Q956VfEiKQ1adiP4N"
    TextAreaDetailsListItem(titleResId = R.string.wallet_address, contentText = receiveAddress, showCopyButton = true)
}
