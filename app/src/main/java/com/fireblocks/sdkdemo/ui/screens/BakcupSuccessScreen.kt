package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.viewmodel.BackupKeysViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun BackupSuccessScreen(
    viewModel: BackupKeysViewModel = viewModel(),
    onBackClicked: () -> Unit,
    onHomeClicked: () -> Unit,
) {
    val userFlow by viewModel.userFlow.collectAsState()

    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.BackupSuccess,
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
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_success),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.backup_keys_success),
                        textStyle = FireblocksNCWDemoTheme.typography.b1
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (userFlow is UiState.Error) {
                        ErrorView(errorState = userFlow as UiState.Error, defaultResId = R.string.backup_keys_error)
                    }
                    DefaultButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.go_home,
                        onClick = onHomeClicked
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun BackupSuccessScreenPreview() {
    FireblocksNCWDemoTheme {
        BackupSuccessScreen(
            onBackClicked = {},
            onHomeClicked = {}
        )
    }
}
