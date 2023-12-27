package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2

/**
 * Created by Fireblocks Ltd. on 15/07/2023.
 */
@Composable
fun TogglePassword(modifier: Modifier,
                   readOnly: Boolean = false,
                   password: MutableState<String> = remember { mutableStateOf("") },
                   onKeyboardDoneClick: () -> Unit = {},
                   showRevealIcon: Boolean = true,
                   revealPassword: MutableState<Boolean> = remember { mutableStateOf(false) } // To reveal the password with toggle
) {
    var trailingIcon: @Composable (() -> Unit)? = null
    if (showRevealIcon) {
        trailingIcon =  {RevealIconButton(revealPassword = revealPassword)}
    }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        value = password.value,
        onValueChange = { newText ->
            password.value = newText
        },
        visualTransformation = if (revealPassword.value) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = trailingIcon,
        textStyle = FireblocksNCWDemoTheme.typography.b1,
        singleLine = false,
        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.padding_default)),
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
fun TogglePasswordPreview(){
    FireblocksNCWDemoTheme {
        TogglePassword(modifier = Modifier)
    }
}