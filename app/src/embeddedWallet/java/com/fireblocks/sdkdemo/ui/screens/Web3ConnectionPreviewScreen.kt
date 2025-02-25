package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.ew.models.CreateWeb3ConnectionResponse
import com.fireblocks.sdk.ew.models.RespondToConnectionRequest
import com.fireblocks.sdk.ew.models.Web3Connection
import com.fireblocks.sdk.ew.models.Web3ConnectionSessionMetadata
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentLinkView
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.Web3Icon
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.signin.SignInUtil
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel

@Composable
fun Web3ConnectionPreviewScreen(
    viewModel: Web3ViewModel = viewModel(),
    onApproved: (web3Connection: Web3Connection) -> Unit = {},
    onDenied: () -> Unit = {}) {
    val context = LocalContext.current

    viewModel.getCreatedWeb3ConnectionResponse()?.let { createdWeb3ConnectionResponse ->
        val connectionId = createdWeb3ConnectionResponse.id ?: ""
        val appName = createdWeb3ConnectionResponse.sessionMetadata?.appName ?: "" // uniswap
        val appDescription = createdWeb3ConnectionResponse.sessionMetadata?.appDescription ?: ""
        val appUrl = createdWeb3ConnectionResponse.sessionMetadata?.appUrl ?: ""

        val uiState by viewModel.uiState.collectAsState()
        val userFlow by viewModel.userFlow.collectAsState()
        val showProgress = userFlow is UiState.Loading
        val mainModifier = Modifier.createMainModifier(showProgress, paddingTop = R.dimen.padding_extra_large_1)

        LaunchedEffect(key1 = uiState.web3Connections) {
            val approvedConnection = uiState.web3Connections.find { it.id == connectionId }
            if (approvedConnection != null) {
                onApproved(approvedConnection)
            }
        }

        LaunchedEffect(key1 = uiState.web3ConnectionApproved) {
            if (uiState.web3ConnectionApproved) {
                viewModel.onWeb3ConnectionApproved(false)
                viewModel.onWeb3ConnectionDenied(false)
                viewModel.loadWeb3Connections(UiState.Loading)
            }
        }

        LaunchedEffect(key1 = uiState.web3ConnectionDenied) {
            if (uiState.web3ConnectionDenied) {
                viewModel.onWeb3ConnectionApproved(false)
                viewModel.onWeb3ConnectionDenied(false)
                onDenied()
            }
        }

        Box(
            modifier = mainModifier
        ) {
            Column(
                modifier = Modifier
                    .background(
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                        color = grey_1
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val userData = SignInUtil.getInstance().getUserData(context)
                Column(modifier = Modifier.offset(y = -dimensionResource(id = R.dimen.web3_image_height_details) / 2),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically,) {
                        val profilePictureUrl = userData?.profilePictureUrl
                        val cardBackgroundColor: MutableState<Color> = remember { mutableStateOf(grey_1) }
                        Card(
                            modifier = Modifier.size(dimensionResource(id = R.dimen.web3_image_height_details)),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor.value),
                            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                        ) {
                            Web3Icon(
                                context,
                                profilePictureUrl,
                                imageSizeResId = R.dimen.web3_details_image_size,
                                placeholderResId = R.drawable.ic_avatar_circle,
                                onDominantColorExtracted = { color ->
                                    cardBackgroundColor.value = color
                                })
                        }
                        Image(
                            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small)),
                            painter = painterResource(id = R.drawable.ic_switch_horizontal),
                            contentDescription = null,
                        )
                        Card(
                            modifier = Modifier.size(dimensionResource(id = R.dimen.web3_image_height_details)),
                            colors = CardDefaults.cardColors(containerColor = white),
                            shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                        ) {
                            Web3Icon(context, createdWeb3ConnectionResponse.sessionMetadata?.appIcon, imageSizeResId = R.dimen.web3_details_image_size)
                        }
                    }
                    FireblocksText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small_2)),
                        text = stringResource(id = R.string.web3_connect_to, appName),
                        textStyle = FireblocksNCWDemoTheme.typography.b1,
                        textAlign = TextAlign.Center,
                        textColor = white
                    )
                    userData?.email?.let { email ->
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_extra_small)),
                            text = stringResource(id = R.string.by_email_suffix, email),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Center,
                            textColor = text_secondary
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.padding_large)
                        )
                ) {
                    TitleContentView(
                        titleText = stringResource(id = R.string.description),
                        titleColor = text_secondary,
                        contentText = appDescription,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = null
                    )
                    TitleContentLinkView(
                        titleText = stringResource(id = R.string.website),
                        titleColor = text_secondary,
                        contentText = appUrl,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = R.dimen.padding_default,
                        bottomPadding = R.dimen.padding_default
                    )
                }
            }
            if (showProgress) {
                ProgressBar()
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_default)),) {
                if (userFlow is UiState.Error) {
                    ErrorView(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),//.align(Alignment.BottomEnd),
                        errorState = userFlow as UiState.Error, defaultResId = R.string.approve_connection_error)
                }
                ConnectButton(modifier = Modifier, viewModel = viewModel, id = connectionId)
                DiscardButton(modifier = Modifier, viewModel = viewModel, id = connectionId)
            }
        }
    }
}

@Composable
private fun ConnectButton(modifier: Modifier, viewModel: Web3ViewModel, id: String) {
    val continueEnabledState = remember { mutableStateOf(true) }
    ContinueButton(
        enabledState = continueEnabledState,
        onClick = {
        viewModel.submitWeb3Connection(id, payload = RespondToConnectionRequest(true))
    },
        labelResourceId = R.string.connect
    )
}

@Composable
private fun DiscardButton(modifier: Modifier, viewModel: Web3ViewModel, id: String) {
    val continueEnabledState = remember { mutableStateOf(true) }
    ContinueButton(
        enabledState = continueEnabledState,
        onClick = {
            viewModel.submitWeb3Connection(id, payload = RespondToConnectionRequest(false))
        },
        labelResourceId = R.string.discard,
        colors = ButtonDefaults.buttonColors(containerColor = transparent)
    )
}

@Preview
@Composable
fun Web3ConnectionApprovalScreenPreview() {
    val viewModel = Web3ViewModel()
    viewModel.onWeb3ConnectionCreated(
        CreateWeb3ConnectionResponse(
            id = "d51e57d9a19cfb20452e30bef76ceaf3523f919c0d48da9ae0f7d3c332df85e3",
            sessionMetadata = Web3ConnectionSessionMetadata(appName = "Uniswap", appDescription = "Uniswap is a decentralized finance protocol that is used to exchange cryptocurrencies.", appUrl = "https://uniswap.org/")
        )
    )
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            Web3ConnectionPreviewScreen(viewModel = viewModel)
        }
    }
}