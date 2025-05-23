package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.fireblocks.sdkdemo.ui.screens.wallet.TopBarTitleData
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel

fun NavGraphBuilder.addAdditionalScreens(
    screenModifier: Modifier,
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    dynamicTitleState: MutableState<TopBarTitleData>,
    nfTsViewModel: NFTsViewModel,
    web3ViewModel: Web3ViewModel,
) {}