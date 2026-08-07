package com.sol.feature.auth.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

class AuthScreenScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun AuthScreenScreenshotTest(){
        OnboardingContent(
            importText = "Импортировать",
            onImportTextChange = {},
            onCreate = {},
            onImport = {},
        )
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun AuthModalSheet(){
        MnemonicSavedDialog(
            mnemonic = listOf("test, banana, apple"),
            onConfirm = {},
        )
    }
}
