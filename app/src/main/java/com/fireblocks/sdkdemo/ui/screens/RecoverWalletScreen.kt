package com.fireblocks.sdkdemo.ui.screens

import android.app.Activity
import android.widget.Toast
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
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
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
                        onBackClicked: () -> Unit,
                        onShowRecoverFromSavedKey: () -> Unit,
                        onRecoverSuccess: (uiState: RecoverKeysViewModel.RecoverKeysUiState) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    viewModel.observeDialogListener(LocalLifecycleOwner.current)

    LaunchedEffect(key1 = uiState.recoverSuccess) {
        if (uiState.recoverSuccess) {
            onRecoverSuccess(uiState)
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
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.recover_wallet_description),
                    textStyle = FireblocksNCWDemoTheme.typography.b1
                )
                RecoverFromGoogleDriveButton(viewModel = viewModel)
//                ICloudButton()
                RecoverFromSavedKeyButton(onShowRecoverFromSavedKey)
            }
            if (userFlow is UiState.Error) {
                ErrorView(message = stringResource(id = R.string.recover_wallet_error), modifier = Modifier
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
private fun RecoverFromSavedKeyButton(onShowRecoverFromSavedKey: () -> Unit) {
    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_default)),
        labelResourceId = R.string.recover_from_saved_key,
        imageResourceId = R.drawable.ic_recover_key,
        onClick = { onShowRecoverFromSavedKey() }
    )
}

@Composable
private fun RecoverFromGoogleDriveButton(viewModel: RecoverKeysViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val callback: (success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?) -> Unit = { success, passphrase, _, _ ->
        if (success && !passphrase.isNullOrEmpty()) {
            viewModel.recoverKeys(context, passphrase)
        }
        viewModel.onError(!success)
    }

    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                GoogleDriveUtil.getPassphraseFromDrive(context = context,
                    coroutineScope = coroutineScope,
                    intent = intent,
                    createPassphraseIfMissing = true,
                    deviceId = viewModel.getDeviceId(),
                    callback = callback)
            } else {
                Toast.makeText(context, "Google Drive Login Error!", Toast.LENGTH_LONG).show()
                callback(false, null, false, null)
            }
        } else {
            callback(false, null, false, null)
        }
    }

    DefaultButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_default)),
        labelResourceId = R.string.recover_from_drive,
        imageResourceId = R.drawable.ic_logo_google,
        onClick = {
            viewModel.showProgress(true)
            val googleSignInClient = GoogleDriveUtil.getGoogleSignInClient(context)
            googleSignInClient.signOut().addOnCompleteListener {
                Timber.i("Signed out successfully")
                startForResult.launch(googleSignInClient.signInIntent)
            }
        }
    )
}

//@Composable
//private fun ICloudButton() {
//    DefaultButton(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = dimensionResource(R.dimen.padding_default)),
//        labelResourceId = R.string.recover_from_icloud,
//        imageResourceId = R.drawable.ic_logo_apple,
//        onClick = {}
//    )
//}

@Preview
@Composable
fun RecoverWalletScreenPreview() {
    FireblocksNCWDemoTheme {
        RecoverWalletScreen(
            onBackClicked = {},
            onShowRecoverFromSavedKey = {},
            onRecoverSuccess = {},)
    }
}