package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.TogglePassword

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun CopyLocallyScreen(
    passphrase: String? = "",
    onBackClicked: () -> Unit = {},
    onHomeClicked: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val text = passphrase ?: ""
    val mutableStateOfPassphrase = remember {
        mutableStateOf(text)
    }
    val modifier = Modifier
        .fillMaxSize()
        .padding(dimensionResource(R.dimen.padding_default))
    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.CreateBackup,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null) { focusManager.clearFocus() },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = modifier.weight(1f)) {
                    FireblocksText(
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                        text = stringResource(id = R.string.backup_local_title),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Start
                    )
                    TogglePassword(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen.padding_large)),
                        readOnly = true,
                        password = mutableStateOfPassphrase,
                        onKeyboardDoneClick = {
                            focusManager.clearFocus()
                            copyToClipboard(context, passphrase)
                        }
                    )
                    DefaultButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.copy_passphrase,
                        imageResourceId = R.drawable.ic_copy,
                        onClick = {
                            copyToClipboard(context, passphrase)
                        }
                    )
                }
                ColoredButton(
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_default)),
                    labelResourceId = R.string.go_home,
                    imageResourceId = R.drawable.ic_home,
                    onClick = onHomeClicked
                )
            }
        }
    }
}

@Preview
@Composable
fun BackupLocalScreenPreview() {
    FireblocksNCWDemoTheme {
        CopyLocallyScreen(
            passphrase = "LrqUwFquM24YTAGpA4M2Av"
        ) {}
    }
}
