package com.fireblocks.sdkdemo.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.GoogleDriveUtil
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.BackupKeysViewModel
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 06/07/2023.
 */

@Composable
fun CreateKeyBackupScreen(viewModel: BackupKeysViewModel = viewModel(),
                          onBackClicked: () -> Unit,
                          onBackupSuccess: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.backupSuccess) {
        if (uiState.backupSuccess) {
            viewModel.onBackupSuccess(false)
            onBackupSuccess()
        }
    }
    LaunchedEffect(key1 = uiState.shouldGetBackupInfo) {
        if (uiState.shouldGetBackupInfo) {
            viewModel.getBackupInfo(context) { backupInfo ->
                viewModel.showProgress(false)
                viewModel.updateShouldGetBackupInfo(false)
                val createdAt = backupInfo?.createdAt
                if (createdAt != null && backupInfo.location == PassphraseLocation.GoogleDrive) {
                    Timber.i("Found previous backup on Google Drive, show already backed up text")
                    val lastBackupDate = createdAt.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", useSpecificDays = false, useTime = true)
                    viewModel.updateLastBackupDate(lastBackupDate)
                } else {
                    Timber.i("No previous backup on Google Drive")
                }
            }
        }
    }

    val modifier: Modifier = Modifier
    var mainModifier = modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_large))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_large))
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

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
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
            Column(modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val imageHeight = screenHeight * 0.3f

                Image(
                    painter = painterResource(R.drawable.backup_keys_illustration),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = imageHeight)
                        .aspectRatio(1f) // Adjust the aspect ratio as needed
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large_1)),
                    text = stringResource(id = R.string.create_key_backup),
                    textStyle = FireblocksNCWDemoTheme.typography.h1,
                    textAlign = TextAlign.Center
                )
                if (!uiState.shouldGetBackupInfo) {
                    if (uiState.lastBackupDate.isNotEmpty()) {
                        val annotatedString = buildAnnotatedString {
                            append(stringResource(id = R.string.last_backup_keys_prefix))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(" ${uiState.lastBackupDate} ")
                            }
                            append(stringResource(id = R.string.last_backup_keys_suffix))
                        }
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                            annotatedString = annotatedString,
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Center,
                            textColor = text_secondary
                        )
                    } else {
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                            text = stringResource(id = R.string.create_backup_description),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Center,
                            textColor = text_secondary
                        )
                    }
                    GoogleDriveButton(viewModel = viewModel)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            if (userFlow is UiState.Error) {
                ErrorView(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)).align(Alignment.BottomEnd),
                    errorState = userFlow as UiState.Error
                )
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }
}

@Composable
private fun GoogleDriveButton(viewModel: BackupKeysViewModel) {
    val context = LocalContext.current

    val callback: (success: Boolean, passphrase: String?) -> Unit = { success, passphrase ->
        viewModel.showProgress(false)
        if (success && !passphrase.isNullOrEmpty()) {
            viewModel.backupKeys(context, passphrase)
        } else {
            viewModel.showError()
        }
    }

    val backupOnDriveLauncher = getBackupOnDriveLauncher(context, viewModel, updatePassphrase = false, callback)
    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.screen_top_padding)),
        labelResourceId = R.string.backup_on_drive,
        imageResourceId = R.drawable.ic_logo_google,
        onClick = {
            viewModel.getPassphraseId(context, PassphraseLocation.GoogleDrive) { passphraseId ->
                if (passphraseId.isNotNullAndNotEmpty()){
                    val googleSignInClient = GoogleDriveUtil.getGoogleSignInClient(context)
                    backupOnDriveLauncher.launch(googleSignInClient.signInIntent)
                } else {
                    viewModel.showError(resId = R.string.backup_keys_error_no_passphraseId)
                }
            }
        }
    )
}

@Composable
fun getBackupOnDriveLauncher(context: Context,
                             viewModel: BackupKeysViewModel,
                             updatePassphrase: Boolean = false,
                             callback: (success: Boolean, passphrase: String?) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                GoogleDriveUtil.getPassphraseFromDrive(
                    context = context,
                    coroutineScope = coroutineScope,
                    intent = intent,
                    createPassphraseIfMissing = true,
                    updatePassphrase = updatePassphrase,
                    passphraseId = viewModel.getPassphraseId() ?: "",
                    callback = callback)
            } else {
                Toast.makeText(context, "Google Drive Login Error!", Toast.LENGTH_LONG).show()
                callback(false, null)
            }
        } else {
            callback(false, null)
        }
    }
    return launcher
}

@Preview
@Composable
fun CreateKeyBackupScreenPreview() {
    FireblocksNCWDemoTheme {
        CreateKeyBackupScreen(
            onBackClicked = {},
        ) {}
    }
}