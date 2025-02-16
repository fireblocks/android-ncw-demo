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
}