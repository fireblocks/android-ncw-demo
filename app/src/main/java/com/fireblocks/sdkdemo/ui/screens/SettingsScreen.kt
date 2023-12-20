package com.fireblocks.sdkdemo.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.rememberSheetState
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.DefaultButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.TransparentButton
import com.fireblocks.sdkdemo.ui.compose.components.VersionAndEnvironmentLabel
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.signin.UserData
import com.fireblocks.sdkdemo.ui.theme.black
import com.fireblocks.sdkdemo.ui.theme.disabled
import com.fireblocks.sdkdemo.ui.theme.disabled_grey
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_4
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding())
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
                onExportPrivateKey)
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
    userData: UserData?,
    viewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val isKeyReady = isKeyReady(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
            .background(color = black),
        verticalArrangement = Arrangement.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (userData?.profilePictureUrl.isNotNullAndNotEmpty()) {
                    AsyncImage(
                        model = userData?.profilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_avatar_circle),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }
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
                    textColor = grey_4
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                BackupButton(modifier = Modifier.weight(1f), enabled = isKeyReady, onCreateBackup = onCreateBackup)
                SettingsItemButton(
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.recover_wallet,
                    iconResourceId = R.drawable.ic_recover_wallet,
                    onClick = { onRecoverWallet() },
                )
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                SettingsItemButton(
                    enabled = isKeyReady,
                    modifier = Modifier.weight(1f),
                    labelResourceId = R.string.export_private_key,
                    iconResourceId = R.drawable.ic_export_keys,
                    onClick = { onExportPrivateKey() },
                )
                AdvancedInfoButton(modifier = Modifier.weight(1f), onAdvancedInfo = onAdvancedInfo)
            }
        }

        // Sign out button
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))
        DefaultButton(
            modifier = Modifier.fillMaxWidth(),
            labelResourceId = R.string.sing_out,
            onClick = {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.expand()
                }
            }
        )
        TransparentButton(
            modifier= Modifier.padding(vertical = dimensionResource(R.dimen.padding_default)),
            labelResourceId = R.string.share_logs,
            onClick = {
                viewModel.shareLogs(context)
            }
        )
        VersionAndEnvironmentLabel(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = dimensionResource(R.dimen.padding_extra_large)))
    }
}

@Composable
fun BackupButton(modifier: Modifier = Modifier, enabled: Boolean, onCreateBackup: () -> Unit) {
    SettingsItemButton(
        enabled = enabled,
        modifier = modifier,
        labelResourceId = R.string.create_a_backup,
        iconResourceId = R.drawable.ic_create_a_backup,
        onClick = { onCreateBackup() },
    )
}

@Composable
private fun isKeyReady(context: Context): Boolean {
    var enabled = false
    var status: Set<KeyDescriptor>
    runCatching {
        status = FireblocksManager.getInstance().getKeyCreationStatus(context, false)
        val readyKey = status.firstOrNull {
            it.keyStatus == KeyStatus.READY
        }
        enabled = readyKey != null
    }.onFailure {
        DialogUtil.getInstance().start("Failure", "${it.message}", buttonText = context.getString(R.string.OK), autoCloseTimeInMillis = 3000)
    }
    return enabled
}

@Composable
fun AdvancedInfoButton(modifier: Modifier = Modifier, onAdvancedInfo: () -> Unit) {
    SettingsItemButton(
        enabled = true,
        modifier = modifier,
        labelResourceId = R.string.advanced_info,
        iconResourceId = R.drawable.ic_advanced_info,
        onClick = { onAdvancedInfo() }
    )
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
) {
    val context = LocalContext.current
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
        containerColor = black,
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
                        modifier = Modifier.padding(top = 13.dp),
                        text = stringResource(id = R.string.sign_out_warning),
                        textStyle = FireblocksNCWDemoTheme.typography.h3,
                    )
                    ColoredButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_default)),
                        labelResourceId = R.string.sign_out,
                        onClick = {
                            coroutineScope.launch {
                                SignInUtil.getInstance().signOut(context){
                                    Toast.makeText(context, context.getString(R.string.signed_out), Toast.LENGTH_LONG).show()
                                    FireblocksManager.getInstance().stopPolling()
                                    onSignOut()
                                }
                            }
                        }
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
            userData = SignInUtil.getInstance().getUserData(context)
        )
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

@Composable
fun SettingsItemButton(
    @StringRes labelResourceId: Int,
    @DrawableRes iconResourceId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val text = stringResource(labelResourceId)
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.settings_button_height))
            .semantics { contentDescription = text },
        enabled = enabled,
        shape = RoundedCornerShape(10),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = grey_1, disabledContainerColor = disabled_grey),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                painter = painterResource(iconResourceId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (enabled) Color.White else disabled)
            )
            FireblocksText(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                text = text,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = if (enabled) Color.White else disabled
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