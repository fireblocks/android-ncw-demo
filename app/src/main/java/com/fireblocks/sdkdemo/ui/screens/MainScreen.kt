package com.fireblocks.sdkdemo.ui.screens

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.FullKeys
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.wallet.WalletScreen
import com.google.gson.Gson
import java.util.Base64

enum class FireblocksScreen(@StringRes val title: Int? = null) {
    Login,
    GenerateKeys(title = R.string.generate_keys_top_bar_title),
    Wallet(title = R.string.wallet_top_bar_title),
    Settings,
    AdvancedInfo(title = R.string.advanced_info_bar_title),
    CreateBackup(title = R.string.create_key_backup),
    CopyLocally(title = R.string.create_key_backup),
    BackupSuccess(title = R.string.create_key_backup),
    AlreadyBackedUp(title = R.string.create_key_backup),
    RecoverWallet(title = R.string.recover_wallet_top_bar_title),
    RecoverWalletFromSavedKeyScreen(title = R.string.recover_wallet_top_bar_title),
    ExportPrivateKey(title = R.string.export_private_key_bar_title),
    ExportPrivateKeyResult(title = R.string.export_private_key_bar_title),
}

private const val AFTER_RECOVER = "afterRecover"
private const val PASSPHRASE = "passphrase"
private const val LAST_BACKUP_DATE = "lastBackupDate"
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
    NavHost(
        navController = navController,
        startDestination = FireblocksScreen.Login.name,
    ) {
        composable(route = FireblocksScreen.Login.name) {
            LoginScreen(
                onNextScreen = { navController.navigate(FireblocksScreen.GenerateKeys.name) },
                onHomeScreen = { navController.navigate(FireblocksScreen.Wallet.name) }
            )
        }
        composable(route = FireblocksScreen.GenerateKeys.name) {
            GenerateKeysScreen(
                onSettingsClicked = { navController.navigate(FireblocksScreen.Settings.name) },
                onRecoverClicked = { navController.navigate(FireblocksScreen.RecoverWallet.name) },
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
                onBackClicked = { navController.popBackStack() },
                showAlreadyBackedUp = { lastBackupDate ->
                    val encodedDate = Base64.getUrlEncoder().encodeToString(lastBackupDate?.toByteArray())
                    navController.navigate("${FireblocksScreen.AlreadyBackedUp.name}/${encodedDate}")
                },
                onBackupSuccess = { backupKeysUiState ->
                    when (backupKeysUiState.showCopyLocallyScreen) {
                        true -> navController.navigate("${FireblocksScreen.CopyLocally.name}/${backupKeysUiState.passphrase}")
                        false -> navController.navigate(FireblocksScreen.BackupSuccess.name)
                    }
                }
            )
        }
        composable(route = "${FireblocksScreen.CopyLocally.name}/{$PASSPHRASE}") { backStackEntry ->
            val passphrase = backStackEntry.arguments?.getString(PASSPHRASE, "")
            CopyLocallyScreen(
                passphrase = passphrase,
                onBackClicked = { navController.popBackStack(FireblocksScreen.Settings.name, inclusive = false) },
            )
        }
        composable(route = "${FireblocksScreen.AlreadyBackedUp.name}/{$LAST_BACKUP_DATE}") { backStackEntry ->
            val encodedDate = backStackEntry.arguments?.getString(LAST_BACKUP_DATE, "")
            val lastBackupDate = String(Base64.getUrlDecoder().decode(encodedDate))
            AlreadyBackedUpScreen(
                lastBackupDate = lastBackupDate,
                onBackClicked = { navController.popBackStack() },
                onBackupSuccess = {
                    navController.navigate(FireblocksScreen.BackupSuccess.name)
                }
            )
        }
        composable(route = FireblocksScreen.BackupSuccess.name) {
            BackupSuccessScreen(
                onBackClicked = { navController.popBackStack(FireblocksScreen.Settings.name, inclusive = false) },
                onHomeClicked = { navController.popBackStack(FireblocksScreen.Wallet.name, inclusive = false) },
            )
        }
        composable(
            route = "${FireblocksScreen.Wallet.name}?$AFTER_RECOVER={$AFTER_RECOVER}",
            arguments = listOf(
                navArgument(AFTER_RECOVER) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val afterRecover = backStackEntry.arguments?.getBoolean(AFTER_RECOVER) ?: false
            WalletScreen(
                onSettingsClicked = {
                    navController.navigate(FireblocksScreen.Settings.name)
                },
                afterRecover = afterRecover,
            )
        }
        composable(route = FireblocksScreen.RecoverWallet.name) {
            RecoverWalletScreen(
                onBackClicked = { navController.popBackStack() },
                onShowRecoverFromSavedKey = { navController.navigate(FireblocksScreen.RecoverWalletFromSavedKeyScreen.name) },
                onRecoverSuccess = {
                    val booleanValue = true
                    navController.navigate("${FireblocksScreen.Wallet.name}?$AFTER_RECOVER=${booleanValue}")
                }
            )
        }
        composable(route = FireblocksScreen.RecoverWalletFromSavedKeyScreen.name) {
            RecoverWalletFromSavedKeyScreen(
                onBackClicked = { navController.popBackStack(FireblocksScreen.Settings.name, inclusive = false) },
                onRecoverSuccess = {
                    val booleanValue = true
                    navController.navigate("${FireblocksScreen.Wallet.name}?$AFTER_RECOVER=${booleanValue}")
                },
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
