package com.fireblocks.sdkdemo.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fireblocks.sdk.ew.models.BlockchainDescriptor
import com.fireblocks.sdk.ew.models.MediaEntityResponse
import com.fireblocks.sdk.ew.models.SpamOwnershipResponse
import com.fireblocks.sdk.ew.models.TokenCollectionResponse
import com.fireblocks.sdk.ew.models.TokenOwnershipResponse
import com.fireblocks.sdk.ew.models.TokensStatus
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeData
import com.fireblocks.sdkdemo.bl.core.storage.models.FeeLevel
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.ContinueButton
import com.fireblocks.sdkdemo.ui.compose.components.ErrorView
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.createMainModifier
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.transparent
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.NFTsViewModel
import com.fireblocks.sdkdemo.ui.viewmodel.WalletViewModel


/**
 * Created by Fireblocks Ltd. on 18/07/2023.
 */
@Composable
fun NFTFeeScreen(
    nfTsViewModel: NFTsViewModel,
    walletViewModel: WalletViewModel,
    onNextScreen: () -> Unit = {},
) {
    val selectedNFT = nfTsViewModel.getSelectedNFT()
    selectedNFT?.let { nft ->
        val uiState by walletViewModel.uiState.collectAsState()
        val userFlow by walletViewModel.userFlow.collectAsState()
        val context = LocalContext.current
        val showProgress = userFlow is UiState.Loading
        val mainModifier = Modifier.createMainModifier(showProgress)

        if (showProgress) {
            ProgressBar()
        }

        LaunchedEffect(key1 = uiState.createdTransaction) {
            if (uiState.createdTransaction) {
                onNextScreen()
            }
        }

        Column(
            modifier = mainModifier,
        ) {
            NFTListItem(nft = nft)

            Column(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
            ) {
                val feeItems = arrayListOf<FeeItem>()
                val selectedIndex = remember { mutableIntStateOf(2) }
                val continueEnabledState = remember { mutableStateOf(true) }

                Column(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(containerColor = grey_1),
                        shape = RoundedCornerShape( size = dimensionResource(id = R.dimen.round_corners_default),)
                    ) {
                        Column(modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_default),
                            vertical = dimensionResource(id = R.dimen.padding_extra_large)
                        ),) {

                            FireblocksText(
                                text = stringResource(id = R.string.select_fee_speed),
                                textStyle = FireblocksNCWDemoTheme.typography.b1,
                                textAlign = TextAlign.Start
                            )
                            Row(
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_default)),
                                verticalAlignment = Alignment.CenterVertically) {

                                feeItems.add(FeeItem(R.string.slow, feeData = FeeData(feeLevel = FeeLevel.LOW)))
                                feeItems.add(FeeItem(R.string.medium, feeData = FeeData(feeLevel = FeeLevel.MEDIUM)))
                                feeItems.add(FeeItem(R.string.fast, feeData = FeeData(feeLevel = FeeLevel.HIGH)))

                                FeeList(selectedIndex, feeItems)
                            }
                        }
                    }
                }

                if (userFlow is UiState.Error) {
                    ErrorView(
                        errorState = userFlow as UiState.Error,
                        defaultResId = R.string.create_transaction_error
                    )
                    continueEnabledState.value = true
                }
                ContinueButton(
                    continueEnabledState,
                    labelResourceId = R.string.create_transaction,
                    onClick = {
                        continueEnabledState.value = false
                        val selectedFee = feeItems[selectedIndex.intValue].feeData
                        walletViewModel.onSelectedFee(selectedFee)
                        walletViewModel.createTransaction(context)
                    })
            }
        }
    }
}

@Composable
fun FeeList(
    selectedIndex: MutableIntState,
    feeItems: ArrayList<FeeItem>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        feeItems.forEachIndexed { index, feeItem ->
            item {
                FeeListItem(
                    feeItem.labelResId,
                    selected = selectedIndex.intValue == index,
                    clickAction = { selectedIndex.intValue = index })
            }
        }
    }
}

data class FeeItem(
    val labelResId: Int,
    val feeData: FeeData,
)

@Composable
private fun FeeListItem(
    @StringRes labelResId: Int,
    fee: String? = null,
    symbol: String? = null,
    selected: Boolean = false,
    clickAction: () -> Unit = {}
) {

    Surface(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { clickAction() }, ),
        color = if (selected) grey_2 else transparent,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_corners_small)),
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_default))
        ) {
            FireblocksText(
                modifier = Modifier.weight(1f),
                text = stringResource(id = labelResId),
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = if (selected) white else text_secondary
            )
        }
    }
}

@Preview
@Composable
fun NFTFeeScreenPreview() {
    val nfTsViewModel = NFTsViewModel()
    nfTsViewModel.onNFTSelected(
        TokenOwnershipResponse(
            id = "NFT-13ae5a0288cae2f191a9e3628790fb08e6a7ac91",
            tokenId = "1",
            standard = "ERC1155",
            blockchainDescriptor = BlockchainDescriptor.XTZ_TEST,
            description = "This is the NFT description",
            name = "sword",
            metadataURI = "ipfs://bafybeidstfcbqv2v2ursvchraw64hu2p4e3rx37dpoqzeguvketcfetlji/1",
            cachedMetadataURI = "https://stage-static.fireblocks.io/dev9/nft/24d1bea228cbcb5010db9a91376c65ea/metadata.json",
            media = listOf(
                MediaEntityResponse(
                    url = "https://stage-static.fireblocks.io/dev9/nft/media/aXBmczovL2JhZnliZWloamNuYXFrd3lucG9kaW5taW54dXdiZ3VucWNxYnNlMmxwb2kzazJibnIyempneXhyaHV1LzE",
                    contentType = MediaEntityResponse.ContentType.IMAGE
                )
            ),
            collection = TokenCollectionResponse(
                id = "0xe0e2C83BdE2893f93012b9FE7cc0bfC2893b344B",
                name = "my collection",
                symbol = "A"
            ),
            spam = SpamOwnershipResponse(
                true,
                source = SpamOwnershipResponse.SpamOwnershipResponseSource.SYSTEM
            ),
            balance = "1",
            ownershipStartTime = 123,
            ownershipLastUpdateTime = 123,
            status = TokensStatus.LISTED,
            vaultAccountId = "1",
            ncwId = "1",
            ncwAccountId = "1",
        )
    )

    FireblocksNCWDemoTheme {
        Surface(color = background) {
            NFTFeeScreen(nfTsViewModel = nfTsViewModel, walletViewModel = WalletViewModel())
        }
    }
}

