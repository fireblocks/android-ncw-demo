package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.Web3Icon
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Web3Screen(
    viewModel: Web3ViewModel = viewModel(),
    web3Connection: Web3Connection? = null,
    onWeb3ConnectionRemoved: (() -> Unit)? = null,
) {
    val context = LocalContext.current

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

        Column(
            modifier = mainModifier
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.web3_image_height_details))
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(containerColor = white),
                    shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                ) {
                    Web3Icon(context, web3Connection.sessionMetadata?.appIcon, imageSizeResId = R.dimen.web3_details_image_size)
                }
                FireblocksText(
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small_2)),
                    text = appName,
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textAlign = TextAlign.Center,
                    textColor = white
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(
                            vertical = dimensionResource(id = R.dimen.padding_default),
                            horizontal = dimensionResource(id = R.dimen.padding_large)
                        )
                ) {
                    TitleContentHorizontalView(
                        titleText = stringResource(id = R.string.connection),
                        titleColor = grey_4,
                        contentText = appName,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = null,
                        contentDescriptionText = stringResource(id = R.string.nft_name_value_desc),
                    )
                    TitleContentHorizontalView(
                        titleText = stringResource(id = R.string.connection_date),
                        titleColor = grey_4,
                        contentText = date,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = R.dimen.padding_default,
                        contentDescriptionText = stringResource(id = R.string.connection_date_value_desc),
                    )
                    if (chainIds.isNotEmpty()) {
                        FireblocksText(
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_default)),
                            text = stringResource(id = R.string.chain_ids),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                            textAlign = TextAlign.Start,
                            textColor = grey_4
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
                                    textColor = grey_4
                                )
                            }
                        }

                    }
                    TitleContentHorizontalView(
                        titleText = stringResource(id = R.string.fee_level),
                        titleColor = grey_4,
                        contentText = feeLevel,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        topPadding = R.dimen.padding_default,
                        contentDescriptionText = stringResource(id = R.string.connection_date_value_desc),
                    )
                    TitleContentView(
                        titleText = stringResource(id = R.string.id),
                        titleColor = grey_4,
                        contentText = connectionId,
                        contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                        contentDrawableRes = R.drawable.ic_copy,
                        onContentButtonClick = { copyToClipboard(context, connectionId) },
                        topPadding = R.dimen.padding_default,
                        contentDescriptionText = stringResource(id = R.string.nft_id_value_desc))
                }
            }
            if (userFlow is UiState.Error) {
                ErrorView(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_default)),
                    errorState = userFlow as UiState.Error, defaultResId = R.string.recover_wallet_error)
            }
            if (showProgress) {
                ProgressBar()
            }

            onWeb3ConnectionRemoved?.let {
                RemoveConnectionButton(viewModel = viewModel, id = connectionId)
                LaunchedEffect(key1 = uiState.web3ConnectionRemoved) {
                    if (uiState.web3ConnectionRemoved) {
                        onWeb3ConnectionRemoved()
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveConnectionButton(viewModel: Web3ViewModel, id: String) { //TODO add are you sure bottom sheet
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
    val item = Web3Connection(
        id = "d51e57d9a19cfb20452e30bef76ceaf3523f919c0d48da9ae0f7d3c332df85e3",
        sessionMetadata = Web3ConnectionSessionMetadata(appUrl = "https://app.ens.domains/", appName = "ens"),
        creationDate = "2023-07-04T12:00:00Z",
        chainIds = listOf("ETH_TEST5", "ETH_MAINNET", "ETH_ROPSTEN", "ETH_RINKEBY"),
        feeLevel = Web3ConnectionFeeLevel.MEDIUM
    )
    FireblocksNCWDemoTheme {
        Web3Screen(web3Connection = item)
    }
}