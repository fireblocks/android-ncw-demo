package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.theme.grey_2

@Composable
fun AddButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(containerColor = grey_2),
        shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_small))
    ) {
        Image(
            modifier = Modifier.clickable { onClick() }
                .padding(dimensionResource(id = R.dimen.padding_small)),
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = null
        )
    }
}