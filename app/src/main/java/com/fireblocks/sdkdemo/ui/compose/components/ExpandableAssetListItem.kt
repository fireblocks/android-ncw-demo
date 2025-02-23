package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.wallet.AssetListItem
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2

/**
 * Created by Fireblocks Ltd. on 06/08/2024.
 */
@Composable
fun ExpandableAssetListItem(
    modifier: Modifier = Modifier,
    supportedAsset: SupportedAsset,
    showBlockchain: Boolean = true,
    onSendClicked: (SupportedAsset) -> Unit = {},
    onReceiveClicked: (SupportedAsset) -> Unit = {}
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val isExpanded = expandedStates[supportedAsset.id] ?: false

    Column(modifier = modifier
        .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
        .animateContentSize(animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        ))
        .clickable {
            expandedStates[supportedAsset.id] = !isExpanded
        }
    ) {
        AssetListItem(
            supportedAsset = supportedAsset,
            showBlockchain = showBlockchain,
            clickable = true,
            onClick = { expandedStates[supportedAsset.id] = !isExpanded }
        )

        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.padding_small), horizontal = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColoredButton(
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.send,
                    imageResourceId = R.drawable.ic_send,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_2),
                    onClick = { onSendClicked(supportedAsset) }
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_default)))

                ColoredButton(
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.receive,
                    imageResourceId = R.drawable.ic_receive,
                    colors = ButtonDefaults.buttonColors(containerColor = grey_2),
                    onClick = { onReceiveClicked(supportedAsset) }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ExpandableAssetListItemPreview() {
    val supportedAsset = SupportedAsset(
        id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum",
        balance = "132.4",
        price = "2,825.04"
    )

    FireblocksNCWDemoTheme {
        Surface {
            ExpandableAssetListItem(
                supportedAsset = supportedAsset,
                onSendClicked = { /* Handle send click */ },
                onReceiveClicked = { /* Handle receive click */ }
            )
        }
    }
}