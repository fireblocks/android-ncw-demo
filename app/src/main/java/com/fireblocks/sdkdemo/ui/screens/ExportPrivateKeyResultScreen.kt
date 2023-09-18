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
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.TogglePassword
import com.fireblocks.sdkdemo.ui.viewmodel.TakeoverViewModel

/**
 * Created by Fireblocks ltd. on 10/08/2023.
 */
@Composable
fun ExportPrivateKeyResultScreen(
    onBackClicked: () -> Unit,
    takeoverResult: Set<FullKey>) {

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.ExportPrivateKeyResult,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
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
                    text = stringResource(id = R.string.export_private_key_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1
                )
                takeoverResult.forEach {fullKey ->

                    val text = fullKey.privateKey ?: ""
                    val mutableStateOfPassphrase = remember {
                        mutableStateOf(text)
                    }
                    TogglePassword(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen.padding_large)),
                        readOnly = true,
                        password = mutableStateOfPassphrase,
                    )
                    DefaultButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.copy_key,
                        imageResourceId = R.drawable.ic_copy,
                        onClick = {
                            copyToClipboard(context, text)
                        }
                    )
                }

            }
        }
    }

}

@Preview
@Composable
fun ExportPrivateKeyResultScreenPreview() {
    val viewModel =  TakeoverViewModel()
    val fullKeys = hashSetOf(FullKey(keyId = "123", privateKey = "xprv9s21ZrQH143K2zPNSbKDKusTNW4XVwvTCCEFvcLkeNyauqJJd9UjZg3AtgeVAEs84BZtyBdnFom3VqrvAQbzE1j9XKJ3uNvxyL1kJZP49cE"))
    viewModel.onTakeoverResult(fullKeys)
    FireblocksNCWDemoTheme {
        ExportPrivateKeyResultScreen(
            onBackClicked = {},
            takeoverResult = fullKeys,
        )
    }
}