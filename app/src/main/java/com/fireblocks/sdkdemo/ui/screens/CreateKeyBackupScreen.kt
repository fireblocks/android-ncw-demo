package com.fireblocks.sdkdemo.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.GoogleDriveUtil
import com.fireblocks.sdkdemo.ui.viewmodel.BackupKeysViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 06/07/2023.
 */

@Composable
fun CreateKeyBackupScreen(modifier: Modifier = Modifier,
                          viewModel: BackupKeysViewModel = viewModel(),
                          onBackClicked: () -> Unit,
                          showAlreadyBackedUp: (lastBackupDate: String?) -> Unit = {},
                          onBackupSuccess: (backupKeysUiState: BackupKeysViewModel.BackupKeysUiState) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    LaunchedEffect(key1 = uiState.backupSuccess) {
        if (uiState.backupSuccess) {
            onBackupSuccess(uiState)
        }
    }

    var mainModifier = modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        mainModifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_default))
            .alpha(0.5f)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = Modifier
            .alpha(0.5f)
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
            Column(modifier = mainModifier) {
                FireblocksText(
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.create_backup_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1
                )
                GoogleDriveButton(viewModel = viewModel, showAlreadyBackedUp = showAlreadyBackedUp)
//                ICloudButton()
                CopyButton(viewModel)
            }
            if (userFlow is UiState.Error) {
                ErrorView(message = stringResource(id = R.string.backup_keys_error), modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_default))
                    .align(Alignment.BottomEnd))
            }
            if (showProgress) {
                ProgressBar()
            }
        }
    }
}

@Composable
private fun CopyButton(viewModel: BackupKeysViewModel) {
    val context = LocalContext.current
    val passphrase = Fireblocks.generateRandomPassphrase()
    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_default)),
        labelResourceId = R.string.copy_locally,
        imageResourceId = R.drawable.ic_copy,
        onClick = {
            viewModel.backupKeys(passphrase, true)
        }
    )
}

@Composable
private fun GoogleDriveButton(viewModel: BackupKeysViewModel, showAlreadyBackedUp: (lastBackupDate: String?) -> Unit) {
    val context = LocalContext.current

    val callback: (success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?) -> Unit = { success, passphrase, alreadyBackedUp, lastBackupDate ->
        viewModel.showProgress(false)
        if (success && !passphrase.isNullOrEmpty()) {
            viewModel.onError(false)
            if (alreadyBackedUp){
                //show already backed up screen
                runBlocking(Dispatchers.Main) {
                    showAlreadyBackedUp(lastBackupDate)
                }
            } else {
                viewModel.backupKeys(passphrase)
            }
        }
        viewModel.onError(!success)
    }

    val backupOnDriveLauncher = getBackupOnDriveLauncher(context, viewModel.getDeviceId(), callback)

    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_default)),
        labelResourceId = R.string.backup_on_drive,
        imageResourceId = R.drawable.ic_logo_google,
        onClick = {
            viewModel.onError(false)
            viewModel.showProgress(true)
            val googleSignInClient = GoogleDriveUtil.getGoogleSignInClient(context)
            googleSignInClient.signOut().addOnCompleteListener {
                Timber.i("Signed out successfully")
                backupOnDriveLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    )
}

@Composable
fun getBackupOnDriveLauncher(context: Context,
                             deviceId: String,
                             callback: (success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val coroutineScope = rememberCoroutineScope()

    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                GoogleDriveUtil.getPassphraseFromDrive(context = context,
                    coroutineScope = coroutineScope,
                    intent = intent,
                    createPassphraseIfMissing = true,
                    deviceId = deviceId,
                    callback = callback)
            } else {
                Toast.makeText(context, "Google Drive Login Error!", Toast.LENGTH_LONG).show()
                callback(false, null, false, null)
            }
        } else {
            callback(false, null, false, null)
        }
    }
    return startForResult
}

//@Composable
//private fun ICloudButton() {
//    DefaultButton(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = dimensionResource(R.dimen.padding_default)),
//        labelResourceId = R.string.backup_on_icloud,
//        imageResourceId = R.drawable.ic_logo_apple,
//        onClick = {}
//    )
//}


@Preview
@Composable
fun CreateKeyBackupScreenPreview() {
    FireblocksNCWDemoTheme {
        CreateKeyBackupScreen(
            onBackClicked = {},
            onBackupSuccess = {},)
    }
}