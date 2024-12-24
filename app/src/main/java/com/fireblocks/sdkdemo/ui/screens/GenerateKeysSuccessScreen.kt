package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton

/**
 * Created by Fireblocks Ltd. on 03/10/2023.
 */

@Composable
fun GenerateKeysSuccessScreen(
    modifier: Modifier = Modifier,
    onSettingsClicked: () -> Unit = {},
    onCreateBackupScreen: () -> Unit = {},
    onHomeScreen: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FireblocksTopAppBar(
                modifier = Modifier,
                currentScreen = FireblocksScreen.GenerateKeysSuccess,
                canNavigateBack = false,
                navigateUp = {},
                onMenuActionClicked = onSettingsClicked
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                    Image(
                        painter = painterResource(R.drawable.ic_success),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.generate_keys_success_description),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    ColoredButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.create_key_backup,
                        onClick = onCreateBackupScreen
                    )
                    TransparentButton(
                        modifier = Modifier.fillMaxWidth(),
                        labelResourceId = R.string.do_this_later,
                        onClick = onHomeScreen
                    )
                }
            }
        }

    }
}

@Preview
@Composable
fun GenerateKeysSuccessScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            GenerateKeysSuccessScreen()
        }
    }
}