package com.fireblocks.sdkdemo.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ColoredButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_3
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white_alpha_50
import com.fireblocks.sdkdemo.ui.viewmodel.SelectAssetViewModel

/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun SelectAssetScreen(
    modifier: Modifier = Modifier,
    viewModel: SelectAssetViewModel = viewModel(),
    onHomeScreen: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()

    val searchText by viewModel.searchText.collectAsState() //TODO put it in the uiState
    val assets by viewModel.filteredAssets.collectAsState()

    val continueEnabledState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var mainModifier = modifier.fillMaxWidth()
    val showProgress = userFlow is UiState.Loading
    if (showProgress) { //TODO handle also alpha color for the toolbar when loading
        mainModifier = modifier
            .fillMaxWidth()
            .alpha(floatResource(R.dimen.progress_alpha))
            .clickable(
                indication = null, // disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    }

    var selectedAssetId: String by remember { mutableStateOf("") }
    val onItemClick = { assetId: String -> selectedAssetId = assetId} //TODO fix bug here. perhaps use only asset id instead of index
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_default))
            .imePadding(),
    ) {
        Column(
            modifier = mainModifier,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {

                val interactionSource = remember { MutableInteractionSource() }
                // use search_bar_height instead of 32.dp

                BasicTextField(
                    modifier = mainModifier
                    .height(dimensionResource(id = R.dimen.search_bar_height))
                        .defaultMinSize(minHeight = 0.dp)
                    ,
                    value = searchText,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    onValueChange = { viewModel.onSearchTextChange(it) },
                    singleLine = true,
                    interactionSource = interactionSource
                ) { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = searchText,
                        placeholder = {
                            FireblocksText(
                                text = stringResource(id = R.string.search_asset),
                                textStyle = FireblocksNCWDemoTheme.typography.b2,
                                textColor = white_alpha_50)
                        },
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = white_alpha_50
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotNullAndNotEmpty()) {
                                Icon(
                                    modifier = Modifier.clickable {
                                        viewModel.onSearchTextChange("")
                                        focusManager.clearFocus()
                                    },
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Icon",
                                    tint = white_alpha_50
                                )
                            }
                        },
                        contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                            top = 0.dp,
                            bottom = 0.dp
                        ),
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_1)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = grey_3,
                            unfocusedContainerColor = grey_3,
                            disabledContainerColor = grey_3,
                            focusedTextColor = white_alpha_50,
                            cursorColor = Color.Red,
                            focusedIndicatorColor = transparent,
                            unfocusedIndicatorColor = transparent,
                            disabledIndicatorColor = transparent,
                        ),
                    )
                }


//                TextField(
//                    modifier = mainModifier,
//                    value = searchText,
//                    onValueChange = { viewModel.onSearchTextChange(it) },
//                    placeholder = {
//                        FireblocksText(
//                            text = stringResource(id = R.string.search_asset),
//                            textStyle = FireblocksNCWDemoTheme.typography.b2,
//                            textColor = white_alpha_50)
//                    },
//                    leadingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Search,
//                            contentDescription = "Search Icon",
//                            tint = white_alpha_50
//                        )
//                    },
//                    trailingIcon = {
//                        if (searchText.isNotNullAndNotEmpty()) {
//                            Icon(
//                                modifier = Modifier.clickable {
//                                    viewModel.onSearchTextChange("")
//                                    focusManager.clearFocus()
//                                },
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Close Icon",
//                                tint = white_alpha_50
//                            )
//                        }
//                    },
//                    textStyle = FireblocksNCWDemoTheme.typography.b1,
//                    singleLine = false,
//                    shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_1)),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedContainerColor = grey_3,
//                        unfocusedContainerColor = grey_3,
//                        disabledContainerColor = grey_3,
//                        focusedBorderColor = transparent,
//                        unfocusedBorderColor = transparent,
//                        focusedTextColor = white_alpha_50,
//                    ),
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(
//                        onDone = {
//                            focusManager.clearFocus()
//                        }
//                    ),
//                )
                //TODO fix keyboard overlapping when list is long
                LazyColumn(
                    modifier = mainModifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small_1))) {
                    assets.forEachIndexed { index, supportedAsset ->
                        item {
                            AssetListItem(
                                modifier = Modifier
                                    .selectable(
                                        selected = selectedAssetId == supportedAsset.id,
                                        onClick = { }
                                    )
                                    .background(
                                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.padding_default)),
                                        color = if (selectedAssetId == supportedAsset.id) grey_2 else transparent),
                                supportedAsset = supportedAsset,
                                onClick = {
                                    continueEnabledState.value = true
                                    onItemClick(it.id)
                                }
                            )
                        }
                    }
                }
            }
            //Footer
            Column(
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                if (userFlow is UiState.Error) {
                    ErrorView(message = stringResource(id = R.string.add_asset_error))
                }
                ColoredButton(
                    modifier = mainModifier,
                    labelResourceId = R.string.add_asset,
                    imageResourceId = R.drawable.ic_plus,
                    onClick = {
//                        val assetId = uiState.assets[selectedIndex].id
                        focusManager.clearFocus()
                        val assetId = selectedAssetId
                        viewModel.addAssetToWallet(context, assetId)
                    },
                    enabledState = continueEnabledState,
                )
            }
        }

        if (showProgress) {
            ProgressBar()
        }

        LaunchedEffect(key1 = uiState.assetAddedToWallet) {
            if (uiState.assetAddedToWallet) {
                onHomeScreen()
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event){
            Lifecycle.Event.ON_CREATE -> {
                val noAssets = (uiState.assets.isEmpty())
                val state = if (noAssets) UiState.Loading else UiState.Idle
                viewModel.loadAssets(context, state)
            }
            else -> {}
        }
    }
}

@Preview
@Composable
fun SelectAssetScreenPreview() {
    val assets = arrayListOf<SupportedAsset>()
    assets.add(SupportedAsset(id = "ETH",
        symbol = "ETH",
        name = "Ethereum",
        type = "BASE_ASSET",
        blockchain = "Ethereum"))
    assets.add(SupportedAsset(id = "SOL",
        symbol = "SOL",
        name = "Solana",
        type = "BASE_ASSET",
        blockchain = "Solana"))
    assets.add(SupportedAsset(id = "BTC",
        symbol = "BTC",
        name = "Bitcoin",
        type = "BASE_ASSET",
        blockchain = "Bitcoin"))
    assets.add(SupportedAsset(id = "ADA", symbol = "ADA", name = "Cardano", type = "BASE_ASSET", blockchain = "Cardano"))
    assets.add(SupportedAsset(id = "AVAX", symbol = "AVAX", name = "Avalanche", type = "BASE_ASSET", blockchain = "Avalanche"))
    assets.add(SupportedAsset(id = "MATIC", symbol = "MATIC", name = "Polygon", type = "BASE_ASSET", blockchain = "Polygon"))
    assets.add(SupportedAsset(id = "USDT", symbol = "USDT", name = "Tether", type = "BASE_ASSET", blockchain = "Ethereum"))

    val viewModel = SelectAssetViewModel()
    viewModel.onAssets(assets)
//    viewModel.uiState.value = SelectAssetViewModel.SelectAssetUiState(assets = assets)

    FireblocksNCWDemoTheme {
        Surface {
            SelectAssetScreen(viewModel = viewModel, onHomeScreen = {})
        }
    }
}