package com.fireblocks.sdkdemo.ui.screens

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.MenuItem
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.StartupTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.BaseLoginViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 02/07/2023.
 */

@Composable
fun SocialLoginScreen(modifier: Modifier = Modifier,
                      viewModel: LoginViewModel = viewModel(),
                      onGenerateKeysScreen: () -> Unit = {},
                      onExistingAccountScreen: () -> Unit = {},
                      onHomeScreen: () -> Unit = {}
) {
    val context = LocalContext.current

    // Check if the user is already signed in
    LaunchedEffect(Unit) {
        Timber.w("Checking if user is already signed in")
        if (SignInUtil.getInstance().isSignedIn(context)) {
            Timber.w("User is already signed in")
            viewModel.handleSuccessSignIn(context)
        }
    }

    BackHandler {
        // prevent back click
    }
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    LaunchedEffect(key1 = uiState.passedLogin) {
        if (uiState.passedLogin) {
            viewModel.showProgress(false)
            MultiDeviceManager.instance.setSplashScreenSeen(true)
            val loginFlow = viewModel.uiState.value.loginFlow
            when (viewModel.hasKeys(context)) {
                // We already have keys locally
                true -> onHomeScreen()
                false -> {
                    when (loginFlow) {
                        BaseLoginViewModel.LoginFlow.SIGN_IN -> {
                            val lastUsedDeviceId = MultiDeviceManager.instance.lastUsedDeviceId(context)
                            lastUsedDeviceId?.let {
                                onGenerateKeysScreen()
                            } ?: onExistingAccountScreen()
                        }

                        BaseLoginViewModel.LoginFlow.SIGN_UP -> onGenerateKeysScreen()
                        BaseLoginViewModel.LoginFlow.DELETE_AND_CREATE_NEW_WALLET -> {}
                    }
                }
            }
        }
    }


    val prefix = stringResource(id = R.string.sing_in)
    addSnackBarObserver(viewModel, LocalLifecycleOwner.current)

    var mainModifier = Modifier.fillMaxWidth()
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading || (SignInUtil.getInstance().isSignedIn(context) && userFlow !is UiState.Error)
    val onSettingsClicked: (MenuItem) -> Unit = { menuItem ->
        when (menuItem) {
            MenuItem.SHARE_LOGS -> {
                viewModel.shareLogs(context)
            }
            MenuItem.REGENERATE_WALLET -> {
                viewModel.setLoginFlow(BaseLoginViewModel.LoginFlow.DELETE_AND_CREATE_NEW_WALLET)
                viewModel.onPassedLogin(false)
            }
        }
    }

    var menuClickListener = onSettingsClicked
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha_dark)
        mainModifier = modifier
            .fillMaxSize()
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
        menuClickListener = {}
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            StartupTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.SocialLogin,
                onMenuActionClicked = menuClickListener,
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.screen_top_padding)),
                    ) {
                        FireblocksText(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = stringResource(id = R.string.sign_in_title),
                            textStyle = FireblocksNCWDemoTheme.typography.h1,
                        )
                    }

                    LaunchedEffect(key1 = uiState.signInState.signInError) {
                        uiState.signInState.signInError?.let { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                    GoogleButton(modifier = Modifier
                        .padding(top = dimensionResource(R.dimen.screen_button_top_padding)),
                        prefix = prefix,
                        viewModel = viewModel)
                    FireblocksText(
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_large)),
                        text = stringResource(id = R.string.or),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textColor = text_secondary
                    )
                    AppleButton(prefix = prefix, viewModel = viewModel)
                }
                if (userFlow is UiState.Error) {
                    val message = getErrorMessage(userFlow as UiState.Error, defaultResId = R.string.login_error, prefix)
                    ErrorView(
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large)),
                        message = message)
                }
                VersionAndEnvironmentLabel(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = dimensionResource(R.dimen.padding_default)))
            }
        }
    }

    if (showProgress) {
        ProgressBar(R.string.loading_wallet)
    }
}

@Composable
private fun getErrorMessage(error: UiState.Error, @StringRes defaultResId: Int, prefix: String): String {
    return error.getErrorMessage(LocalContext.current) ?: stringResource(id = defaultResId, prefix)
}

private fun addSnackBarObserver(viewModel: LoginViewModel, lifecycleOwner: LifecycleOwner) {
    viewModel.snackBar().observe(lifecycleOwner) { observedEvent ->
        observedEvent.contentIfNotHandled?.let {
            viewModel.onSnackbarChanged(true, it)
        }
    }
}

@Composable
fun GoogleButton(modifier: Modifier = Modifier,
                 prefix: String,
                 viewModel: LoginViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

//    val googleUiClient = SignInUtil.getGoogleAuthUiClient(context.applicationContext)
    val googleUiClient = SignInUtil.getInstance().getGoogleSignInClient(context.applicationContext)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
//        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                coroutineScope.launch {
                    val signInResult = googleUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    viewModel.onSignInResult(signInResult)
                    if (signInResult.errorMessage.isNotNullAndNotEmpty()){
                        // failed to sign in
                        viewModel.showError()
                    } else {
                        viewModel.handleSuccessSignIn(context)
                    }
                }
            } else {
                // failed to sign in
                Timber.e("failed to sign in with google. $result")
                viewModel.showError()
                SignInUtil.getInstance().signOut(context)
            }
        }
    )

    DefaultButton(
        modifier = modifier.fillMaxWidth(),
        labelText = stringResource(R.string.sing_in_with_google, prefix),
        imageResourceId = R.drawable.ic_logo_google,
        onClick = {
            viewModel.showProgress(true)
            coroutineScope.launch {
                val signInIntentSender = SignInUtil.getInstance().signInWithGoogle(context)
                launcher.launch(signInIntentSender ?: return@launch)
//                launcher.launch(IntentSenderRequest.Builder(
//                    signInIntentSender ?: return@launch
//                ).build()
//                )
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
    )
}

@Composable
fun AppleButton(modifier: Modifier = Modifier,
                prefix: String,
                viewModel: LoginViewModel) {

    val context = LocalContext.current

    DefaultButton(
        labelText = stringResource(R.string.sing_in_with_apple, prefix),
        imageResourceId = R.drawable.ic_logo_apple,
        onClick = {
            runBlocking {
                SignInUtil.getInstance().signInWithApple(context) { signInResult ->
                    viewModel.onSignInResult(signInResult)
                    if (signInResult.errorMessage.isNotNullAndNotEmpty()){
                        // failed to sign in
                        viewModel.showError()
                    } else {
                        viewModel.handleSuccessSignIn(context)
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
    )
}

@Preview
@Composable
fun SocialLoginScreenPreview() {
    FireblocksNCWDemoTheme {
        SocialLoginScreen()
    }
}
