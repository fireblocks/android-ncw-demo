package com.fireblocks.sdkdemo.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.FullKey
import com.fireblocks.sdk.keys.KeyData
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.CryptoIcon
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TogglePassword
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.viewmodel.TakeoverViewModel

/**
 * Created by Fireblocks Ltd. on 10/08/2023.
 */
@Composable
fun ExportPrivateKeyResultScreen(
    viewModel: TakeoverViewModel,
    onBackClicked: () -> Unit,
    takeoverResult: Set<FullKey>, ) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    var mainModifier = Modifier
        .fillMaxSize()
        .padding(horizontal = dimensionResource(R.dimen.padding_default))
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.padding_default))
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
        topBarModifier = Modifier
            .alpha(progressAlpha)
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.ExportPrivateKeyResult,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                val assets = uiState.assets
                if (!showProgress && assets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.padding_extra_large_2)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_missing_asset),
                            contentDescription = null,
                            modifier = Modifier.width(300.dp)
                        )
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_extra_large)),
                            text = stringResource(id = R.string.missing_assets_in_wallet),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))) {
                        takeoverResult.forEach { fullKey ->
                            fullKey.privateKey?.let { privateKey ->
                                val algorithmName = fullKey.algorithm?.name ?: ""
                                item {
                                    FireblocksText(
                                        text = algorithmName,
                                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                                        maxLines = 1,
                                    )
                                }
                                item {
                                    val resId = when (fullKey.algorithm) {
                                        Algorithm.MPC_ECDSA_SECP256K1 -> R.string.xprv_title
                                        Algorithm.MPC_EDDSA_ED25519 -> R.string.fprv_title
                                        null -> R.string.xprv_title
                                    }
                                    DerivedAssetListItem(derivedKey = privateKey, title = stringResource(id = resId))
                                }
                                assets.forEach { asset ->
                                    if (asset.algorithm == fullKey.algorithm && asset.algorithm == Algorithm.MPC_ECDSA_SECP256K1) {
                                        item {
                                            DerivedAssetListItem(supportedAsset = asset)
                                        }
                                    }
                                }
                                item {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_default)),
                                        color = grey_2,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showProgress) {
                ProgressBar()
            }
            if (userFlow is UiState.Error) {
                ErrorView(errorState = userFlow as UiState.Error, defaultResId = R.string.takeover_error)
            }
        }

        OnLifecycleEvent { _, event ->
            when (event){
                Lifecycle.Event.ON_CREATE -> {
                    viewModel.loadAssets(context, takeoverResult)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DerivedAssetListItem(modifier: Modifier = Modifier, supportedAsset: SupportedAsset) {
    DerivedAssetListItem(modifier = modifier, supportedAsset = supportedAsset, derivedKey = supportedAsset.derivedAssetKey?.data ?: "", title = supportedAsset.name)
}

@Composable
fun DerivedAssetListItem(modifier: Modifier = Modifier, supportedAsset: SupportedAsset? = null, derivedKey: String, title: String) {
    val context = LocalContext.current
    val revealKeyState: MutableState<Boolean> = remember {
        mutableStateOf(false) // To reveal the key with toggle
    }
    val keyDataState = remember {
        mutableStateOf(derivedKey)
    }
    Column(
        modifier = modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
            .padding(dimensionResource(id = R.dimen.padding_default)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            supportedAsset?.let {
                Card(
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small)),
                    colors = CardDefaults.cardColors(containerColor = transparent),
                ) {
                    CryptoIcon(
                        iconUrl = supportedAsset.iconUrl,
                        symbol = supportedAsset.symbol,
                        imageSizeResId = R.dimen.image_size_small,
                        paddingResId = R.dimen.padding_extra_small,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                FireblocksText(
                    text = title,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    maxLines = 1,
                )
            }
            Row(horizontalArrangement = Arrangement.End) {
                Image(modifier = Modifier
//                    .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
                    .clickable { copyToClipboard(context, derivedKey) },
                    painter = painterResource(id =  R.drawable.ic_copy_button), contentDescription = null)
            }
        }

        val privateKeyDesc = stringResource(id = R.string.private_key_value_desc, title)
        TogglePassword(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small_2))
                .semantics { contentDescription = privateKeyDesc },
            readOnly = true,
            password = keyDataState,
            showRevealIcon = true,
            revealPassword = revealKeyState,
        )
        supportedAsset?.let {
            val wif = supportedAsset.wif ?: ""
            WifView(wif, context, supportedAsset)
        }
    }
}

@Composable
private fun WifView(wif: String,
                    context: Context,
                    supportedAsset: SupportedAsset) {
    if (wif.isNotNullAndNotEmpty()) {
        val revealWifState: MutableState<Boolean> = remember {
            mutableStateOf(false) // To reveal the key with toggle
        }
        val keyDataWifState = remember {
            mutableStateOf(wif)
        }
        Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small_2)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.padding_small))) {
                FireblocksText(
                    text = stringResource(id = R.string.wif),
                    textStyle = FireblocksNCWDemoTheme.typography.b3,
                    maxLines = 1,
                )
            }
            Row(horizontalArrangement = Arrangement.End) {
                Image(modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_default))
                    .clickable { copyToClipboard(context, wif) },
                    painter = painterResource(id = R.drawable.ic_copy), contentDescription = null)
            }
        }
        WifSection(supportedAsset, revealWifState, keyDataWifState)
    }
}

@Composable
fun WifSection(supportedAsset: SupportedAsset, revealKeyState: MutableState<Boolean>, keyDataWifState: MutableState<String>) {
    val privateKeyWifDesc = stringResource(id = R.string.private_key_wif_value_desc, supportedAsset.name)
    TogglePassword(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_small_2))
            .semantics { contentDescription = privateKeyWifDesc },
        readOnly = true,
        password = keyDataWifState,
        showRevealIcon = true,
        revealPassword = revealKeyState,
    )
}

@Preview
@Composable
fun WifViewPreview(){
    val context = LocalContext.current
    val wif = "p2wpkh:KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qY8zgY9Y9f3b8n"
    val supportedAsset = SupportedAsset(
        id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93",
        derivedAssetKey = KeyData(data = "9s21ZrQH143K2zPNSbKDKusTNW4XVwvTCCEFvcLkeNyauqJJd9UjZg3AtgeVAEs84BZtyBdnFom3VqrvAQbzE1j9XKJ3uNvxyL1kJZP49cE"),
        wif = wif
    )
    FireblocksNCWDemoTheme {
        Surface {
            Column(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
                    .padding(dimensionResource(id = R.dimen.padding_default)),
            ) {
                WifView(wif, context, supportedAsset)
            }
        }
    }
}

@Preview
@Composable
fun ExportPrivateKeyResultScreenPreview() {
    val viewModel =  TakeoverViewModel()
    val fullKeys = hashSetOf(FullKey(keyId = "123", privateKey = "xprv9s21ZrQH143K2zPNSbKDKusTNW4XVwvTCCEFvcLkeNyauqJJd9UjZg3AtgeVAEs84BZtyBdnFom3VqrvAQbzE1j9XKJ3uNvxyL1kJZP49cE"))
    viewModel.onTakeoverResult(fullKeys)
    val assets = arrayListOf<SupportedAsset>()
    val derivedAssetKey = KeyData(data = "9s21ZrQH143K2zPNSbKDKusTNW4XVwvTCCEFvcLkeNyauqJJd9UjZg3AtgeVAEs84BZtyBdnFom3VqrvAQbzE1j9XKJ3uNvxyL1kJZP49cE")
    assets.add(SupportedAsset(
        id = "BTC_TEST",
        symbol = "BTC_TEST",
        name = "Bitcoin Test",
        type = "BASE_ASSET",
        blockchain = "BTC_TEST",
        balance = "0.00001",
        price = "0.29",
        derivedAssetKey = derivedAssetKey
    ))
    assets.add(SupportedAsset(
        id = "ETH_TEST3",
        symbol = "ETH_TEST3",
        name = "Ethereum Test (Goerli)",
        decimals = 18,
        type = "BASE_ASSET",
        blockchain = "ETH_TEST3",
        balance = "132.4",
        price = "2,825.04",
        derivedAssetKey = derivedAssetKey
    ),
    )
    assets.add(SupportedAsset(
        id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum",
        balance = "132.4",
        price = "2,825.04",
        derivedAssetKey = derivedAssetKey
        ))
    assets.add(SupportedAsset(id = "SOL",
        symbol = "SOL",
        name = "Solana",
        type = "BASE_ASSET",
        blockchain = "Solana",
        balance = "217",
        price = "1,336.72",
        derivedAssetKey = derivedAssetKey
        ))
    assets.add(SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin",
        balance = "2.48",
        price = "41,044.93",
        derivedAssetKey = derivedAssetKey,
        wif = "p2wpkh:KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qY8zgY9Y9f3b8n")
    )
    val takeoverViewModel = TakeoverViewModel()
    takeoverViewModel.onAssets(assets)
    takeoverViewModel.updateUserFlow(UiState.Idle)

    FireblocksNCWDemoTheme {
        ExportPrivateKeyResultScreen(
            onBackClicked = {},
            takeoverResult = fullKeys,
            viewModel = takeoverViewModel,
        )
    }
}