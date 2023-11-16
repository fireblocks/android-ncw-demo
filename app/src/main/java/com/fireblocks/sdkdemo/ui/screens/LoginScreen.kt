package com.fireblocks.sdkdemo.ui.screens

import android.app.Activity.RESULT_OK
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.disabled
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 02/07/2023.
 */

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(),
                onGenerateKeysScreen: () -> Unit,
                onHomeScreen: () -> Unit) { //TODO disable back click
    // Scaffold
    val scaffoldState = rememberBottomSheetScaffoldState(
        //Initially, we need the sheet to be closed
        bottomSheetState = SheetState(true, SheetValue.Expanded, { false }, true),
    )
    var fullScreen by remember { mutableStateOf(false) }
    var nextScreen by remember { mutableStateOf(FireblocksScreen.GenerateKeys) }
    val bottomSheetHeight: Float by animateFloatAsState(
        targetValue = if (fullScreen) 1f else 0.8f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        finishedListener = {
            if (nextScreen == FireblocksScreen.GenerateKeys) {
                onGenerateKeysScreen()
            } else {
                onHomeScreen()
            }
        }, label = ""
    )
    val radius = if (fullScreen) {
        0.dp
    } else {
        dimensionResource(R.dimen.bottom_sheet_round_corners)
    }
    val cornerRadius = animateDpAsState(
        targetValue = radius,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = ""
    )

    BottomSheetScaffold(
        sheetContainerColor = black,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        scaffoldState = scaffoldState,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(cornerRadius.value),
        sheetContent = {
            Column(modifier = Modifier
                .fillMaxHeight(fraction = bottomSheetHeight)) {
                LoginSheetContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_default)),
                    viewModel,
                    onGenerateKeysScreen = {
                        nextScreen = FireblocksScreen.GenerateKeys
                        fullScreen = true
                    },
                    onHomeScreen = {
                        nextScreen = FireblocksScreen.Wallet
                        fullScreen = true
                    }
                )
            }
        }
    ) {
        //Main Screen Content here
        MainContent()
    }

    val userFlow by viewModel.userFlow.collectAsState()
    if (userFlow is UiState.Loading) {
        ProgressBar()
    }
}

@Composable
private fun MainContent() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.login_screen_bg),
            contentDescription = null,
            modifier = Modifier
        )
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = dimensionResource(id = R.dimen.padding_extra_large))
        )
    }
}

@Composable
fun LoginSheetContent(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onGenerateKeysScreen: () -> Unit,
    onHomeScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val signInSelected = uiState.signInSelected
    val prefix = stringResource(id = if (signInSelected) R.string.sing_in else R.string.sign_up)
    val context = LocalContext.current
    addSnackBarObserver(viewModel, LocalLifecycleOwner.current)
    addLoginObserver(viewModel, LocalLifecycleOwner.current, onGenerateKeysScreen, onHomeScreen, context = context)

    var mainModifier = Modifier.fillMaxWidth()
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        mainModifier = Modifier
            .fillMaxSize()
            .alpha(floatResource(R.dimen.progress_alpha))
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }
    Box(
        modifier = modifier,
    ) {
        Column(
            modifier = mainModifier,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                FireblocksText(
                    modifier = Modifier.padding(top = 13.dp),
                    text = stringResource(id = R.string.login_title),
                    textStyle = FireblocksNCWDemoTheme.typography.h2,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                FireblocksText(
                    text = stringResource(id = R.string.login_subtitle),
                    textStyle = FireblocksNCWDemoTheme.typography.b1, modifier = Modifier.padding(top = 13.dp),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))
                SignToggleLayout(viewModel)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                Row(modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically) {
                    Divider(
                        color = grey_2,
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    FireblocksText(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_default)),
                        text = stringResource(id = R.string.choose_an_account),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Start,
                    )
                    Divider(
                        color = grey_2,
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                LaunchedEffect(key1 = uiState.signInState.signInError) {
                    uiState.signInState.signInError?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                GoogleButton(prefix = prefix, signInFlow = signInSelected, viewModel = viewModel)

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                AppleButton(prefix = prefix, signInFlow = signInSelected, viewModel = viewModel)
            }
           if (userFlow is UiState.Error) {
               ErrorView(message = stringResource(id = R.string.login_error, prefix))
           }
           FireblocksText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_default)),
                text = "Env: ${BuildConfig.FLAVOR}",
                textStyle = FireblocksNCWDemoTheme.typography.b2, textColor = disabled,
                textAlign = TextAlign.Center
           )
        }
    }
}

private fun addLoginObserver(viewModel: LoginViewModel,
                             lifecycleOwner: LifecycleOwner,
                             onGenerateKeysScreen: () -> Unit,
                             onHomeScreen: () -> Unit,
                             context: Context
) {
    viewModel.onPassLogin().observe(lifecycleOwner) { observedEvent ->
        observedEvent.contentIfNotHandled?.let { passedLogin ->
            viewModel.showProgress(false)
            if (passedLogin) {
                when(generatedSuccessfully(context)) {
                    true -> onHomeScreen()
                    false -> onGenerateKeysScreen()
                }
            } else {
                viewModel.onError()
            }
        }
    }
}

fun generatedSuccessfully(context: Context): Boolean {
    val status = FireblocksManager.getInstance().getKeyCreationStatus(context, false)
    return generatedSuccessfully(status)
}

fun generatedSuccessfully(keyDescriptors: Set<KeyDescriptor>): Boolean {
    var generatedKeys = keyDescriptors.isNotEmpty()
    keyDescriptors.forEach {
        if (it.keyStatus != KeyStatus.READY) {
            generatedKeys = false
        }
    }
    return generatedKeys
}

private fun addSnackBarObserver(viewModel: LoginViewModel, lifecycleOwner: LifecycleOwner) {
    viewModel.snackBar().observe(lifecycleOwner) { observedEvent ->
        observedEvent.contentIfNotHandled?.let {
            viewModel.onSnackbarChanged(true, it)
        }
    }
}
@Composable
fun SignToggleLayout(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val signInSelected = uiState.signInSelected
    Row(modifier = Modifier
        .background(color = grey_2, shape = RoundedCornerShape(dimensionResource(R.dimen.padding_default)))
        .padding(dimensionResource(id = R.dimen.padding_extra_small))
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        DefaultButton(
            modifier = Modifier.weight(1f),
            selected = signInSelected,
            labelResourceId = R.string.sing_in,
            onClick = {
                viewModel.setSignInSelected(true)
            }
        )
        DefaultButton(
            modifier = Modifier.weight(1f),
            selected = !signInSelected,
            labelResourceId = R.string.sign_up,
            onClick = {
                viewModel.setSignInSelected(false)
            }
        )
    }
}

@Composable
fun GoogleButton(prefix: String,
                 signInFlow: Boolean,
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
                        viewModel.onError()
                    } else {
                        viewModel.handleSuccessSignIn(signInFlow, context, viewModel)
                    }
                }
            } else {
                // failed to sign in
                Timber.e("failed to sign in with google. $result")
                viewModel.onError()
                SignInUtil.getInstance().signOut(context){}
            }
        }
    )

    DefaultButton(
        modifier = Modifier.fillMaxWidth(),
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
                signInFlow: Boolean,
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
                        viewModel.onError()
                    } else {
                        viewModel.handleSuccessSignIn(signInFlow, context, viewModel)
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
fun LoginScreenPreview() {
    FireblocksNCWDemoTheme {
        LoginScreen(onGenerateKeysScreen = {}, onHomeScreen = {})
    }
}

@Preview
@Composable
fun LoginSheetContentPreview(){
    FireblocksNCWDemoTheme {
        LoginSheetContent(viewModel = LoginViewModel(), onGenerateKeysScreen = {}, onHomeScreen = {})
    }
}
