package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.TogglePassword

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun CopyLocallyScreen(
    modifier: Modifier = Modifier,
    passphrase: String? = "",
    onBackClicked: () -> Unit,
) {
    val context = LocalContext.current
    val text = passphrase ?: ""
    val mutableStateOfPassphrase = remember {
        mutableStateOf(text)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.CreateBackup,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.backup_local_title),
                    textStyle = FireblocksNCWDemoTheme.typography.b1
                )
                TogglePassword(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.padding_large)),
                    readOnly = true,
                    password = mutableStateOfPassphrase,
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
        }
    }
}

@Preview
@Composable
fun BackupLocalScreenPreview() {
    FireblocksNCWDemoTheme {
        CopyLocallyScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
            passphrase = "LrqUwFquM24YTAGpA4M2Av"
        ) {}
    }
}
