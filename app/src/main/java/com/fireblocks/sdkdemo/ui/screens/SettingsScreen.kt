package com.fireblocks.sdkdemo.ui.screens

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.rememberSheetState
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.ProfileIcon
import com.fireblocks.sdkdemo.ui.compose.components.SDKVersionsLabel
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.signin.UserData
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.disabled
import com.fireblocks.sdkdemo.ui.theme.disabled_grey
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.text_tertiary
import com.fireblocks.sdkdemo.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by Fireblocks Ltd. on 02/07/2023.
 */
@Composable
fun SettingsScreen(
    onClose: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onAdvancedInfo: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onRecoverWallet: () -> Unit = {},
    onExportPrivateKey: () -> Unit = {},
    onAddNewDevice: () -> Unit = {},
    onGenerateKeys: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            FireblocksTopAppBar(
                modifier = Modifier,
                currentScreen = FireblocksScreen.Settings,
                canNavigateBack = false,
                navigateUp = {},
                onMenuActionClicked = onClose,
                menuActionType = TopBarMenuActionType.Close
            )
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
//        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomPadding = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding()
                )
        ) {
            val coroutineScope = rememberCoroutineScope()
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                //Initially, we need the sheet to be closed
                bottomSheetState = rememberSheetState(
                    true,
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false
                ),
            )
            SignOutBottomSheet(
                bottomSheetScaffoldState,
                coroutineScope,
                onSignOut,
                onAdvancedInfo,
                onCreateBackup,
                onRecoverWallet,
                onExportPrivateKey,
                onAddNewDevice,
                onGenerateKeys,
                bottomPadding = bottomPadding)
        }
    }
}

@Composable
fun SettingsMainContent(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    onAdvancedInfo: () -> Unit,
    onCreateBackup: () -> Unit,
    onRecoverWallet: () -> Unit,
    onExportPrivateKey: () -> Unit = {},
    onAddNewDevice: () -> Unit = {},
    onGenerateKeys: () -> Unit = {},
    userData: UserData?,
    viewModel: SettingsViewModel = viewModel(),
    bottomPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val isKeyReady = isKeyReady(context)
    val isSignedIn = SignInUtil.getInstance().isSignedIn(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = dimensionResource(id = R.dimen.padding_default),
                end = dimensionResource(id = R.dimen.padding_default),
                bottom = bottomPadding
            )
            .background(color = background),
        verticalArrangement = Arrangement.Top
    ) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier
            .weight(1f)
            .verticalScroll(state = scrollState, flingBehavior = ScrollableDefaults.flingBehavior())) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileIcon(userData?.profilePictureUrl)
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    text = userData?.userName,
                    textStyle = FireblocksNCWDemoTheme.typography.h3,
                )
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                    text = userData?.email,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = text_secondary
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                VersionAndEnvironmentLabel(modifier = Modifier.align(Alignment.CenterHorizontally))
                SDKVersionsLabel(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = dimensionResource(R.dimen.padding_small)), ncwVersion = viewModel.getNCWVersion())
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(vertical = dimensionResource(R.dimen.padding_extra_large_1)))
            {
                // Wallet Actions
                FireblocksText(
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small_2)),
                    text = stringResource(id = R.string.wallet_actions),
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = text_tertiary
                )
                SettingsItemButton(
                    enabled = isSignedIn,
                    labelResourceId = R.string.recover_wallet,
                    iconResourceId = R.drawable.ic_recover_wallet,
                    shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_small)),
                    onClick = onRecoverWallet,
                )
                SettingsItemButton(
                    enabled = isKeyReady,
                    labelResourceId = R.string.create_a_backup,
                    iconResourceId = R.drawable.ic_create_a_backup,
                    onClick = onCreateBackup,
                )
                SettingsItemButton(
                    enabled = isKeyReady,
                    labelResourceId = R.string.export_private_key,
                    iconResourceId = R.drawable.ic_export_keys,
                    onClick = onExportPrivateKey,
                )
                if (isDevFlavor()) {
                    SettingsItemButton(
                        enabled = isSignedIn,
                        labelResourceId = R.string.generate_keys,
                        iconResourceId = R.drawable.ic_export_keys,
                        onClick = onGenerateKeys,
                    )
                }
                SettingsItemButton(
                    enabled = isKeyReady,
                    labelResourceId = R.string.add_new_device,
                    iconResourceId = R.drawable.ic_add_new_device,
                    divider = false,
                    shape = RoundedCornerShape(
                        bottomStart = dimensionResource(id = R.dimen.round_corners_small),
                        bottomEnd = dimensionResource(id = R.dimen.round_corners_small)
                    ),
                    onClick = onAddNewDevice,
                )
                // General Settings
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_large_1), bottom = dimensionResource(id = R.dimen.padding_small_2)),
                    text = stringResource(id = R.string.advanced),
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    textColor = text_tertiary
                )
                SettingsItemButton(
                    labelResourceId = R.string.advanced_info,
                    iconResourceId = R.drawable.ic_advanced_info,
                    shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_small)),
                    onClick = onAdvancedInfo,
                )
                SettingsItemButton(
                    divider = false,
                    labelResourceId = R.string.share_logs,
                    iconResourceId = R.drawable.ic_share,
                    shape = RoundedCornerShape(
                        bottomStart = dimensionResource(id = R.dimen.round_corners_small),
                        bottomEnd = dimensionResource(id = R.dimen.round_corners_small)
                    ),
                    onClick = {
                        viewModel.shareLogs(context)
                    }
                )
                SettingsItemButton(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_large_1), bottom = dimensionResource(id = R.dimen.padding_small_2)),
                    labelResourceId = R.string.sign_out,
                    iconResourceId = R.drawable.ic_sign_out_small,
                    divider = false,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_corners_small)),
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun isDevFlavor() = BuildConfig.FLAVOR_server == "dev"

@Composable
private fun isKeyReady(context: Context): Boolean {
    var enabled = false
    var status: Set<KeyDescriptor>
    runCatching {
        if (SignInUtil.getInstance().isSignedIn(context)) {
            status = FireblocksManager.getInstance().getKeyCreationStatus(context)
            val readyKey = status.firstOrNull {
                it.keyStatus == KeyStatus.READY
            }
            enabled = readyKey != null
        }
    }.onFailure {
        DialogUtil.getInstance().start("Failure", "${it.message}", buttonText = context.getString(R.string.OK), autoCloseTimeInMillis = 3000)
    }
    return enabled
}

@Composable
fun SignOutBottomSheet(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    onSignOut: () -> Unit = {},
    onAdvancedInfo: () -> Unit = {},
    onCreateBackup: () -> Unit = {},
    onRecoverWallet: () -> Unit = {},
    onExportPrivateKey: () -> Unit = {},
    onAddNewDevice: () -> Unit = {},
    onGenerateKeys: () -> Unit = {},
    bottomPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
//    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    BottomSheetScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }
                }
            })
        },
        sheetContainerColor = grey_2,
        containerColor = background,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Column(modifier = Modifier
                .fillMaxHeight(fraction = 0.45f)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = painterResource(id = R.drawable.ic_sign_out),
                        contentDescription = null,
                    )
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                        text = stringResource(id = R.string.sign_out_warning),
                        textStyle = FireblocksNCWDemoTheme.typography.h3,
                    )
                    DefaultButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.sign_out,
                        colors = ButtonDefaults.buttonColors(containerColor = grey_1),
                        onClick = onSignOut
                    )
                    TransparentButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.never_mind,
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }
                    )
                }
            }
        }
    ) {
        SettingsMainContent(
            bottomSheetScaffoldState = bottomSheetScaffoldState,
            coroutineScope = coroutineScope,
            onAdvancedInfo = { onAdvancedInfo() },
            onCreateBackup = { onCreateBackup() },
            onRecoverWallet = { onRecoverWallet() },
            onExportPrivateKey = { onExportPrivateKey() },
            onAddNewDevice = { onAddNewDevice() },
            onGenerateKeys = { onGenerateKeys() },
            userData = SignInUtil.getInstance().getUserData(context),
            bottomPadding = bottomPadding
        )
    }
}

@Composable
fun SettingsItemButton(
    @StringRes labelResourceId: Int,
    @DrawableRes iconResourceId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(0),
    divider: Boolean = true,
) {
    val text = stringResource(labelResourceId)
    val description = text.replace("\n", " ")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
            .background(
                color = if (enabled) grey_1 else disabled_grey,
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
        ,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Card(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
                colors = CardDefaults.cardColors(containerColor = grey_2),
            ) {
                Image(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    painter = painterResource(iconResourceId),
                    contentDescription = null,
                    colorFilter = if (!enabled) ColorFilter.tint(disabled) else null
                )
            }
            FireblocksText(
                modifier = Modifier.weight(1f),
                text = text,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = if (enabled) Color.White else disabled
            )
            Image(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
                painter = painterResource(R.drawable.ic_next_arrow),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (enabled) text_tertiary else disabled)
            )
        }
        if (divider) {
            Divider(color = grey_2,)
        }
    }
}

@Preview
@Composable
fun SettingsItemButtonPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            SettingsItemButton(
                labelResourceId = R.string.create_a_backup,
                iconResourceId = R.drawable.ic_create_a_backup,
                onClick = {}
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SettingsMainContentPreview() {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    FireblocksNCWDemoTheme {
        Surface {
            SettingsMainContent(
                bottomSheetScaffoldState = bottomSheetScaffoldState,
                coroutineScope = coroutineScope,
                onAdvancedInfo = {},
                onCreateBackup = {},
                onRecoverWallet = {},
                userData = UserData("xxx@fireblocks.com", "John Do", null, idToken = null)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SettingsScreenPreview() {
    FireblocksNCWDemoTheme {
        Surface {
            SettingsScreen(
                onClose = {},
                onAdvancedInfo = {},
                onCreateBackup = {},
                onRecoverWallet = {},
                onSignOut = {},
            )
        }
    }
}

@Preview
@Composable
fun SignOutBottomSheetPreview() {
    FireblocksNCWDemoTheme {
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded,
                skipHiddenState = false
            )
        )
        SignOutBottomSheet(bottomSheetScaffoldState, coroutineScope, {}, {}, {}, {})
    }
}
