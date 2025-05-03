package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fireblocks.sdk.ew.models.Asset
import com.fireblocks.sdkdemo.bl.core.extensions.getBlockchainDisplayName
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.screens.wallet.TopBarTitleData
import com.fireblocks.sdkdemo.ui.screens.wallet.WalletNavigationScreens
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel


fun NavGraphBuilder.addAdditionalScreens(
    screenModifier: Modifier,
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    dynamicTitleState: MutableState<TopBarTitleData>,
    nfTsViewModel: NFTsViewModel,
    web3ViewModel: Web3ViewModel
) {
    composable(route = WalletNavigationScreens.BottomNFTs.name) {
        Box(modifier = screenModifier) {
            NFTsScreen()
            { selectedNFT ->
                nfTsViewModel.onNFTSelected(selectedNFT)
                navController.navigate(WalletNavigationScreens.NFTDetails.name)
            }
        }
    }
    composable(route = WalletNavigationScreens.NFTDetails.name) {
        val selectedNFT = nfTsViewModel.getSelectedNFT()
        Box(modifier = screenModifier) {
            NFTDetailsScreen(
                nft = selectedNFT,
                onTransferNFTClicked = {
                    walletViewModel.cleanBeforeNewFlow()
                    navController.navigate(WalletNavigationScreens.NFTReceivingAddress.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.NFTReceivingAddress.name) {
        val context = LocalContext.current
        Box(modifier = screenModifier) {
            NFTReceivingAddressScreen(
                viewModel = nfTsViewModel,
                onNextScreen = { address ->
                    val selectedNFT = nfTsViewModel.getSelectedNFT()
                    walletViewModel.apply {
                        onAssetAmount("1")
                        onSendDestinationAddress(address)
                        onSelectedNFT(
                            NFTWrapper(
                                id = selectedNFT?.id,
                                name = selectedNFT?.name,
                                collectionName = selectedNFT?.collection?.name,
                                iconUrl = selectedNFT?.media?.firstOrNull()?.url,
                                blockchain = selectedNFT?.getBlockchainDisplayName(context),
                                blockchainSymbol = selectedNFT?.blockchainDescriptor?.name,
                                balance = "1",
                                standard = selectedNFT?.standard
                            )
                        )
                        onSelectedAsset(SupportedAsset(Asset(id = selectedNFT?.id, symbol = selectedNFT?.blockchainDescriptor?.name)))
                    }
                    navController.navigate(WalletNavigationScreens.NFTFeeScreen.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.NFTFeeScreen.name) {
        Box(modifier = screenModifier) {
            NFTFeeScreen(
                viewModel = walletViewModel,
                onNextScreen = {
                    navController.navigate(WalletNavigationScreens.TransferApproval.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.BottomWeb3.name) {
        Box(modifier = screenModifier) {
            Web3ConnectionsScreen(
                onWeb3ConnectionClicked = { web3Connection ->
                    web3ViewModel.onWeb3ConnectionSelected(web3Connection)
                    navController.navigate(WalletNavigationScreens.Web3.name)
                },
                onAddConnectionClicked = {
                    navController.navigate(WalletNavigationScreens.Web3ConnectionReceivingAddress.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3.name) {
        Box(modifier = screenModifier) {
            Web3Screen(
                viewModel = web3ViewModel,
                onWeb3ConnectionRemoved = {
                    navController.navigate(WalletNavigationScreens.BottomWeb3.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3ConnectionReceivingAddress.name) {
        web3ViewModel.resetUserFlow()
        Box(modifier = screenModifier) {
            Web3ConnectionReceivingAddressScreen(
                viewModel = web3ViewModel,
                onNextScreen = {
                    navController.navigate(WalletNavigationScreens.Web3ConnectionPreview.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3ConnectionPreview.name) {
        Box(modifier = screenModifier) {
            Web3ConnectionPreviewScreen(
                viewModel = web3ViewModel,
                onApproved = {
                    navController.navigate(WalletNavigationScreens.BottomWeb3.name)
                },
                onDenied = {
                    navController.popBackStack(WalletNavigationScreens.BottomWeb3.name, inclusive = false)
                }
            )
        }
    }
}