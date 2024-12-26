package com.fireblocks.sdkdemo.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.storage.models.FullKeys
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.screens.adddevice.AddDeviceCanceledScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.AddDeviceDetailsScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.AddDeviceErrorScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.AddDeviceScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.AddDeviceSuccessScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.JoinWalletQRScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.JoinWalletScreen
import com.fireblocks.sdkdemo.ui.screens.adddevice.JoinWalletSuccessScreen
import com.fireblocks.sdkdemo.ui.screens.wallet.WalletScreen
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.LoginViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.TakeoverViewModel
import com.google.gson.Gson

/**
 * Created by Fireblocks Ltd. on 10/08/2023.
 */
enum class FireblocksScreen(@StringRes val title: Int? = null,
                            val showCloseButton: Boolean = false) {
    SplashScreen(title = R.string.splash_screen_title),
    SocialLogin(title = R.string.splash_screen_title),
    ExistingAccount(title = R.string.existing_account_top_bar_title, showCloseButton = true),
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
    AddDevice(title = R.string.add_new_device_bar_title),
    AddDeviceDetails(title = R.string.add_new_device_bar_title),
    AddDeviceSuccess,
    AddDeviceCanceled,
    AddDeviceError(showCloseButton = true),
    JoinWallet(showCloseButton = true),
    JoinWalletQRScreen(title = R.string.add_new_device_bar_title, showCloseButton = true),
    JoinWalletSuccess,
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
        navController.navigate(getStartDestinationScreenName())
    }
}

fun getStartDestinationScreenName(): String {
    return if (MultiDeviceManager.instance.isSplashScreenSeen()) {
        FireblocksScreen.SocialLogin.name
    } else {
        FireblocksScreen.SplashScreen.name
    }
}

@Composable
private fun MainScreenNavigationConfigurations(navController: NavHostController) {
    val takeoverViewModel: TakeoverViewModel = viewModel()
    val addDeviceViewModel: AddDeviceViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = getStartDestinationScreenName(),
    ) {
        composable(route = FireblocksScreen.SplashScreen.name) {
            SplashScreen(
                onNextScreen = { navController.navigate(FireblocksScreen.SocialLogin.name) }
            )
        }
        composable(route = FireblocksScreen.SocialLogin.name) {
            SocialLoginScreen(
                viewModel = loginViewModel,
                onSettingsClicked = { navController.navigate(FireblocksScreen.Settings.name) },
                onGenerateKeysScreen = { navController.navigate(FireblocksScreen.GenerateKeys.name) },
                onExistingAccountScreen = { navController.navigate(FireblocksScreen.ExistingAccount.name) },
                onHomeScreen = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
        composable(route = FireblocksScreen.ExistingAccount.name) {
            ExistingAccountScreen(
                viewModel = loginViewModel,
                onRecoverClicked = { navController.navigate(FireblocksScreen.RecoverWallet.name) },
                onJoinWalletScreen = { navController.navigate(FireblocksScreen.JoinWallet.name) },
            ) {
                signOut(context, navController)
            }
        }
        composable(route = FireblocksScreen.GenerateKeys.name) {
            GenerateKeysScreen(
                onSettingsClicked = { navController.navigate(FireblocksScreen.Settings.name) },
            ) { navController.navigate(FireblocksScreen.GenerateKeysSuccess.name) }
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
                    navController.navigate(FireblocksScreen.SplashScreen.name)
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
                },
                onAddNewDevice = {
                    navController.navigate(FireblocksScreen.AddDevice.name)
                },
                onGenerateKeys = {
                    navController.navigate(FireblocksScreen.GenerateKeys.name)
                },
                onDeleteAndCreateNewWallet = {
                    loginViewModel.setLoginFlow(LoginViewModel.LoginFlow.DELETE_AND_CREATE_NEW_WALLET)
                    loginViewModel.onPassedLogin(false)
                    navController.navigate(FireblocksScreen.SocialLogin.name)
                },
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
        composable(route = FireblocksScreen.AddDevice.name) {
            AddDeviceScreen(
                viewModel = addDeviceViewModel,
                onBackClicked = { navController.popBackStack() },
                onNextScreen = {
                     navController.navigate(FireblocksScreen.AddDeviceDetails.name)
                }
            )
        }
        composable(route = FireblocksScreen.AddDeviceDetails.name) {
            AddDeviceDetailsScreen(
                viewModel = addDeviceViewModel,
                onBackClicked = { navController.popBackStack(FireblocksScreen.AddDevice.name, inclusive = false) },
                onAddDeviceSuccess = {
                     navController.navigate(FireblocksScreen.AddDeviceSuccess.name)
                },
                onAddDeviceCanceled = {
                    navController.navigate(FireblocksScreen.AddDeviceCanceled.name)
                },
                onExpired = {
                    navController.navigate(FireblocksScreen.AddDeviceError.name)
                },
            )
        }
        composable(route = FireblocksScreen.AddDeviceSuccess.name) {
            AddDeviceSuccessScreen(
                viewModel = addDeviceViewModel,
                onHomeClicked = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
        composable(route = FireblocksScreen.AddDeviceError.name) {
            AddDeviceErrorScreen(
                viewModel = addDeviceViewModel,
                onBackToAddDevice = { navController.popBackStack(FireblocksScreen.AddDevice.name, inclusive = false) },
                onBackToJoinWallet = { navController.popBackStack(FireblocksScreen.JoinWallet.name, inclusive = false) },
                onCloseJoinWallet = {
                    signOut(context, navController)
                },
                onCloseAddDevice = { navController.navigate(FireblocksScreen.AddDeviceCanceled.name) }
            )
        }
        composable(route = FireblocksScreen.AddDeviceCanceled.name) {
            AddDeviceCanceledScreen(
                onHomeClicked = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
        composable(route = FireblocksScreen.JoinWallet.name) {
            JoinWalletScreen(
                viewModel = addDeviceViewModel,
                onCloseClicked = {
                    signOut(context, navController)
                },
                onNextScreen = {
                    navController.navigate(FireblocksScreen.JoinWalletQRScreen.name)
                }
            )
        }
        composable(route = FireblocksScreen.JoinWalletQRScreen.name) {
            JoinWalletQRScreen(
                viewModel = addDeviceViewModel,
                onBackClicked = { navController.popBackStack() },
                onCloseClicked = {
                    signOut(context, navController)
                },
                onNextScreen = {
                    navController.navigate(FireblocksScreen.JoinWalletSuccess.name)
                },
                onError = {
                    navController.navigate(FireblocksScreen.AddDeviceError.name)
                },
            )
        }
        composable(route = FireblocksScreen.JoinWalletSuccess.name) {
            JoinWalletSuccessScreen(
                onHomeClicked = { navController.navigate(FireblocksScreen.Wallet.name) },
            )
        }
    }
}

private fun signOut(context: Context, navController: NavHostController) {
    SignInUtil.getInstance().signOut(context) {
        navController.navigate(FireblocksScreen.SplashScreen.name)
    }
}

@Preview
@Composable
fun FireblocksAppPreview() {
    FireblocksNCWDemoTheme {
        FireblocksApp()
    }
}
