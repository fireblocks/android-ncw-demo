package com.fireblocks.sdkdemo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdk.keys.Algorithm
import com.fireblocks.sdk.keys.KeyDescriptor
import com.fireblocks.sdk.keys.KeyStatus
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.capitalizeFirstLetter
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.StatusLabel
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.theme.error
import com.fireblocks.sdkdemo.ui.theme.grey_2
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.purple
import com.fireblocks.sdkdemo.ui.theme.success
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
        Column {
            //DeviceId
            val deviceId = infoData.deviceId
            TitleContentView(
                titleText = stringResource(id = R.string.device_id),
                titleColor = grey_4,
                contentText = deviceId,
                contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                contentDrawableRes = R.drawable.ic_copy,
                onContentButtonClick = { copyToClipboard(context, deviceId) },
                topPadding = null,
                contentDescriptionText = stringResource(id = R.string.device_id_value_desc),
            )
            //Wallet Id
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
            val walletId = StorageManager.get(context, deviceId).walletId.value()

            TitleContentView(
                titleText = stringResource(id = R.string.wallet_id),
                titleColor = grey_4,
                contentText = walletId,
                contentTextStyle = FireblocksNCWDemoTheme.typography.b2,
                contentDrawableRes = R.drawable.ic_copy,
                onContentButtonClick = { copyToClipboard(context, walletId) },
                topPadding = null,
                contentDescriptionText = stringResource(id = R.string.wallet_id_value_desc),
            )
        }
        infoData.keys.forEach { keyDescriptor ->
            Divider(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_default)),
                color = grey_2,
            )
            Column {
                val keyIdDescriptor = stringResource(R.string.key_id_value_desc)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // KeyId
                    FireblocksText(text = stringResource(R.string.key_id), textColor = grey_4)
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_default)))
                    // Status
                    Status(keyDescriptor)
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))
                FireblocksText(
                    modifier = Modifier.semantics { contentDescription = keyIdDescriptor },
                    text = keyDescriptor.keyId,
                    textStyle = FireblocksNCWDemoTheme.typography.b2)


                // Algorithm
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
                FireblocksText(text = stringResource(R.string.algorithm), textColor = grey_4)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_default)))

                val algorithmDescriptor = stringResource(R.string.algorithm_value_desc)
                FireblocksText(
                    modifier = Modifier.semantics { contentDescription = algorithmDescriptor },
                    text = keyDescriptor.algorithm?.name, textStyle = FireblocksNCWDemoTheme.typography.b3)
            }
        }
    }
}

@Composable
private fun Status(keyDescriptor: KeyDescriptor) {
    keyDescriptor.keyStatus?.let { keyStatus ->
        val color = when (keyStatus) {
            KeyStatus.READY -> success
            KeyStatus.INITIATED,
            KeyStatus.REQUESTED_SETUP,
            KeyStatus.SETUP,
            KeyStatus.SETUP_COMPLETE -> purple

            KeyStatus.STOPPED,
            KeyStatus.TIMEOUT,
            KeyStatus.ERROR -> error
        }
        StatusLabel(
            message = keyStatus.name.capitalizeFirstLetter(),
            color = color,
        )
    }
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
    FireblocksNCWDemoTheme {
        val infoData = InfoData(deviceId = deviceId,
            keys = arrayListOf(KeyDescriptor("ca111-89aa-4e65-b96c-7a0a669c9645", Algorithm.MPC_ECDSA_SECP256K1, KeyStatus.READY),
                KeyDescriptor("ca222-89aa-4e65-b96c-7a0a669c9645", Algorithm.MPC_ECDSA_SECP256K1, KeyStatus.ERROR)))
        AdvancedInfoScreen(onBackClicked = {}, infoData = infoData)
    }
}

data class InfoData(val deviceId: String, val keys: ArrayList<KeyDescriptor>)