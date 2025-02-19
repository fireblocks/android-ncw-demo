package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fireblocks.sdkdemo.ui.screens.wallet.WalletNavigationScreens
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel


fun NavGraphBuilder.addAdditionalScreens(
    screenModifier: Modifier,
    navController: NavHostController,
    nfTsViewModel: NFTsViewModel,
    web3ViewModel: Web3ViewModel
) {
    composable(route = WalletNavigationScreens.BottomNFTs.name) {
        Box(modifier = screenModifier) {
            NFTsScreen()
            { selectedNFT ->
                nfTsViewModel.onNFTSelected(selectedNFT)
                navController.navigate(WalletNavigationScreens.NFT.name)
            }
        }
    }
    composable(route = WalletNavigationScreens.NFT.name) {
        val selectedNFT = nfTsViewModel.getSelectedNFT()
        Box(modifier = screenModifier) {
            NFTScreen(nft = selectedNFT)
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
                    navController.navigate(WalletNavigationScreens.Web3ConnectionCreate.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3.name) {
        val selectedWeb3Connection = web3ViewModel.getSelectedWeb3Connection()
        Box(modifier = screenModifier) {
            Web3Screen(
                viewModel = web3ViewModel,
                web3Connection = selectedWeb3Connection,
                onWeb3ConnectionRemoved = {
                    navController.navigate(WalletNavigationScreens.BottomWeb3.name)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3ConnectionCreate.name) {
        Box(modifier = screenModifier) {
            Web3ConnectionCreateScreen(
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
                onApproved = { web3Connection ->
                    web3ViewModel.onWeb3ConnectionSelected(web3Connection)
                    navController.navigate(WalletNavigationScreens.Web3Approved.name)
                },
                onDenied = {
                    navController.popBackStack(WalletNavigationScreens.BottomWeb3.name, inclusive = false)
                }
            )
        }
    }
    composable(route = WalletNavigationScreens.Web3Approved.name) {
        val selectedWeb3Connection = web3ViewModel.getSelectedWeb3Connection()
        Box(modifier = screenModifier) {
            Web3Screen(
                viewModel = web3ViewModel,
                web3Connection = selectedWeb3Connection,
            )
        }
    }
}