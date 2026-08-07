package com.sol.feature.send.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.sol.cryprowallet.wallet.ui.HomeScreen

class HomeScreenScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun HomeScreenPreviewTest(){
        HomeScreen(
            publicKey = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
            balanceLamports = 1000000000,
            isLoadingBalance = false,
            onRefresh = {},
            onSend = {},
            onReceive = {},
            onShowMessage = {},
            onDelete = {},
        )
    }
}