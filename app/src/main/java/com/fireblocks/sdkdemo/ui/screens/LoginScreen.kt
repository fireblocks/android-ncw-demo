package com.fireblocks.sdkdemo.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.bl.core.storage.CertificateStore.Companion.prefix
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.disabled_grey
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.semiTransparentBlue
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel


/**
 * Created by Fireblocks Ltd. on 02/07/2023.
 */

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(),
                onNextScreen: () -> Unit = {}) {
    BackHandler {
        // prevent back click
    }
    // Scaffold
    val scaffoldState = rememberBottomSheetScaffoldState(
        //Initially, we need the sheet to be closed
        bottomSheetState = SheetState(true, SheetValue.Expanded, { false }, true),
    )
    val bottomSheetHeight = 0.8f
    val radius = dimensionResource(R.dimen.bottom_sheet_round_corners)
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
                    onNextScreen = onNextScreen
                )
            }
        }
    ) {
        //Main Screen Content here
        MainContent(viewModel)
    }

    val userFlow by viewModel.userFlow.collectAsState()
    if (userFlow is UiState.Loading) {
        ProgressBar()
    }
}

@Composable
private fun MainContent(viewModel: LoginViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.login_screen_bg),
            contentDescription = null,
            modifier = Modifier
        )
        Column(modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = dimensionResource(id = R.dimen.padding_extra_large)),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
            )
        }
        VersionAndEnvironmentLabel(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = dimensionResource(id = R.dimen.login_screen_build_top_padding)),
            //modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large)),
            backgroundColor = semiTransparentBlue,
            borderColor = transparent,
            ncwVersion = viewModel.getNCWVersion())
    }
}

@Composable
fun SendLogsButton(modifier: Modifier = Modifier, colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), onClicked: () -> Unit,){
    //
    Button(
        modifier = modifier,
        onClick = onClicked,
        colors = colors,
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = android.R.drawable.ic_menu_share),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = stringResource(R.string.share_logs)
        )
    }
}

@Composable
fun LoginSheetContent(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onNextScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    val context = LocalContext.current

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
                Box(modifier = Modifier.fillMaxWidth(),
                ) {
                    SendLogsButton(modifier = Modifier.align(Alignment.CenterEnd), onClicked = {
                        viewModel.emailAllLogs(context)
                    })
                    FireblocksText(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.login_title),
                        textStyle = FireblocksNCWDemoTheme.typography.h2,
                    )
                }
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                    text = stringResource(id = R.string.login_subtitle),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = grey_4,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large_2)))

                LaunchedEffect(key1 = uiState.signInState.signInError) {
                    uiState.signInState.signInError?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                    ExistingAccountButton(modifier = Modifier.weight(1f), viewModel = viewModel, onNextScreen = onNextScreen)
                    NewAccountButton(modifier = Modifier.weight(1f), viewModel = viewModel, onNextScreen = onNextScreen)
                }
            }
           if (userFlow is UiState.Error) {
               ErrorView(message = stringResource(id = R.string.login_error, prefix))
           }
            Row(modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically) {
                Divider(
                    color = grey_2,
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            JoinWalletButton(viewModel, onNextScreen)
        }
    }
}

@Composable
private fun JoinWalletButton(viewModel: LoginViewModel, onNextScreen: () -> Unit) {
    DefaultButton(
        modifier = Modifier.fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_default), bottom = dimensionResource(R.dimen.padding_large)),
        labelText = stringResource(R.string.sing_in_with_a_new_device_description),
        onClick = {
            viewModel.setLoginFlow(LoginViewModel.LoginFlow.JOIN_WALLET)
            onNextScreen()
        },
        colors = ButtonDefaults.buttonColors(containerColor = grey_1, contentColor = Color.White),
    )
}

@Composable
fun ExistingAccountButton(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    onNextScreen: () -> Unit = {},
) {

    LoginItemButton(
        modifier = modifier,
        iconResourceId = R.drawable.ic_existing_account,
        titleResId = R.string.existing_user,
        contentText = stringResource(id = R.string.existing_account_desc),
        onClick = {
            viewModel.setLoginFlow(LoginViewModel.LoginFlow.SIGN_IN)
            onNextScreen()
        }
    )
}

@Composable
fun NewAccountButton(modifier: Modifier = Modifier,
                     viewModel: LoginViewModel,
                     onNextScreen: () -> Unit) {
    LoginItemButton(
        modifier = modifier,
        iconResourceId = R.drawable.ic_new_account,
        titleResId = R.string.new_user,
        contentText = stringResource(id = R.string.new_user_desc),
        onClick = {
            viewModel.setLoginFlow(LoginViewModel.LoginFlow.SIGN_UP)
            onNextScreen()
        }
    )
}

@Composable
fun LoginItemButton(
    modifier: Modifier = Modifier,
    @DrawableRes iconResourceId: Int,
    @StringRes titleResId: Int? = null,
    titleText: String? = null,
    contentText: String? = null,
    onClick: () -> Unit,
    contentDescriptionText: String = ""
) {
    val title = titleResId?.let { stringResource(id = it) } ?: titleText
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.settings_button_height))
            .semantics { contentDescription = contentDescriptionText },
        shape = RoundedCornerShape(10),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = grey_1, disabledContainerColor = disabled_grey),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                painter = painterResource(iconResourceId),
                contentDescription = null,
            )
            TitleContentView(
                topPadding = R.dimen.padding_small_2,
                titleText = title,
                titleColor = white,
                contentText = contentText,
                contentTextStyle = FireblocksNCWDemoTheme.typography.b4,)
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    FireblocksNCWDemoTheme {
        LoginScreen()
    }
}

@Preview
@Composable
fun LoginSheetContentPreview(){
    FireblocksNCWDemoTheme {
        LoginSheetContent(viewModel = LoginViewModel())
    }
}
