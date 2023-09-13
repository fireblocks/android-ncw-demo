package com.fireblocks.sdkdemo.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.viewmodel.BackupKeysViewModel

@Composable
fun BackupSuccessScreen(
    modifier: Modifier = Modifier,
    viewModel: BackupKeysViewModel = viewModel(),
    onBackClicked: () -> Unit,
    onHomeClicked: () -> Unit,
) {
    val userFlow by viewModel.userFlow.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.BackupSuccess,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_default)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                    Image(
                        painter = painterResource(R.drawable.ic_backup_key),
                        contentDescription = null,
                        modifier = Modifier.width(300.dp)
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.backup_keys_success),
                        textStyle = FireblocksNCWDemoTheme.typography.b1
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (userFlow is UiState.Error) {
                        ErrorView(message = stringResource(id = R.string.backup_keys_error))
                    }
                    ColoredButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.go_home,
                        imageResourceId = R.drawable.ic_home,
                        onClick = onHomeClicked
                    )
                }
            }
        }
    }
}


@Composable
fun GoHomeButton(
    @StringRes labelResourceId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_default))

    ) {
        Text(stringResource(labelResourceId), color = Color.White)
    }
}


@Preview
@Composable
fun BackupSuccessScreenPreview() {
    FireblocksNCWDemoTheme {
        BackupSuccessScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_default)),
            onBackClicked = {},
            onHomeClicked = {}
        )
    }
}
