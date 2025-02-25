package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.ew.models.Web3Connection
import com.fireblocks.sdk.ew.models.Web3ConnectionFeeLevel
import com.fireblocks.sdk.ew.models.Web3ConnectionSessionMetadata
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.Label
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentHorizontalView
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentLinkView
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.Web3Icon
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Web3Screen(
    viewModel: Web3ViewModel = viewModel(),
    onWeb3ConnectionRemoved: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val web3Connection = viewModel.getSelectedWeb3Connection()
    web3Connection?.let {

        val uiState by viewModel.uiState.collectAsState()
        val userFlow by viewModel.userFlow.collectAsState()
        val showProgress = userFlow is UiState.Loading
        val mainModifier = Modifier.createMainModifier(showProgress)

        val connectionId = web3Connection.id ?: ""
        val appName = web3Connection.sessionMetadata?.appName ?: "" // metamask
        val date = web3Connection.creationDate?.let {
            val parsedDate = ZonedDateTime.parse(it)
            val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            formattedDate
        } ?: "" // 2023-07-04 12:00:00
        val chainIds = web3Connection.chainIds ?: listOf()
        val feeLevel = web3Connection.feeLevel?.name ?: ""
        val appDescription = web3Connection.sessionMetadata?.appDescription
        val appUrl = web3Connection.sessionMetadata?.appUrl ?: ""

        Box(
            modifier = mainModifier
        ) {
            Column(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.padding_large))
                    .background(
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                        color = grey_1),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.offset(y = -dimensionResource(id = R.dimen.web3_image_height_details) / 2)) {
                    Card(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.web3_image_height_details))
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(containerColor = white),
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                    ) {
                        Web3Icon(context, web3Connection.sessionMetadata?.appIcon, imageSizeResId = R.dimen.web3_details_image_size)
                    }
                    Row(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small_2)),
                        verticalAlignment = Alignment.CenterVertically) {
                        FireblocksText(
                            text = stringResource(id = R.string.dapp_connection, appName),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Center,
                            textColor = white
                        )
                        Image(
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_small_1)),
                            painter = painterResource(id = R.drawable.ic_green_dot),
                            contentDescription = null
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_default),)
                ) {
                    TitleContentHorizontalView(
                        titleText = stringResource(id = R.string.connection_date),
                        titleColor = text_secondary,
                        contentText = date,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        contentDescriptionText = stringResource(id = R.string.connection_date_value_desc),
                        topPadding = null
                    )
                    if (chainIds.isNotEmpty()) {
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                            text = stringResource(id = R.string.chain_ids),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Start,
                            textColor = text_secondary
                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(R.dimen.padding_default)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                        ) {
                            chainIds.forEach {
                                Label(
                                    text = it,
                                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                                    textColor = text_secondary
                                )
                            }
                        }

                    }
                    TitleContentHorizontalView(
                        titleText = stringResource(id = R.string.fee_level),
                        titleColor = text_secondary,
                        contentText = feeLevel,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = R.dimen.padding_default,
                        contentDescriptionText = stringResource(id = R.string.connection_date_value_desc),
                    )
                    TitleContentView(
                        titleText = stringResource(id = R.string.id),
                        titleColor = text_secondary,
                        contentText = connectionId,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, connectionId) },
                        topPadding = R.dimen.padding_default,
                        contentDescriptionText = stringResource(id = R.string.nft_id_value_desc))
                    appDescription?.let {
                        TitleContentView(
                            titleText = stringResource(id = R.string.description),
                            titleColor = text_secondary,
                            contentText = appDescription,
                            contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                            topPadding = R.dimen.padding_default,
                        )
                    }
                    TitleContentLinkView(
                        titleText = stringResource(id = R.string.website),
                        titleColor = text_secondary,
                        contentText = appUrl,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = R.dimen.padding_default,
                        bottomPadding = R.dimen.padding_large,
                    )
                }
            }
            if (showProgress) {
                ProgressBar()
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                if (userFlow is UiState.Error) {
                    ErrorView(
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_default)),
                        errorState = userFlow as UiState.Error, defaultResId = R.string.recover_wallet_error
                    )
                }

                onWeb3ConnectionRemoved?.let {
                    RemoveConnectionButton(viewModel = viewModel, id = connectionId)
                    LaunchedEffect(key1 = uiState.web3ConnectionRemoved) {
                        if (uiState.web3ConnectionRemoved) {
                            viewModel.clean()
                            onWeb3ConnectionRemoved()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveConnectionButton(viewModel: Web3ViewModel, id: String) {
    val continueEnabledState = remember { mutableStateOf(true) }
    ContinueButton(
        enabledState = continueEnabledState,
        onClick = {
        viewModel.removeWeb3Connection(id)
    },
        labelResourceId = R.string.remove_connection
    )
}

@Preview
@Composable
fun Web3ScreenPreview() {
    val web3Connection = Web3Connection(
        id = "d51e57d9a19cfb20452e30bef76ceaf3523f919c0d48da9ae0f7d3c332df85e3",
        sessionMetadata = Web3ConnectionSessionMetadata(appUrl = "https://app.ens.domains/", appName = "ENS", appDescription = "ENS is a decentralized domain name service", appIcon = "https://app.ens.domains/favicon.ico"),
        creationDate = "2023-07-04T12:00:00Z",
        chainIds = listOf("ETH_TEST5", "ETH_MAINNET", "ETH_ROPSTEN", "ETH_RINKEBY"),
        feeLevel = Web3ConnectionFeeLevel.MEDIUM
    )
    val viewModel = Web3ViewModel()
    viewModel.onWeb3ConnectionSelected(web3Connection)
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            Web3Screen(viewModel = viewModel, onWeb3ConnectionRemoved = {})
        }
    }
}