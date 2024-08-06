package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CloseButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.SettingsButton
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.primary_blue
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel

/**
 * Created by Fireblocks Ltd. on 11/07/2023.
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
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {
    Wallet(titleResId = R.string.wallet_top_bar_title, showSettingsButton = true, horizontalArrangement = Arrangement.Start),
    BottomAssets(titleResId = R.string.wallet_top_bar_title, bottomTitleResId = R.string.assets, R.drawable.ic_wallet, showSettingsButton = true, horizontalArrangement = Arrangement.Start),
    BottomTransfers(titleResId = R.string.transfers, bottomTitleResId = R.string.transfers, R.drawable.ic_transfers, showSettingsButton = true),
    Asset(titleResId = R.string.asset_top_bar_title, showCloseButton = true),
    SelectAsset(titleResId = R.string.select_asset_top_bar_title, showCloseButton = true),
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
    onSettingsClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    BackHandler {
        // prevent back click
    }

    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        topBarModifier = Modifier
            .alpha(floatResource(R.dimen.progress_alpha))
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

    val dynamicTitleState = remember { mutableStateOf(TopBarTitleData()) }

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
        WalletScreenNavigationConfigurations(innerPadding, navController, viewModel, uiState, dynamicTitleState, onCloseClicked)
    }
}

@Composable
internal fun WalletTopAppBar(
    modifier: Modifier = Modifier,
    currentScreen: WalletNavigationScreens,
    navigateUp: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onCloseWarningClicked: () -> Unit = {},
    dynamicTitleState: MutableState<TopBarTitleData>,
) {
    var titleText: String? = null
    var labelText: String? = null
     when (currentScreen.showDynamicTitle) {
        true -> {
            titleText = dynamicTitleState.value.titleText
            labelText = dynamicTitleState.value.labelText
        }
        false -> {
            titleText = currentScreen.titleResId?.let { stringResource(it) }
        }
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = currentScreen.horizontalArrangement,) {
                FireblocksText(
                    text = titleText,
                    textStyle = FireblocksNCWDemoTheme.typography.h3,
                )
                if (labelText != null) {
                    Label(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_small)), text = labelText)
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            if (currentScreen.showSettingsButton) {
                SettingsButton(onSettingsClicked)
            }
            if (currentScreen.showCloseButton) {
                CloseButton(onCloseClicked = onCloseClicked)
            }
            if (currentScreen.showCloseWarningButton) {
                CloseButton(onCloseClicked = onCloseWarningClicked)
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

data class TopBarTitleData(var titleText: String? = null, var labelText: String? = null)

@Composable
private fun WalletScreenNavigationConfigurations(
    innerPadding: PaddingValues,
    navController: NavHostController,
    viewModel: WalletViewModel,
    uiState: WalletViewModel.WalletUiState,
    dynamicTitleState: MutableState<TopBarTitleData>,
    onCloseClicked: () -> Unit = {},
) {
    val layoutDirection = LocalLayoutDirection.current
    val bottomPadding = innerPadding.calculateBottomPadding()
    val screenModifier = Modifier.padding(bottom = bottomPadding)

    val modifier = Modifier.padding(
        start = innerPadding.calculateStartPadding(layoutDirection),
        end = innerPadding.calculateEndPadding(layoutDirection),
        top = innerPadding.calculateTopPadding()
    )
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = WalletNavigationScreens.BottomAssets.name) {
        composable(route = WalletNavigationScreens.BottomAssets.name) {
            Box(modifier = screenModifier) {
                AssetListScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onSendClicked = {
                        viewModel.cleanBeforeNewFlow()
                        viewModel.onSendFlow(true)
                        viewModel.onSelectedAsset(it)
                        navController.navigate(WalletNavigationScreens.Amount.name)
                    },
                    onReceiveClicked = {
                        viewModel.onSendFlow(false)
                        viewModel.onSelectedAsset(it)
                        navController.navigate(WalletNavigationScreens.Receive.name)
                    },
                    onAddAssetClicked = {
                        navController.navigate(WalletNavigationScreens.SelectAsset.name)
                    }
                )
            }
        }
        composable(route = WalletNavigationScreens.BottomTransfers.name) {
            Box(modifier = screenModifier) {
                TransferListScreen {
                    viewModel.onTransactionSelected(it)
                    navController.navigate(WalletNavigationScreens.Transfer.name)
                }
            }
        }
        composable(route = WalletNavigationScreens.SelectAsset.name) {
            Box(modifier = screenModifier) {
                SelectAssetScreen(
                    onHomeScreen = { navController.popBackStack(WalletNavigationScreens.BottomAssets.name, inclusive = false) }
                )
            }
        }
        composable(route = WalletNavigationScreens.Asset.name) {
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
        composable(route = WalletNavigationScreens.Amount.name) {
            Box(modifier = screenModifier) {
                AmountScreen(
                    uiState = uiState,
                    onNextScreen = { amount, usdAmount ->
                        viewModel.onAssetAmount(amount)
                        viewModel.onAssetUsdAmount(usdAmount)
                        navController.navigate(WalletNavigationScreens.ReceivingAddress.name)
                    }
                )
            }
        }
        composable(route = WalletNavigationScreens.ReceivingAddress.name) {
            Box(modifier = screenModifier) {
                ReceivingAddressScreen(
                    uiState = uiState,
                ) {
                    viewModel.onSendDestinationAddress(it)
                    navController.navigate(WalletNavigationScreens.Fee.name)
                }
            }
        }
        composable(route = WalletNavigationScreens.Fee.name) {
            Box(modifier = screenModifier) {
                FeeScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNextScreen = {
                        navController.navigate(WalletNavigationScreens.Preview.name)
                    }
                )
            }
        }
        composable(route = WalletNavigationScreens.Preview.name) {
            PreviewScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNextScreen = { navController.navigate(WalletNavigationScreens.Sending.name) },
                onDiscard = onCloseClicked,
                bottomPadding = bottomPadding
            )
        }
        composable(route = WalletNavigationScreens.Sending.name) {
            Box(modifier = screenModifier) {
                SendingScreen(
                    uiState = uiState
                ) {
                    navController.navigate(WalletNavigationScreens.Transfer.name)
                }
            }
        }
        composable(route = WalletNavigationScreens.Transfer.name) {
            uiState.transactionWrapper?.transaction?.details?.let { transactionDetails ->
                val assetId = transactionDetails.assetId ?: ""
                val asset = viewModel.getAsset(assetId)
                transactionDetails.asset = asset
                val deviceId = viewModel.getDeviceId(LocalContext.current)
                val titleData = TopBarTitleData()
                if (uiState.transactionWrapper.isOutgoingTransaction(LocalContext.current, deviceId)) {
                    titleData.titleText = stringResource(id = R.string.sent_top_bar_title, assetId)
                } else {
                    titleData.titleText = stringResource(id = R.string.received_top_bar_title, assetId)
                }
                titleData.labelText = transactionDetails.feeCurrency
                dynamicTitleState.value = titleData
            }
            Box(modifier = screenModifier) {
                TransferScreen(
                    uiState.transactionWrapper,
                    onGoBack = { navController.popBackStack() }
                )
            }
        }
        composable(route = WalletNavigationScreens.Receive.name) {
            Box(modifier = screenModifier) {
                ReceiveScreen(uiState = uiState)
            }
        }
    }
}

@Composable
fun WalletBottomBar(
    navController: NavHostController,
    items: List<WalletNavigationScreens>
) {
    NavigationBar(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large)),
        containerColor = transparent,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.name } == true
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primary_blue,
                    unselectedIconColor = grey_4,
                    indicatorColor = Color.Transparent
                ),
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
                        FireblocksText(
                            text = stringResource(id = screen.bottomTitleResId),
                            textStyle = FireblocksNCWDemoTheme.typography.b2,
                            textColor = if (selected) Color.White else grey_4
                        )
                    }
                },
                selected = selected,
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
                        // re selecting the same item
                        launchSingleTop = true
                        // Restore state when re selecting a previously selected item
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
        ) {}
    }
}