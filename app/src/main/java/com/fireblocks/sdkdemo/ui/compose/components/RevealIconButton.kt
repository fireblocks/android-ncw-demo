package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme

/**
 * Created by Fireblocks Ltd. on 09/11/2023.
 */

@Composable
fun RevealIconButton(modifier: Modifier = Modifier,
                     revealPassword: MutableState<Boolean>) {
    if (revealPassword.value) {
        IconButton(
            modifier = modifier,
            onClick = {
                revealPassword.value = false
            },
        ) {
            Icon(imageVector = Icons.Filled.Visibility, contentDescription = null)
        }
    } else {
        IconButton(
            modifier = modifier,
            onClick = {
                revealPassword.value = true
            },
        ) {

            Icon(imageVector = Icons.Filled.VisibilityOff, contentDescription = null)
        }
    }
}

@Preview
@Composable
fun RevealIconButtonPreview() {
    val revealPassword: MutableState<Boolean> = remember {
        mutableStateOf(false)
    } // To reveal the password with toggle
    FireblocksNCWDemoTheme {
        RevealIconButton(revealPassword = revealPassword)
    }
}