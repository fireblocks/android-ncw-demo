package com.fireblocks.sdkdemo.ui.screens.wallet

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CloseButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.SettingsButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks ltd. on 11/07/2023.
 */

enum class WalletNavigationScreens(
    @StringRes val titleResId: Int? = null,
    @StringRes val bottomTitleResId: Int? = null,
    @DrawableRes val iconResId: Int? = null,
    val showNavigateBack: Boolean = false,
    val showSettingsButton: Boolean = false,
    val showCloseButton: Boolean = false,
    val showCloseWarningButton: Boolean = false,
    val showDynamicTitle: Boolean = false,
) {
    Wallet(titleResId = R.string.wallet_top_bar_title, showSettingsButton = true),
    BottomAssets(titleResId = R.string.wallet_top_bar_title, bottomTitleResId = R.string.assets, R.drawable.ic_wallet, showSettingsButton = true),
    BottomTransfers(titleResId = R.string.transfers, bottomTitleResId = R.string.transfers, R.drawable.ic_transfers, showSettingsButton = true),
    Asset(titleResId = R.string.asset_top_bar_title, showCloseButton = true),
    Amount(titleResId = R.string.amount_top_bar_title, showCloseButton = true, showNavigateBack = true),
    ReceivingAddress(titleResId = R.string.receiving_address_top_bar_title, showCloseButton = true, showNavigateBack = true),
    Fee(titleResId = R.string.fee_top_bar_title, showCloseButton = true, showNavigateBack = true),
    Preview(titleResId = R.string.preview_top_bar_title, showCloseWarningButton = true),
    Sending(showCloseButton = true),
    Transfer(showNavigateBack = true, showDynamicTitle = true),
    Receive(titleResId = R.string.receive_top_bar_title, showNavigateBack = true, showCloseButton = true),
}

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = viewModel(),
    onSettingsClicked: () -> Unit,
    afterRecover: Boolean
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    BackHandler {
        // prevent back click
    }

    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        topBarModifier = Modifier
            .alpha(0.5f)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    val navController = rememberNavController()
    val bottomNavigationItems = listOf(
        WalletNavigationScreens.BottomAssets,
        WalletNavigationScreens.BottomTransfers,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = WalletNavigationScreens.valueOf(
        navBackStackEntry?.destination?.route ?: WalletNavigationScreens.Wallet.name
    )

    val dynamicTitleState = remember { mutableStateOf("") }

    val onCloseClicked: () -> Unit = {
        navController.popBackStack(WalletNavigationScreens.BottomAssets.name, inclusive = false)
    }

    val onCloseWarningClicked: () -> Unit = {
        viewModel.onCloseWarningClicked(true)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            WalletTopAppBar(
                modifier = topBarModifier,
                currentScreen = currentScreen,
                dynamicTitleState = dynamicTitleState,
                navigateUp = { navController.popBackStack() },
                onSettingsClicked = onSettingsClicked,
                onCloseClicked = onCloseClicked,
                onCloseWarningClicked = onCloseWarningClicked
            )
        },
        bottomBar = {
            if (currentScreen == WalletNavigationScreens.Wallet || currentScreen == WalletNavigationScreens.BottomAssets || currentScreen == WalletNavigationScreens.BottomTransfers) {
                WalletBottomBar(navController, bottomNavigationItems)
            }
        },
    ) { innerPadding ->
        LaunchedEffect(key1 = afterRecover) {
            if (afterRecover) {
                Toast.makeText(context, context.getString(R.string.wallet_recovered), Toast.LENGTH_SHORT).show()
            }
        }
        WalletScreenNavigationConfigurations(innerPadding, navController, viewModel, uiState, dynamicTitleState, onCloseClicked)
    }
}

@Composable
internal fun WalletTopAppBar(
    currentScreen: WalletNavigationScreens,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClicked: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onCloseWarningClicked: () -> Unit = {},
    dynamicTitleState: MutableState<String>,
) {
    val text = when (currentScreen.showDynamicTitle) {
        true -> dynamicTitleState.value
        false -> currentScreen.titleResId?.let { stringResource(it) }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            FireblocksText(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                textStyle = FireblocksNCWDemoTheme.typography.h3,
                textAlign = TextAlign.Center,
            )
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            if (currentScreen.showSettingsButton) {
                SettingsButton(onSettingsClicked)
            }
            if (currentScreen.showCloseButton) {
                CloseButton(onCloseClicked)
            }
            if (currentScreen.showCloseWarningButton) {
                CloseButton(onCloseWarningClicked)
            }
        },
        navigationIcon = {
            if (currentScreen.showNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
    )
}

@Composable
private fun WalletScreenNavigationConfigurations(
    innerPadding: PaddingValues,
    navController: NavHostController,
    viewModel: WalletViewModel,
    uiState: WalletViewModel.WalletUiState,
    dynamicTitleState: MutableState<String>,
    onCloseClicked: () -> Unit = {},
) {
    NavHost(
        modifier = Modifier.padding(innerPadding),
        navController = navController,
        startDestination = WalletNavigationScreens.BottomAssets.name) {
        composable(WalletNavigationScreens.BottomAssets.name) {
            AssetsScreen(
                uiState = uiState,
                viewModel = viewModel,
                onSendClicked = {
                    viewModel.cleanBeforeNewFlow()
                    viewModel.onSendFlow(true)
                    navController.navigate(WalletNavigationScreens.Asset.name)
                },
                onReceiveClicked = {
                    viewModel.onSendFlow(false)
                    navController.navigate(WalletNavigationScreens.Asset.name)
                }
            )
        }
        composable(WalletNavigationScreens.BottomTransfers.name) {
            TransfersScreen {
                viewModel.onTransactionSelected(it)
                navController.navigate(WalletNavigationScreens.Transfer.name)
            }
        }
        composable(WalletNavigationScreens.Asset.name) {
            AssetScreen(
                uiState = uiState,
                onNextScreen = {
                    viewModel.onSelectedAsset(it)
                    when (uiState.sendFlow){
                        true -> navController.navigate(WalletNavigationScreens.Amount.name)
                        false -> navController.navigate(WalletNavigationScreens.Receive.name)
                    }
                }
            )
        }
        composable(
            route = WalletNavigationScreens.Amount.name) {
            AmountScreen(
                uiState = uiState,
                onNextScreen = { amount, usdAmount ->
                    viewModel.onAssetAmount(amount)
                    viewModel.onAssetUsdAmount(usdAmount)
                    navController.navigate(WalletNavigationScreens.ReceivingAddress.name)
                }
            )
        }
        composable(
            route = WalletNavigationScreens.ReceivingAddress.name) {
            ReceivingAddressScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNextScreen = {
                    viewModel.onSendDestinationAddress(it)
                    navController.navigate(WalletNavigationScreens.Fee.name)
                },
            )
        }
        composable(
            route = WalletNavigationScreens.Fee.name) {
            FeeScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNextScreen = {
                    navController.navigate(WalletNavigationScreens.Preview.name)
                }
            )
        }
        composable(
            route = WalletNavigationScreens.Preview.name) {
            PreviewScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNextScreen = { navController.navigate(WalletNavigationScreens.Sending.name) },
                onDiscard = onCloseClicked
            )
        }
        composable(
            route = WalletNavigationScreens.Sending.name) {
            SendingScreen(
                uiState = uiState
            ) {
                navController.navigate(WalletNavigationScreens.Transfer.name)
            }
        }
        composable(
            route = WalletNavigationScreens.Transfer.name) {
            uiState.transactionWrapper?.transaction?.details?.let { transactionDetails ->
                val assetId = transactionDetails.assetId ?: ""
                val deviceId = MultiDeviceManager.instance.lastUsedDeviceId()
                if (uiState.transactionWrapper.isOutgoingTransaction(LocalContext.current, deviceId)) {
                    dynamicTitleState.value = stringResource(id = R.string.sent_top_bar_title, assetId)
                } else {
                    dynamicTitleState.value = stringResource(id = R.string.received_top_bar_title, assetId)
                }
            }
            TransferScreen(
                uiState.transactionWrapper,
                onGoBack = { navController.popBackStack() }
            )
        }
        composable(
            route = WalletNavigationScreens.Receive.name) {
            ReceiveScreen(uiState = uiState)
        }
    }
}

@Composable
fun WalletBottomBar(
    navController: NavHostController,
    items: List<WalletNavigationScreens>
) {
    NavigationBar(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large)),containerColor = transparent) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.iconResId?.let { iconResId ->
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = screen.name,
                        )
                    }
                },
                label = {
                    screen.bottomTitleResId?.let {
                        FireblocksText(text = stringResource(id = screen.bottomTitleResId), textStyle = FireblocksNCWDemoTheme.typography.b2)
                    }
                },
                selected = currentDestination?.hierarchy?.any { it.route == screen.name } == true,
                onClick =
                {
                    navController.navigate(screen.name) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
            )
        }
    }
}


@Preview
@Composable
fun WalletScreenPreview() {
    val viewModel = WalletViewModel()
    val assets = arrayListOf<SupportedAsset>()
    assets.add(SupportedAsset(id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum",
        balance = "132.4",
        price = "2,825.04"))
    assets.add(SupportedAsset(id = "SOL",
        symbol = "SOL",
        name = "Solana",
        type = "BASE_ASSET",
        blockchain = "Solana",
        balance = "217",
        price = "1,336.72"))
    assets.add(SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93"))
    viewModel.onAssets(assets)

    viewModel.onBalance("45,873.03")

    FireblocksNCWDemoTheme {
        WalletScreen(
            viewModel = viewModel,
            onSettingsClicked = {},
            afterRecover = false,
        )
    }
}