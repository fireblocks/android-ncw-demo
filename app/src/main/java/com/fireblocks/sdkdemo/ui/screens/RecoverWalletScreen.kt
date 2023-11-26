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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.recover.FireblocksPassphraseResolver
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.storage.models.PassphraseLocation
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.GoogleDriveUtil
import com.fireblocks.sdkdemo.ui.viewmodel.RecoverKeysViewModel
import timber.log.Timber

/**
 * Created by Fireblocks Ltd. on 06/07/2023.
 */
@Composable
fun RecoverWalletScreen(modifier: Modifier = Modifier,
                        viewModel: RecoverKeysViewModel = viewModel(),
                        onBackClicked: () -> Unit = {},
                        onRecoverSuccess: (uiState: RecoverKeysViewModel.RecoverKeysUiState) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    viewModel.observeDialogListener(LocalLifecycleOwner.current)
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.recoverSuccess) {
        if (uiState.recoverSuccess) {
            Toast.makeText(context, context.getString(R.string.wallet_recovered), Toast.LENGTH_SHORT).show()
            onRecoverSuccess(uiState)
        }
    }

    if (uiState.shouldStartRecover) {
        viewModel.updateShouldStartRecover(false)
        viewModel.getBackupInfo(context) { backupInfo ->
            viewModel.showProgress(false)
            val createdAt = backupInfo?.createdAt
            if (createdAt != null) {
                if (backupInfo.location == PassphraseLocation.GoogleDrive){
                    Timber.i("Found previous backup on Google Drive, show Drive button")
                    viewModel.onCanRecoverFromGoogleDrive(true)
                } else {
                    Timber.i("Found previous backup on iCloud, show relevant error message")
                    viewModel.onCanRecoverFromGoogleDrive(false)
                    viewModel.showError(R.string.recover_keys_error_icloud)
                }
            } else {
                Timber.i("No previous backup")
                viewModel.onCanRecoverFromGoogleDrive(false)
                viewModel.showError(R.string.recover_keys_error_no_backup)
            }
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

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.RecoverWallet,
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
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                    text = stringResource(id = R.string.recover_wallet_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1
                )
                if (uiState.canRecoverFromGoogleDrive) {
                    RecoverButton(viewModel = viewModel, userFlow = userFlow)
                }
            }
            if (userFlow is UiState.Error) {
                ErrorView(message = stringResource(id = R.string.recover_wallet_error),
                    modifier = Modifier
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
private fun getPassphraseResolver(context: Context,
                                  viewModel: RecoverKeysViewModel): FireblocksPassphraseResolver {
    return object : FireblocksPassphraseResolver {
        val launcher = getRecoverFromDriveLauncher(viewModel)
        override fun resolve(passphraseId: String, callback: (passphrase: String) -> Unit) {
            viewModel.setPassphraseId(passphraseId)
            viewModel.setPassphraseCallback(callback)


            FireblocksManager.getInstance().getPassphraseLocation(context, passphraseId = passphraseId) { passphraseInfo ->
                if (passphraseInfo?.location == PassphraseLocation.GoogleDrive) {
                    viewModel.showProgress(true)

                    val googleSignInClient = GoogleDriveUtil.getGoogleSignInClient(context)
                    googleSignInClient.signOut().addOnCompleteListener {
                        Timber.i("Signed out successfully")
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                } else {
                    callback("")
                }
            }
        }
    }
}

@Composable
fun getRecoverFromDriveLauncher(viewModel: RecoverKeysViewModel): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val callback: (success: Boolean, passphrase: String?) -> Unit = { success, passphrase->
        if (success && !passphrase.isNullOrEmpty()) {
            viewModel.onError(false)
            viewModel.resolvePassphrase(passphrase)
        } else {
            Timber.e("Failed to recover keys, no passphrase found")
            viewModel.onError(true)
            viewModel.resolvePassphrase("")
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                GoogleDriveUtil.getPassphraseFromDrive(context = context,
                    coroutineScope = coroutineScope,
                    intent = intent,
                    createPassphraseIfMissing = false,
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


@Composable
private fun RecoverButton(viewModel: RecoverKeysViewModel, userFlow: UiState) {
    val context = LocalContext.current

    val passphraseResolver = getPassphraseResolver(context, viewModel)
    var labelResourceId = R.string.recover_from_drive
    var imageResourceId: Int? = R.drawable.ic_logo_google
    if (userFlow is UiState.Error) {
        labelResourceId = R.string.try_again
        imageResourceId = null
    }
    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_large)),
        labelResourceId = labelResourceId,
        imageResourceId = imageResourceId,
        onClick = {
            viewModel.recoverKeys(passphraseResolver)
        }
    )
}

@Preview
@Composable
fun RecoverWalletScreenPreview() {
    FireblocksNCWDemoTheme {
        RecoverWalletScreen()
    }
}