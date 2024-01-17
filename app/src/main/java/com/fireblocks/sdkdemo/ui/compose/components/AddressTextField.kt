package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2

/**
 * Created by Fireblocks Ltd. on 15/07/2023.
 */
@Composable
fun AddressTextField(modifier: Modifier,
                     readOnly: Boolean = false,
                     text: MutableState<String> = remember { mutableStateOf("") },
                     onKeyboardDoneClick: () -> Unit = {}
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        value = text.value,
        onValueChange = { newText ->
            text.value = newText
        },
        trailingIcon = {
            if (text.value.isNotNullAndNotEmpty()) {
                IconButton(
                    onClick = {
                        text.value = ""
                    },
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                }
            }
        },
        textStyle = FireblocksNCWDemoTheme.typography.b1,
        singleLine = false,
        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = grey_1,
            unfocusedContainerColor = grey_1,
            disabledContainerColor = grey_1,
            focusedBorderColor = grey_2,
            unfocusedBorderColor = grey_2
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { onKeyboardDoneClick() }
        ),
    )
}

@Preview
@Composable
fun AddressTextFieldPreview() {
    AddressTextField(modifier = Modifier)
}