package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.DetailsListItem
import com.fireblocks.sdkdemo.ui.compose.components.StatusLabel
import com.fireblocks.sdkdemo.ui.theme.blue
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.success
import com.fireblocks.sdkdemo.ui.theme.text_secondary
import com.fireblocks.sdkdemo.ui.viewmodel.AdvancedInfoViewModel

/**
 * Created by Fireblocks Ltd. on 05/07/2023.
 */
@Composable
fun AdvancedInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: AdvancedInfoViewModel = viewModel(),
    onBackClicked: () -> Unit = {},
    infoData: InfoData = createInfoData(viewModel)
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                currentScreen = FireblocksScreen.AdvancedInfo,
                navigateUp = onBackClicked,
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Content(infoData)
        }
    }
}

@Composable
fun createInfoData(viewModel: AdvancedInfoViewModel): InfoData {
    val deviceId = getDeviceId(viewModel)
    val items = ArrayList<KeyDescriptor>()
    val context = LocalContext.current
    runCatching {
        val status = FireblocksManager.getInstance().getKeyCreationStatus(context)
        status.forEach {
            items.add((it))
        }
    }.onFailure {
        DialogUtil.getInstance().start("Failure", "${it.message}", buttonText = context.getString(R.string.OK), autoCloseTimeInMillis = 3000)
    }

    return InfoData(deviceId = deviceId,
        keys = items
    )
}

@Composable
fun Content(infoData: InfoData) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_default))) {
        Column(modifier = Modifier.background(color = grey_1, shape = RoundedCornerShape(size = dimensionResource(R.dimen.round_corners_default)))) {
            //DeviceId
            val deviceId = infoData.deviceId
            DetailsListItem(titleResId = R.string.device_id, contentText = deviceId, showCopyButton = true)
            HorizontalDivider()
            //Wallet Id
            val walletId = StorageManager.get(context, deviceId).walletId.value()
            DetailsListItem(titleResId = R.string.wallet_id, contentText = walletId, showCopyButton = true)
        }
        infoData.keys.forEach { keyDescriptor ->
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
            Column(
                modifier = Modifier.background(color = grey_1, shape = RoundedCornerShape(size = dimensionResource(R.dimen.round_corners_default))))
            {
                val keyIdWithStatusAnnotatedString = getKeyIdWithStatusAnnotatedString(stringResource(R.string.key_id), keyDescriptor.keyStatus)
                DetailsListItem(titleAnnotatedString = keyIdWithStatusAnnotatedString, contentText = keyDescriptor.keyId)
                HorizontalDivider()
                DetailsListItem(titleResId = R.string.algorithm, contentText = keyDescriptor.algorithm?.name)
            }
        }
    }
}

@Composable
private fun getKeyIdWithStatusAnnotatedString(keyId: String, keyStatus: KeyStatus?): AnnotatedString {
    return buildAnnotatedString {
        // Add keyId with styling
        withStyle(style = SpanStyle(
            color = text_secondary,
            fontSize = FireblocksNCWDemoTheme.typography.b2.fontSize
        )) {
            append(keyId)
        }

        append("\n")

        // Add status if available
        if (keyStatus != null) {
            withStyle(style = SpanStyle(
                color = getStatusColor(keyStatus),
                fontSize = FireblocksNCWDemoTheme.typography.b3.fontSize
            )) {
                append(keyStatus.name.capitalizeFirstLetter())
            }
        }
    }
}

@Composable
private fun Status(keyDescriptor: KeyDescriptor) {
    keyDescriptor.keyStatus?.let { keyStatus ->
        val color = getStatusColor(keyStatus)
        StatusLabel(
            message = keyStatus.name.capitalizeFirstLetter(),
            color = color,
        )
    }
}

@Composable
private fun getStatusColor(keyStatus: KeyStatus): Color {
    val color = when (keyStatus) {
        KeyStatus.READY -> success
        KeyStatus.INITIATED,
        KeyStatus.REQUESTED_SETUP,
        KeyStatus.SETUP,
        KeyStatus.SETUP_COMPLETE -> blue

        KeyStatus.STOPPED,
        KeyStatus.TIMEOUT,
        KeyStatus.ERROR -> error
    }
    return color
}

@Composable
private fun getDeviceId(viewModel: AdvancedInfoViewModel): String {
    var latestDeviceId = viewModel.getDeviceId(LocalContext.current)
    if (latestDeviceId.isEmpty()) {
        latestDeviceId = stringResource(R.string.not_initialized)
    }
    return latestDeviceId
}

@Preview
@Composable
fun AdvancedInfoScreenPreview() {

    val deviceId = "ca7189b2-89aa-4e65-b96c-7a0a669c9645"
    StorageManager.get(LocalContext.current, deviceId).walletId.set("ca7189b2-89aa-4e65-b96c-7a0a669c9645")
    FireblocksNCWDemoTheme {
        val infoData = InfoData(deviceId = deviceId,
            keys = arrayListOf(KeyDescriptor("ca111-89aa-4e65-b96c-7a0a669c9645", Algorithm.MPC_ECDSA_SECP256K1, KeyStatus.READY),
                KeyDescriptor("ca222-89aa-4e65-b96c-7a0a669c9645", Algorithm.MPC_ECDSA_SECP256K1, KeyStatus.ERROR)))
        AdvancedInfoScreen(onBackClicked = {}, infoData = infoData)
    }
}

data class InfoData(val deviceId: String, val keys: ArrayList<KeyDescriptor>)