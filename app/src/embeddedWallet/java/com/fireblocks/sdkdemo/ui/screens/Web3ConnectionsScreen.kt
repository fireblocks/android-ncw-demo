package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.ew.models.Web3Connection
import com.fireblocks.sdk.ew.models.Web3ConnectionSessionMetadata
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.Web3Icon
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.compose.lifecycle.OnLifecycleEvent
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.Web3ViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Web3ConnectionsScreen(modifier: Modifier = Modifier,
                          viewModel: Web3ViewModel = viewModel(),
                          onAddConnectionClicked: () -> Unit = {},
                          onWeb3ConnectionClicked: (web3Connection: Web3Connection) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val showProgress = userFlow is UiState.Loading
    val mainModifier = Modifier.createMainModifier(showProgress)

    val refreshing = userFlow is UiState.Refreshing
    fun refresh() = viewModel.loadWeb3Connections(state = UiState.Refreshing)
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    val web3Connections = uiState.web3Connections

    Box(
        modifier = mainModifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default)),
        ) {
            item {
                ListHeader(onAddConnectionClicked = onAddConnectionClicked)
            }
            web3Connections.forEach {
                item {
                    Web3ConnectionListItem(
                        web3Connection = it,
                        onClick = { onWeb3ConnectionClicked(it) }
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing,
            pullRefreshState,
            modifier.align(Alignment.TopCenter),
            contentColor = white,
            backgroundColor = transparent
        )

        if (showProgress) {
            ProgressBar()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event){
            Lifecycle.Event.ON_CREATE -> {
                val noConnections = (uiState.web3Connections.isEmpty())
                val state = if (noConnections) UiState.Loading else UiState.Idle
                viewModel.loadWeb3Connections(state)
            }
            else -> {}
        }
    }
}

@Composable
fun ListHeader(onAddConnectionClicked: () -> Unit = {}) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = dimensionResource(id = R.dimen.padding_default),
            start = dimensionResource(id = R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_default)))
    {
        FireblocksText(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.connected_dapps),
            textStyle = FireblocksNCWDemoTheme.typography.b1
        )
        Card(
            modifier = Modifier,
            colors = CardDefaults.cardColors(containerColor = grey_2),
            shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_small))
        ) {
            Image( //TODO add card with color
                modifier = Modifier.clickable { onAddConnectionClicked() }
                    .padding(dimensionResource(id = R.dimen.padding_small)),
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = stringResource(id = R.string.connected_dapps)
            )
        }
    }
}

@Composable
fun Web3ConnectionListItem(modifier: Modifier = Modifier,
                           web3Connection: Web3Connection,
                           clickable: Boolean = true,
                           onClick: (Web3Connection) -> Unit = {}) {
    val context = LocalContext.current
    val appName = web3Connection.sessionMetadata?.appName ?: "" // metamask
    val date = web3Connection.creationDate?.let {
        val parsedDate = ZonedDateTime.parse(it)
        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        formattedDate
    } ?: "" // 2023-07-04 12:00:00

    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)), color = grey_1)
            .padding(end = dimensionResource(id = R.dimen.padding_small))
            .clickable(enabled = clickable) { onClick(web3Connection) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_default)),
            colors = CardDefaults.cardColors(containerColor = white),
        ) {
            Web3Icon(context, web3Connection.sessionMetadata?.appIcon)
        }
        Column(modifier = Modifier.weight(1f)) {
            FireblocksText(
                text = appName,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
            )
            FireblocksText(
                modifier = Modifier.padding(top = 2.dp),
                text = stringResource(id = R.string.established_suffix, date),
                textStyle = FireblocksNCWDemoTheme.typography.b2,
                textColor = text_secondary
            )
        }
    }
}

@Preview
@Composable
fun Web3ConnectionsScreenPreview() {
    val viewModel = Web3ViewModel()
    val items = listOf(
        Web3Connection(
            id = "1",
            sessionMetadata = Web3ConnectionSessionMetadata(appUrl = "https://metamask.io/", appName = "Metamask"),
            chainIds = listOf("ETH_TEST5"),
            creationDate = "2023-07-04T12:00:00Z"
        ),
        Web3Connection(
            id = "2",
            sessionMetadata = Web3ConnectionSessionMetadata(appUrl = "https://trustwallet.com/", appName = "Trust Wallet"),
            chainIds = listOf("ETH_TEST5"),
            creationDate = "2023-07-04T12:00:00Z"
        ),
    )
    viewModel.onWeb3ConnectionsLoaded(items)
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            Web3ConnectionsScreen(viewModel = viewModel)
        }
    }
}

