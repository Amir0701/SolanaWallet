package com.sol.feature.receive.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

class ReceiveScreenScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun ReceiveScreenPreviewTest() {
        ReceiveContent(
            publicKey = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
            onBack = {},
            onCopy = {},
        )
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun ReceiveScreenEmptyKeyPreviewTest() {
        ReceiveContent(
            publicKey = "",
            onBack = {},
            onCopy = {},
        )
    }
}
