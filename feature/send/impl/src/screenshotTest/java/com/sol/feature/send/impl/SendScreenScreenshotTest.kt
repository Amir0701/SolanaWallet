package com.sol.feature.send.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

class SendScreenScreenshotTest {
    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun SendScreenPreviewTest(){
        SendScreenContent(
            state = SendViewModel.UiState(),
            onBack = {},
            onRecipientChange = {},
            onAmountChange = {},
            send = {}
        )
    }
}