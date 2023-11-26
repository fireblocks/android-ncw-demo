package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.GoogleDriveUtil
import com.fireblocks.sdkdemo.ui.viewmodel.BackupKeysViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun AlreadyBackedUpScreen(
    modifier: Modifier = Modifier,
    viewModel: BackupKeysViewModel = viewModel(),
    onBackClicked: () -> Unit,
    onBackupSuccess: (backupKeysUiState: BackupKeysViewModel.BackupKeysUiState) -> Unit = {},
    lastBackupDate: String? = ""
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.backupSuccess) {
        if (uiState.backupSuccess) {
            viewModel.onBackupSuccess(false)
            onBackupSuccess(uiState)
        }
    }

    var mainModifier = modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_default))
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = Modifier
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    val callback: (success: Boolean, passphrase: String?) -> Unit = { success, passphrase ->
        if (success && !passphrase.isNullOrEmpty()) {
            viewModel.backupKeys(passphrase)
        } else {
            viewModel.onError()
        }
    }

    val backupOnDriveLauncher = getBackupOnDriveLauncher(context, viewModel, updatePassphrase = true, callback)

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.AlreadyBackedUp,
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
                modifier = mainModifier,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                    Image(
                        painter = painterResource(R.drawable.ic_backup_key),
                        contentDescription = null,
                        modifier = Modifier.width(300.dp)
                    )

                    val annotatedString = buildAnnotatedString {
                        append(stringResource(id = R.string.last_backup_keys_prefix))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" $lastBackupDate ")
                        }
                    }
                    FireblocksText(
                        annotatedString = annotatedString,
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Center,
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
                        labelResourceId = R.string.update_key_backup,
                        onClick = {
                            viewModel.getPassphraseId(context, PassphraseLocation.GoogleDrive) { passphraseId ->
                                if (passphraseId.isNotNullAndNotEmpty()){
                                    val googleSignInClient = GoogleDriveUtil.getGoogleSignInClient(context)
                                    backupOnDriveLauncher.launch(googleSignInClient.signInIntent)
                                }
                            }
                        }
                    )
                }
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }
}


@Preview
@Composable
fun AlreadyBackedUpScreenPreview() {
    FireblocksNCWDemoTheme {
        AlreadyBackedUpScreen(
            modifier = Modifier
                .fillMaxSize(),
            onBackClicked = {},
            lastBackupDate = "05/31/2023"
        )
    }
}
