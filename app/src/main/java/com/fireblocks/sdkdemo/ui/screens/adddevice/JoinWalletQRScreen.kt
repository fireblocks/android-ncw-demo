package com.fireblocks.sdkdemo.ui.screens.adddevice

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.copyToClipboard
import com.fireblocks.sdkdemo.bl.core.extensions.floatResource
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme
import com.fireblocks.sdkdemo.ui.compose.components.BaseTopAppBar
import com.fireblocks.sdkdemo.ui.compose.components.FireblocksText
import com.fireblocks.sdkdemo.ui.compose.components.ProgressBar
import com.fireblocks.sdkdemo.ui.compose.components.TitleContentView
import com.fireblocks.sdkdemo.ui.compose.components.rememberQrBitmapPainter
import com.fireblocks.sdkdemo.ui.main.UiState
import com.fireblocks.sdkdemo.ui.screens.FireblocksScreen
import com.fireblocks.sdkdemo.ui.theme.grey_1
import com.fireblocks.sdkdemo.ui.theme.grey_4
import com.fireblocks.sdkdemo.ui.theme.white
import com.fireblocks.sdkdemo.ui.viewmodel.AddDeviceViewModel

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun JoinWalletQRScreen(
    modifier: Modifier = Modifier,
    viewModel: AddDeviceViewModel = viewModel(),
    onBackClicked: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onNextScreen: () -> Unit = {},
    onError: () -> Unit = {}
) {
    BackHandler {
        // prevent back click
    }
    val uiState by viewModel.uiState.collectAsState()
    val userFlow by viewModel.userFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.joinedExistingWallet) {
        if (uiState.joinedExistingWallet) {
            viewModel.clean()
            onNextScreen()
        }
    }

    var mainModifier = modifier.fillMaxSize()
    var topBarModifier: Modifier = Modifier
    val showProgress = userFlow is UiState.Loading
    if (showProgress) {
        val progressAlpha = floatResource(R.dimen.progress_alpha)
        mainModifier = modifier
            .fillMaxSize()
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
        modifier = modifier,
        topBar = {
            BaseTopAppBar(
                modifier = topBarModifier,
                currentScreen = FireblocksScreen.JoinWalletQRScreen,
                navigateUp = {
                    viewModel.clean()
                    viewModel.stopJoinWallet(context)
                    onBackClicked()
                },
                onCloseClicked = {
                    viewModel.clean()
                    viewModel.stopJoinWallet(context)
                    onCloseClicked()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = mainModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = dimensionResource(R.dimen.padding_default)),
                ) {
                    NumberedInstructionList(
                        instructions = listOf(
                            Instruction(stringResource(id = R.string.add_device_instruction_step_1)),
                            Instruction(stringResource(id = R.string.add_device_instruction_step_2), R.drawable.ic_top_bar_menu),
                            Instruction(stringResource(id = R.string.add_device_instruction_step_3)),
                            Instruction(stringResource(id = R.string.add_device_instruction_step_4)),
                        )
                    )

                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.padding_large), bottom = dimensionResource(id = R.dimen.padding_default)),
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.round_corners_default)),
                        colors = CardDefaults.cardColors(containerColor = grey_1),
                    ) {
                        uiState.joinRequestData?.let { joinRequestData ->
                            if (joinRequestData.requestId.isNotNullAndNotEmpty()) {
                                val content = joinRequestData.encode()
                                Image(
                                    painter = rememberQrBitmapPainter(content),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .padding(vertical = dimensionResource(id = R.dimen.padding_extra_large))
                                        .width(171.dp)
                                        .height(171.dp)
                                        .align(Alignment.CenterHorizontally),
                                )
                                TitleContentView(
                                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_default)),
                                    titleText = stringResource(id = R.string.qr_code_link),
                                    titleColor = white,
                                    titleTextAlign = TextAlign.Center,
                                    contentText = content,
                                    contentTextAlign = TextAlign.Center,
                                    contentColor = grey_4,
                                    contentMaxLines = 1,
                                    contentDrawableRes = R.drawable.ic_copy,
                                    onContentButtonClick = { copyToClipboard(context, content) },
                                    topPadding = null,
                                    bottomPadding = R.dimen.padding_large,
                                    contentDescriptionText = stringResource(id = R.string.qr_code_link_value_desc),
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_default)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    if (userFlow is UiState.Error) {
                        viewModel.updateErrorType(AddDeviceViewModel.AddDeviceErrorType.FAILED)
                        onError()//TODO stop timer task
                    }
                    ExpirationTimer(viewModel = viewModel, onExpired = onError)
                }
            }
            if (showProgress) {
                ProgressBar(R.string.adding_device_progress_message)
            }
        }
    }
}
data class Instruction(val text: String, @DrawableRes val imageResourceId: Int? = null)
@Composable
fun NumberedInstructionList(
    instructions: List<Instruction>,
) {
    LazyColumn {
        instructions.forEachIndexed { index, instruction ->
            item {
                NumberedInstructionItem(
                    instruction = instruction.text,
                    number = index + 1,
                    imageResourceId = instruction.imageResourceId
                )
                if (index < instructions.lastIndex) {
                    Box(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small_1), vertical = dimensionResource(R.dimen.padding_small))) {
                        Divider(
                            modifier = Modifier
                            .width(4.dp)
                            .height(32.dp)
                            .background(color = grey_1, shape = RoundedCornerShape(size = 100.dp)),
                             color = grey_1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberedInstructionItem(instruction: String, number: Int, @DrawableRes imageResourceId: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .background(color = grey_1, shape = RoundedCornerShape(size = dimensionResource(R.dimen.round_corners_default))),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FireblocksText(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small), vertical = dimensionResource(R.dimen.padding_extra_small)),
                text = number.toString(),
                textStyle = FireblocksNCWDemoTheme.typography.b3,
                textAlign = TextAlign.Center,
            )
        }
        FireblocksText(
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small)),
            text = instruction,
            textStyle = FireblocksNCWDemoTheme.typography.b1,
            textAlign = TextAlign.Center,
        )
        imageResourceId?.let {
            Image(
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small)),
                painter = painterResource(id = it),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun JoinWalletQRScreenPreview() {
    val viewModel = AddDeviceViewModel()
    val joinRequestData = JoinRequestData("8bcc27a9-6646-4300-86d1-62815ebe9e7a", AddDeviceViewModel.Platform.ANDROID, "xxx@gmail.com")
    viewModel.updateJoinRequestData(joinRequestData)
    FireblocksNCWDemoTheme {
        JoinWalletQRScreen(viewModel = viewModel)
    }
}
