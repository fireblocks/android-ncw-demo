package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.fireblocks.sdkdemo.BuildConfig
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.CloseButton
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.SettingsButton
import com.fireblocks.sdkdemo.ui.compose.components.TopBarEmptySideBox
import com.fireblocks.sdkdemo.ui.compose.utils.AssetsUtils
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.addAdditionalScreens
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletUiState
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel

/**
 * Created by Fireblocks Ltd. on 11/07/2023.
 */

enum class WalletNavigationScreens(
    @StringRes val titleResId: Int? = null,
    @StringRes val bottomTitleResId: Int? = null,
    @DrawableRes val iconResId: Int? = null,
    val showNavigateBack: Boolean = false,
    val showLogo: Boolean = false,
    val showSettingsButton: Boolean = false,
    val showCloseButton: Boolean = false,
    val showCloseWarningButton: Boolean = false,
    val showDynamicTitle: Boolean = false,
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {
    Wallet(titleResId = R.string.wallet_top_bar_title, showSettingsButton = true, horizontalArrangement = Arrangement.Start, showLogo = true),
    BottomAssets(titleResId = R.string.wallet_top_bar_title, bottomTitleResId = R.string.assets, showSettingsButton = true, horizontalArrangement = Arrangement.Start, showLogo = true),
    BottomTransfers(titleResId = R.string.transfer_history, bottomTitleResId = R.string.transfers, showSettingsButton = true),
    BottomNFTs(titleResId = R.string.nfts, bottomTitleResId = R.string.nfts, showSettingsButton = true),
    NFTDetails(titleResId = R.string.nft_details, showNavigateBack = true),
    NFTReceivingAddress(titleResId = R.string.nft_transfer_top_bar_title, showNavigateBack = true),
    NFTFeeScreen(titleResId = R.string.fee_top_bar_title, showNavigateBack = true),
    BottomWeb3(titleResId = R.string.web3_connections, bottomTitleResId = R.string.web3, showSettingsButton = true),
    Web3(titleResId = R.string.web3_connected_app, showNavigateBack = true),
    Web3ConnectionReceivingAddress(titleResId = R.string.add_web3_connection, showNavigateBack = true),
    Web3ConnectionPreview(titleResId = R.string.review_web3_connection, showCloseButton = true),
    Asset(titleResId = R.string.asset_top_bar_title, showCloseButton = true),
    SelectAsset(titleResId = R.string.select_asset_top_bar_title, showCloseButton = true),
    Amount(titleResId = R.string.amount_top_bar_title, showCloseButton = true, showNavigateBack = true),
    ReceivingAddress(titleResId = R.string.receiving_address_top_bar_title, showCloseButton = true, showNavigateBack = true),
    Fee(titleResId = R.string.fee_top_bar_title, showCloseButton = true, showNavigateBack = true),
    TransferApproval(showCloseWarningButton = true, showDynamicTitle = true),
    Sending(showCloseButton = true, showDynamicTitle = true),
    TransferDetails(titleResId = R.string.transfer_details,showNavigateBack = true),
    Receive(titleResId = R.string.receive_top_bar_title, showNavigateBack = true, showCloseButton = true),
}

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = viewModel(),
    onSettingsClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nfTsViewModel: NFTsViewModel = viewModel()
    val web3ViewModel: Web3ViewModel = viewModel()

    BackHandler {
        // prevent back click
    }

    val navController = rememberNavController()
    val bottomNavigationItems = arrayListOf(
        WalletNavigationScreens.BottomAssets,
        WalletNavigationScreens.BottomTransfers,
    )
    if (BuildConfig.FLAVOR_wallet == "embeddedWallet") {
        bottomNavigationItems += WalletNavigationScreens.BottomNFTs
        bottomNavigationItems += WalletNavigationScreens.BottomWeb3
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route?.substringBefore("/")
    val currentScreen = WalletNavigationScreens.valueOf(
        route ?: WalletNavigationScreens.Wallet.name
    )

    val dynamicTitleState = remember { mutableStateOf(TopBarTitleData()) }

    val onCloseClicked: () -> Unit = {
        val navigationRoute = when (currentScreen) {
            WalletNavigationScreens.Web3ConnectionPreview -> {
                web3ViewModel.discardWeb3Connection()
                web3ViewModel.partialClean()
                WalletNavigationScreens.BottomWeb3.name
            }
            else -> WalletNavigationScreens.BottomAssets.name
        }
        navController.popBackStack(navigationRoute, inclusive = false)
    }

    val onCloseWarningClicked: () -> Unit = {
        viewModel.onCloseWarningClicked(true)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            WalletTopAppBar(
                currentScreen = currentScreen,
                dynamicTitleState = dynamicTitleState,
                navigateUp = { navController.popBackStack() },
                onSettingsClicked = onSettingsClicked,
                onCloseClicked = onCloseClicked,
                onCloseWarningClicked = onCloseWarningClicked
            )
        },
        bottomBar = {
            if (currentScreen == WalletNavigationScreens.Wallet || bottomNavigationItems.contains(currentScreen)) {
                WalletBottomBar(navController, bottomNavigationItems)
            }
        },
    ) { innerPadding ->
        WalletScreenNavigationConfigurations(innerPadding, navController, viewModel, uiState, dynamicTitleState, onCloseClicked, nfTsViewModel = nfTsViewModel, web3ViewModel = web3ViewModel)
    }
}

@Composable
internal fun WalletTopAppBar(
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
        title = {
            Row(modifier = Modifier
                .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = currentScreen.horizontalArrangement,
                ) {
                    FireblocksText(
                        text = titleText,
                        textStyle = FireblocksNCWDemoTheme.typography.h4,
                        textAlign = TextAlign.Center
                    )
                    if (labelText != null) {
                        Label(
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_small)),
                        text = labelText)
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            if (currentScreen.showSettingsButton) {
                SettingsButton(onSettingsClicked)
            } else if (currentScreen.showCloseButton) {
                CloseButton(onCloseClicked = onCloseClicked)
            } else if (currentScreen.showCloseWarningButton) {
                CloseButton(onCloseClicked = onCloseWarningClicked)
            } else {
                TopBarEmptySideBox()
            }
        },
        navigationIcon = {
            if (currentScreen.showNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            } else if (currentScreen.showLogo) {
                Box(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small_1))) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.logo_image_size))
                    )
                }
            } else {
                TopBarEmptySideBox()
            }
        },
    )
}

data class TopBarTitleData(var titleText: String? = null, var labelText: String? = null)

@Composable
private fun WalletScreenNavigationConfigurations(
    innerPadding: PaddingValues,
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    uiState: WalletUiState,
    dynamicTitleState: MutableState<TopBarTitleData>,
    onCloseClicked: () -> Unit = {},
    nfTsViewModel: NFTsViewModel,
    web3ViewModel: Web3ViewModel
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
                    viewModel = walletViewModel,
                    onSendClicked = {
                        walletViewModel.cleanBeforeNewFlow()
                        walletViewModel.onSendFlow(true)
                        walletViewModel.onSelectedAsset(it)
                        navController.navigate(WalletNavigationScreens.Amount.name)
                    },
                    onReceiveClicked = {
                        walletViewModel.onSendFlow(false)
                        walletViewModel.onSelectedAsset(it)
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
                TransferHistoryScreen(
                    walletViewModel = walletViewModel,
                    onNextScreen = {
                        walletViewModel.onTransactionSelected(it)
                        navController.navigate(WalletNavigationScreens.TransferDetails.name)
                    })
            }
        }
        addAdditionalScreens(
            screenModifier = screenModifier,
            navController = navController,
            walletViewModel = walletViewModel,
            dynamicTitleState = dynamicTitleState,
            nfTsViewModel = nfTsViewModel,
            web3ViewModel = web3ViewModel
        )

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
                    walletViewModel.onSelectedAsset(it)
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
                        walletViewModel.onAssetAmount(amount)
                        walletViewModel.onAssetUsdAmount(usdAmount)
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
                    walletViewModel.onSendDestinationAddress(it)
                    navController.navigate(WalletNavigationScreens.Fee.name)
                }
            }
        }
        composable(route = WalletNavigationScreens.Fee.name) {
            Box(modifier = screenModifier) {
                FeeScreen(
                    viewModel = walletViewModel,
                    onNextScreen = {
                        navController.navigate(WalletNavigationScreens.TransferApproval.name)
                    }
                )
            }
        }
        composable(route = WalletNavigationScreens.TransferApproval.name) {
            uiState.transactionWrapper?.let { transactionWrapper ->
                val titleData = TopBarTitleData()
                val assetName = transactionWrapper.assetName
                titleData.titleText = stringResource(id = R.string.transfer_top_bar_title, assetName)
                dynamicTitleState.value = titleData
            }
            TransferApprovalScreen(
                uiState = uiState,
                viewModel = walletViewModel,
                onNextScreen = { navController.navigate(WalletNavigationScreens.Sending.name) },
                onDiscard = onCloseClicked,
                bottomPadding = bottomPadding
            )
        }
        composable(route = WalletNavigationScreens.Sending.name) {
            uiState.transactionWrapper?.let { transactionWrapper ->
                val titleData = TopBarTitleData()
                val assetName = AssetsUtils.removeTestSuffix(transactionWrapper.assetName)
                titleData.titleText = stringResource(id = R.string.transfer_top_bar_title, assetName)
                dynamicTitleState.value = titleData
            }
            Box(modifier = screenModifier) {
                SendingScreen(
                    uiState = uiState,
                    onHomeClicked = onCloseClicked
                )
            }
        }
        composable(route = WalletNavigationScreens.TransferDetails.name) {
            uiState.transactionWrapper?.let { transactionWrapper ->
                transactionWrapper.assetId?.let { assetId ->
                    val asset = walletViewModel.getAsset(assetId)
                    asset?.let {
                        transactionWrapper.setAsset(asset)
                    }
                }
                uiState.selectedNFT?.let {
                    transactionWrapper.nftWrapper = it
                }
            }
            Box(modifier = screenModifier) {
                TransferDetailsScreen(
                    transactionWrapper = uiState.transactionWrapper,
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
    BottomNavigation(
        modifier = Modifier
            .windowInsetsPadding(insets = WindowInsets.navigationBars)
        ,
        backgroundColor = transparent,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.name } == true
            BottomNavigationItem(
                selectedContentColor = white,
                unselectedContentColor = text_secondary,
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = white,
//                    unselectedIconColor = text_secondary,
//                    indicatorColor = background,
//                ),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_small)),
                                    color = if (selected) grey_2 else transparent
                                ),
                            contentAlignment = Alignment.Center,
                        )
                        {
                            FireblocksText(
                                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small), vertical = dimensionResource(R.dimen.padding_small)),
                                text = stringResource(id = screen.bottomTitleResId),
                                textStyle = FireblocksNCWDemoTheme.typography.b2,
                                textColor = if (selected) Color.White else text_secondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
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