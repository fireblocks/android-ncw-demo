package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.beautifySigningStatus
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.roundToDecimalFormat
import com.fireblocks.sdkdemo.bl.core.storage.models.NFTWrapper
import com.fireblocks.sdkdemo.bl.core.storage.models.SigningStatus
import com.fireblocks.sdkdemo.bl.core.storage.models.SupportedAsset
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.utils.AssetsUtils
import com.fireblocks.sdkdemo.ui.screens.wallet.getStatusColor
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.theme.white
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun TransferDetailsListView(
    modifier: Modifier? = Modifier,
    rate: Double = 1.0,
    showAssetItem: Boolean? = false,
    isOutgoingTransaction: Boolean? = null,
    supportedAsset: SupportedAsset? = null,
    assetAmount: String? = null,
    assetUsdAmount: String? = null,
    recipientAddress: String? = null,
    fee: String? = null,
    symbol: String? = null,
    status: SigningStatus? = null,
    txId: String? = null,
    txHash: String? = null,
    nftId: String? = null,
    creationDate: String? = null,
    nftWrapper: NFTWrapper? = null
) {
    Card(
        modifier = modifier ?: Modifier,
        colors = CardDefaults.cardColors(containerColor = grey_1),
        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_list_item))
    ) {
        LazyColumn {
            val itemsList = mutableListOf<@Composable () -> Unit>()

            if (showAssetItem == true && supportedAsset != null && !assetAmount.isNullOrEmpty() && !assetUsdAmount.isNullOrEmpty()) {
                itemsList.add {
                    val asset = supportedAsset.copy(balance = assetAmount, price = assetUsdAmount)
                    TransferDetailsAssetListItem(supportedAsset = asset, isOutgoingTransaction = isOutgoingTransaction, nftWrapper = nftWrapper)
                }
            }
            recipientAddress?.let {
                itemsList.add {
                    DetailsListItem(titleResId = R.string.recipient, contentText = it, showCopyButton = true)
                }
            }
            if (!fee.isNullOrEmpty() && !symbol.isNullOrEmpty()) {
                itemsList.add {
                    val estimatedFee = stringResource(
                        id = R.string.asset_amount,
                        fee.roundToDecimalFormat(), symbol
                    )
                    val feeAmountAsBigDecimal = BigDecimal(fee)
                    val rateAsBigDecimal = BigDecimal(rate)
                    val estimatedFeeInUsdAsBigDecimal = feeAmountAsBigDecimal.multiply(rateAsBigDecimal)
                    val symbols = DecimalFormatSymbols(Locale.ENGLISH)
                    val df = DecimalFormat("#.########", symbols)
                    df.roundingMode = RoundingMode.DOWN
                    val estimatedFeeInUsd = df.format(estimatedFeeInUsdAsBigDecimal)
                    val estimatedFeeInUsdFormatted = stringResource(id = R.string.usd_balance, estimatedFeeInUsd)
                    val feeTitleResId = when(isOutgoingTransaction) {
                        true, false -> R.string.fee
                        null -> R.string.estimated_fee
                    }
                    DetailsListItem(
                        titleResId = feeTitleResId,
                        contentText = estimatedFee,
                        subContent = estimatedFeeInUsdFormatted
                    )
                }
            }
            status?.name?.let { statusName ->
                itemsList.add {
                    DetailsListItem(
                        titleResId = R.string.status,
                        contentText = statusName.beautifySigningStatus(),
                        contentColor = getStatusColor(status)
                    )
                }
            }
            creationDate?.let {
                itemsList.add {
                    DetailsListItem(titleResId = R.string.creation_date, contentText = it)
                }
            }
            txId?.let {
                itemsList.add {
                    DetailsListItem(
                        titleResId = R.string.fireblocks_transaction_id,
                        contentText = it,
                        showCopyButton = true,
                    )
                }
            }
            nftId?.let {
                itemsList.add {
                    DetailsListItem(
                        titleResId = R.string.fireblocks_nft_id,
                        contentText = it,
                        showCopyButton = true,
                    )
                }
            }
            if (!txHash.isNullOrEmpty()) {
                itemsList.add {
                    DetailsListItem(
                        titleResId = R.string.transaction_hash,
                        contentText = txHash,
                        showCopyButton = true,
                    )
                }
            }

            itemsIndexed(itemsList) { index, item ->
                item()
                if (index < itemsList.size - 1) {
                    FireblocksHorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun SymbolListItem(
    titleResId: Int = R.string.blockchain,
    blockchain: String,
    blockchainSymbol: String
) {
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_large))
            .height(dimensionResource(R.dimen.details_list_item_height))
        ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FireblocksText(
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.cell_title_width))
                .padding(end = dimensionResource(R.dimen.padding_small_2))
            ,
            text = stringResource(titleResId),
            textStyle = FireblocksNCWDemoTheme.typography.b1,
            textColor = text_secondary,
            textAlign = TextAlign.Start,
            maxLines = 2,
        )
        CryptoIcon(
            symbol = blockchainSymbol,
            imageSizeResId = R.dimen.image_size_very_small,
            paddingResId = R.dimen.padding_none,
            placeholderResId = R.drawable.ic_blockchain_placeholder
        )
            FireblocksText(
                modifier = Modifier.weight(1f).padding(start = dimensionResource(R.dimen.padding_extra_small_1)),
                text = blockchain,
                textColor = white,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                maxLines = 1,
            )
    }
}

@Preview
@Composable
fun SymbolListItemPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            SymbolListItem(
                blockchain = "Ethereum Testnet Sepolia",
                blockchainSymbol = "ETH",
            )
        }
    }
}

@Composable
fun TransferDetailsAssetListItem(supportedAsset: SupportedAsset, isOutgoingTransaction: Boolean? = null, nftWrapper: NFTWrapper? = null) {
    val titleResId = when (isOutgoingTransaction) {
        true -> R.string.sent
        false -> R.string.received
        null -> R.string.send
    }
    nftWrapper?.let {
        val numberOfNfts = when(supportedAsset.balance) { //TODO consider taking from transactionWrapper.amount
            null, "", "1" -> stringResource(R.string.one_nft)
            else -> stringResource(R.string.n_nfts, supportedAsset.balance!!)
        }
        DetailsListItem(
            titleResId = titleResId,
            contentText = numberOfNfts,
        )
    } ?: run {
        DetailsListItem(
            titleResId = titleResId,
            contentText = supportedAsset.balance?.roundToDecimalFormat(),
            subContent = stringResource(id = R.string.usd_balance, supportedAsset.price),
        )
    }
    Row(
        modifier = Modifier.padding(
            start = (dimensionResource(R.dimen.cell_title_width))
                    + dimensionResource(R.dimen.padding_large),
            bottom = dimensionResource(R.dimen.padding_small)
        ),
    ) {
        Column {
            FireblocksHorizontalDivider()
            Row(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small_2))) {
                nftWrapper?.let {
                    TransferNFTView(nftWrapper = it)
                } ?: run {
                    CryptoIconCard(
                        iconUrl = supportedAsset.iconUrl,
                        symbol = supportedAsset.symbol,
                        blockchain = supportedAsset.blockchain
                    )
                    Column(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_default))) {
                        FireblocksText(
                            text = AssetsUtils.getAssetTitleText(LocalContext.current, supportedAsset),
                            textStyle = FireblocksNCWDemoTheme.typography.b1,
                        )
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FireblocksText(
                                text = supportedAsset.name,
                                textStyle = FireblocksNCWDemoTheme.typography.b2,
                                textColor = text_secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsListItem(
    titleResId: Int? = null,
    titleAnnotatedString: AnnotatedString? = null,
    contentText: String? = null,
    subContent: String? = null,
    showCopyButton: Boolean? = false,
    contentColor: Color? = white,
    overflow: TextOverflow = TextOverflow.MiddleEllipsis,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_large))
            .height(dimensionResource(R.dimen.details_list_item_height)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (titleAnnotatedString != null) {
            FireblocksText(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.cell_title_width))
                    .padding(end = dimensionResource(R.dimen.padding_small_2))
                ,
                annotatedString = titleAnnotatedString,
                textStyle = FireblocksNCWDemoTheme.typography.b1,
                textColor = text_secondary,
                textAlign = TextAlign.Start,
                maxLines = 2,
            )
        } else {
            titleResId?.let {
                FireblocksText(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.cell_title_width))
                        .padding(end = dimensionResource(R.dimen.padding_small_2))
                    ,
                    text = stringResource(it),
                    textStyle = FireblocksNCWDemoTheme.typography.b1,
                    textColor = text_secondary,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                )
            }
        }
        if (!contentText.isNullOrEmpty()) {
            if (subContent.isNullOrEmpty()) {
                FireblocksText(
                    modifier = Modifier.weight(1f),
                    text = contentText,
                    textColor = contentColor ?: white,
                    textStyle = FireblocksNCWDemoTheme.typography.b2,
                    maxLines = 1,
                    overflow = overflow
                )
            } else {
                Column(modifier = Modifier.weight(1f))
                {
                    TitleContentView(
                        topPadding = null,
                        titleText = contentText,
                        titleColor = white,
                        contentText = subContent,
                        contentColor = text_secondary,
                        contentDescriptionText = stringResource(id = R.string.address_value_desc),
                    )
                }
            }
        }

        if (showCopyButton == true) {
            Image(
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.padding_default))
                    .clickable { copyToClipboard(context, contentText) },
                painter = painterResource(id = R.drawable.ic_copy), contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun TransferDetailsTableViewPreview() {
    FireblocksNCWDemoTheme {
        Surface(color = background) {
            val supportedAsset = SupportedAsset(
                id = "ETH",
                symbol = "ETH",
                name = "Ether",
                type = "BASE_ASSET",
                blockchain = "Ethereum",
                iconUrl = "",
                balance = "1.0",
                price = "2000.0"
            )
            TransferDetailsListView(
                showAssetItem = true,
                supportedAsset = supportedAsset,
                assetAmount = "0.0001",
                assetUsdAmount = "0.2",
                recipientAddress = "0x324387ynckc83y48fhlc883mf",
                fee = "0.00002",
                rate = 2.0,
                symbol = "ETH",
                status = SigningStatus.PENDING_SIGNATURE,
                txId = "b4cf722f-34e9-47d0-b206-81c641ae87c7",
                txHash = "0x324387ynckc83y48fhlc883mf",
                nftId = "0x324387ynckc83y48fhlc883mf"
            )
        }
    }
}