package com.sol.feature.receive.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReceiveScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val publicKey = "PubKey1111111111111111111111111111111111111"

    private fun fakeViewModel(key: String = publicKey): ReceiveViewModel =
        mockk<ReceiveViewModel>().also { viewModel ->
            every { viewModel.publicKey } returns MutableStateFlow(key)
        }

    @Test
    fun receiveScreen_displaysTitleBodyAndPublicKey() {
        composeTestRule.setContent {
            ReceiveScreen(
                onBack = {},
                onShowMessage = {},
                viewModel = fakeViewModel(),
            )
        }

        val context = composeTestRule.activity
        composeTestRule.onNodeWithText(context.getString(R.string.receive_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.receive_body)).assertIsDisplayed()
        composeTestRule.onNodeWithText(publicKey).assertIsDisplayed()
    }

    @Test
    fun receiveScreen_clickingBack_invokesOnBack() {
        var backClicked = false
        composeTestRule.setContent {
            ReceiveScreen(
                onBack = { backClicked = true },
                onShowMessage = {},
                viewModel = fakeViewModel(),
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.receive_back))
            .performClick()

        assertTrue(backClicked)
    }

    @Test
    fun receiveScreen_clickingCopy_showsCopiedMessage() {
        var shownMessage: String? = null
        composeTestRule.setContent {
            ReceiveScreen(
                onBack = {},
                onShowMessage = { shownMessage = it },
                viewModel = fakeViewModel(),
            )
        }

        val context = composeTestRule.activity
        composeTestRule
            .onNodeWithText(context.getString(R.string.receive_copy))
            .performClick()

        assertEquals(context.getString(R.string.receive_copied), shownMessage)
    }

    @Test
    fun receiveScreen_withoutInteraction_doesNotShowMessage() {
        var shownMessage: String? = null
        composeTestRule.setContent {
            ReceiveScreen(
                onBack = {},
                onShowMessage = { shownMessage = it },
                viewModel = fakeViewModel(),
            )
        }

        assertNull(shownMessage)
    }
}
