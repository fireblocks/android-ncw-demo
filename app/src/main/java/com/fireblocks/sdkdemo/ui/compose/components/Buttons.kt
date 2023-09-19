package com.fireblocks.sdkdemo.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.light_blue
import com.fireblocks.sdkdemo.ui.theme.primary_blue_disabled

/**
 * Created by Fireblocks Ltd. on 04/07/2023.
 */
/**
 * Customizable button composable that displays the [labelResourceId]
 * and triggers [onClick] lambda when this composable is clicked
 */
@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    @StringRes labelResourceId: Int? = null,
    labelText: String? = null,
    textStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
    @DrawableRes imageResourceId: Int? = null,
    onClick: () -> Unit,
    colors: ButtonColors?  = null,
    selected: Boolean = true,
    enabledState: MutableState<Boolean> = remember { mutableStateOf(true) },
) {
    val buttonColors = colors ?: ButtonDefaults.buttonColors(containerColor = if (selected) grey_1 else grey_2)
    Button(
        enabled = enabledState.value,
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_default)),
        onClick = onClick,
        colors = buttonColors,
        contentPadding = PaddingValues(0.dp),
    ) {
        imageResourceId?.let {
            Image(
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small)),
                painter = painterResource(id = it),
                contentDescription = ""
            )
        }
        val text = labelText ?: labelResourceId?.let { stringResource(it) }
        if (text.isNotNullAndNotEmpty()) {
            FireblocksText(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_default)),
                text = text,
                textStyle = textStyle,
            )
        }
    }
}

@Preview
@Composable
fun DefaultButtonPreview() {
    FireblocksNCWDemoTheme {
        DefaultButton(
            modifier = Modifier.fillMaxWidth(),
            labelResourceId = R.string.sing_in,
            onClick = {},
            imageResourceId = R.drawable.ic_copy)
    }
}

@Composable
fun ColoredButton(
    modifier: Modifier = Modifier,
    @StringRes labelResourceId: Int,
    @DrawableRes imageResourceId: Int? = null,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(disabledContentColor = primary_blue_disabled, disabledContainerColor = primary_blue_disabled),
    enabledState: MutableState<Boolean> = remember { mutableStateOf(true) },
) {
    DefaultButton(
        modifier = modifier,
        labelResourceId = labelResourceId,
        onClick = onClick,
        imageResourceId = imageResourceId,
        colors = colors,
        enabledState = enabledState
    )
}

@Preview
@Composable
fun ColoredButtonPreview(){
    FireblocksNCWDemoTheme {
        ColoredButton(
            modifier = Modifier.fillMaxWidth(),
            labelResourceId = R.string.generate_keys,
            onClick = {},
            imageResourceId = R.drawable.ic_home)
    }
}

@Composable
fun TransparentButton(
    @StringRes labelResourceId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
    ) {
        FireblocksText(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_default)),
            text = stringResource(labelResourceId),
            textColor = light_blue,
        )
    }
}

@Preview
@Composable
fun TransparentButtonPreview(){
    FireblocksNCWDemoTheme {
        TransparentButton(labelResourceId = R.string.recover_existing_wallet, onClick = {})
    }
}

@Composable
fun SettingsButton(onSettingsClicked: () -> Unit) {
    IconButton(onClick = {
        onSettingsClicked()
    }) {
        Image(
            painter = painterResource(R.drawable.ic_top_bar_menu),
            contentDescription = null,
        )
    }
}

@Composable
fun CloseButton(onCloseClicked: () -> Unit) {
    Button(
        onClick = onCloseClicked,
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        //                alignment = Alignment.TopEnd,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = ""
        )
    }
}

@Composable
fun ContinueButton(enabledState: MutableState<Boolean>,
                   onClick: () -> Unit,
                   @StringRes labelResourceId: Int = R.string.continue_button,
                   @DrawableRes imageResourceId: Int? = null,
) {

    val continueButtonModifier = when (enabledState.value) {
        false -> Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_default))
            .alpha(floatResource(R.dimen.progress_alpha))
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )

        true -> Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_default))
    }
    ColoredButton(
        modifier = continueButtonModifier,
        labelResourceId = labelResourceId,
        imageResourceId = imageResourceId,
        onClick = onClick,
        enabledState = enabledState
    )
}

@Preview
@Composable
fun ContinueButtonPreview() {
    FireblocksNCWDemoTheme {
        ContinueButton(
            remember {mutableStateOf(true)},
            labelResourceId = R.string.approve,
            imageResourceId = R.drawable.ic_approve,
            onClick = {
    //                    onContinueClick(viewModel, onNextScreen, addressTextState)
            })
    }
}