package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme

/**
 * Created by Fireblocks ltd. on 10/07/2023.
 */
@Composable
fun ProgressBar(textResId: Int? = null) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            indication = null, // disable ripple effect
            interactionSource = remember { MutableInteractionSource() },
            onClick = { }
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_large)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            textResId?.let {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                FireblocksText(
                    modifier = Modifier,
                    text = stringResource(id = textResId),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}