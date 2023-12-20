package com.fireblocks.sdkdemo.ui.screens

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.FullKeys
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.wallet.WalletScreen
import com.fireblocks.sdkdemo.ui.viewmodel.TakeoverViewModel
import com.google.gson.Gson

/**
 * Created by Fireblocks Ltd. on 10/08/2023.
 */
enum class FireblocksScreen(@StringRes val title: Int? = null) {
    Login,
    GenerateKeys(title = R.string.generate_keys_top_bar_title),
    GenerateKeysSuccess(title = R.string.generate_keys_success_top_bar_title),
    Wallet(title = R.string.wallet_top_bar_title),
    Settings,
    AdvancedInfo(title = R.string.advanced_info_bar_title),
    CreateBackup(title = R.string.create_key_backup),
    BackupSuccess(title = R.string.create_key_backup),
    RecoverWallet(title = R.string.recover_wallet_top_bar_title),
    ExportPrivateKey(title = R.string.export_private_key_bar_title),
    ExportPrivateKeyResult(title = R.string.export_private_key_bar_title),
    QRScannerScreen(title = R.string.scan_qr_bar_title),
}

private const val FULL_KEYS = "fullKeys"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FireblocksApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold()
    {
        MainScreenNavigationConfigurations(navController)
        navController.navigate(FireblocksScreen.Login.name)
    }
}

@Composable
private fun MainScreenNavigationConfigurations(navController: NavHostController) {
    val takeoverViewModel: TakeoverViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = FireblocksScreen.Login.name,
    ) {
        composable(route = FireblocksScreen.Login.name) {
            LoginScreen(
                onGenerateKeysScreen = { navController.navigate(FireblocksScreen.GenerateKeys.name) },
                onHomeScreen = { navController.navigate(FireblocksScreen.Wallet.name) }
            )
        }
        composable(route = FireblocksScreen.GenerateKeys.name) {
            GenerateKeysScreen(
                onSettingsClicked = { navController.navigate(FireblocksScreen.Settings.name) },
                onRecoverClicked = { navController.navigate(FireblocksScreen.RecoverWallet.name) },
                onSuccessScreen = { navController.navigate(FireblocksScreen.GenerateKeysSuccess.name) },
            )
        }
        composable(route = FireblocksScreen.GenerateKeysSuccess.name) {
            GenerateKeysSuccessScreen(
                onSettingsClicked = { navController.navigate(FireblocksScreen.Settings.name) },
                onCreateBackupScreen = { navController.navigate(FireblocksScreen.CreateBackup.name) },
                onHomeScreen = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
        composable(route = FireblocksScreen.Settings.name) {
            SettingsScreen(
                onClose = {
                    navController.popBackStack()
                },
                onSignOut = {
                    navController.navigate(FireblocksScreen.Login.name)
                },
                onAdvancedInfo = {
                    navController.navigate(FireblocksScreen.AdvancedInfo.name)
                },
                onCreateBackup = {
                    navController.navigate(FireblocksScreen.CreateBackup.name)
                },
                onRecoverWallet = {
                    navController.navigate(FireblocksScreen.RecoverWallet.name)
                },
                onExportPrivateKey = {
                    navController.navigate(FireblocksScreen.ExportPrivateKey.name)
                }
            )
        }
        composable(route = FireblocksScreen.ExportPrivateKey.name) {
            ExportPrivateKeyScreen(
                viewModel = takeoverViewModel,
                onBackClicked = { navController.popBackStack() },
                onTakeoverSuccess = {
                    val fullKeysJson = Gson().toJson(FullKeys(it), FullKeys::class.java)
                    navController.navigate("${FireblocksScreen.ExportPrivateKeyResult.name}/${fullKeysJson}")
                }
            )
        }
        composable(route = "${FireblocksScreen.ExportPrivateKeyResult.name}/{$FULL_KEYS}") { backStackEntry ->
            val fullKeysJson = backStackEntry.arguments?.getString(FULL_KEYS, "")
            val fullKeys = Gson().fromJson(fullKeysJson, FullKeys::class.java)
            ExportPrivateKeyResultScreen(
                viewModel = takeoverViewModel,
                takeoverResult = fullKeys.fullKeys,
                onBackClicked = { navController.popBackStack(FireblocksScreen.Settings.name, inclusive = false) },
            )
        }
        composable(route = FireblocksScreen.AdvancedInfo.name) {
            AdvancedInfoScreen(
                onBackClicked = { navController.navigateUp() },
            )
        }
        composable(route = FireblocksScreen.CreateBackup.name) {
            CreateKeyBackupScreen(
                onBackClicked = { navController.popBackStack() }
            ) {
                navController.navigate(FireblocksScreen.BackupSuccess.name)
            }
        }
        composable(route = FireblocksScreen.BackupSuccess.name) {
            BackupSuccessScreen(
                onBackClicked = { navController.navigateUp() },
                onHomeClicked = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
        composable(
            route = FireblocksScreen.Wallet.name,
        ) { backStackEntry ->
            WalletScreen {
                backStackEntry.arguments?.clear()
                navController.navigate(FireblocksScreen.Settings.name)
            }
            backStackEntry.arguments?.clear()
        }
        composable(route = FireblocksScreen.RecoverWallet.name) {
            RecoverWalletScreen(
                onBackClicked = { navController.popBackStack() },
                onRecoverSuccess = {
                    navController.navigate(FireblocksScreen.Wallet.name)
                }
            )
        }
    }
}

@Preview
@Composable
fun FireblocksAppPreview() {
    FireblocksNCWDemoTheme {
        FireblocksApp()
    }
}
