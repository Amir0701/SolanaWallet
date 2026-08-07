package com.sol.feature.receive.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sol.feature.send.impl.R
import com.sol.feature.send.impl.SendMessage
import com.sol.feature.send.impl.SendScreen
import com.sol.feature.send.impl.SendViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SendScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel: SendViewModel = mockk<SendViewModel>(relaxed = true).apply {
        every { state } returns MutableStateFlow(SendViewModel.UiState())
    }

    @Test
    fun checkSuccessSendingSol(){
        val context = composeTestRule.activity
        var showMessage: String? = null

        every { viewModel.messages } returns flowOf(SendMessage.Success)

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = { showMessage = it },
                viewModel = viewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_recipient))
            .performTextInput("323rdsadlk12")

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_amount_sol))
            .performTextInput("1000")

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_confirm))
            .performClick()

        assertEquals(context.getString(R.string.send_success), showMessage)
    }

    @Test
    fun checkBadAmountMessage(){
        val context = composeTestRule.activity
        var showMessage: String? = null

        every { viewModel.messages } returns flowOf(SendMessage.BadAmount)

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = { showMessage = it },
                viewModel = viewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_recipient))
            .performTextInput("dsfvsdfvs34r34r")

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_amount_sol))
            .performTextInput("10..00")

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_confirm))
            .performClick()

        assertEquals(context.getString(R.string.send_error_bad_amount), showMessage)
    }

    @Test
    fun checkOnBackClick(){
        val context = composeTestRule.activity
        var onBackClick = false

        every { viewModel.messages } returns flowOf()

        composeTestRule.setContent {
            SendScreen(
                onBack = { onBackClick = true },
                onShowMessage = {},
                viewModel = viewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_back))
            .performClick()

        assertTrue(onBackClick)
    }

    @Test
    fun checkFailedMessage() {
        val context = composeTestRule.activity
        var showMessage: String? = null

        every { viewModel.messages } returns flowOf(SendMessage.Failed)

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = { showMessage = it },
                viewModel = viewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_confirm))
            .performClick()

        assertEquals(context.getString(R.string.send_error_failed), showMessage)
    }

    @Test
    fun checkPlainMessageShowsRawText() {
        var showMessage: String? = null

        every { viewModel.messages } returns flowOf(SendMessage.Plain("insufficient funds"))

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = { showMessage = it },
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        assertEquals("insufficient funds", showMessage)
    }

    @Test
    fun checkSendingStateHidesConfirmButton() {
        val context = composeTestRule.activity
        val sendingViewModel = mockk<SendViewModel>(relaxed = true).apply {
            every { state } returns MutableStateFlow(SendViewModel.UiState(isSending = true))
            every { messages } returns flowOf()
        }

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = {},
                viewModel = sendingViewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_confirm))
            .assertDoesNotExist()
    }

    @Test
    fun checkLastSignatureIsDisplayed() {
        val signature = "5xSignature11111111111111111111111111111111"
        val signatureViewModel = mockk<SendViewModel>(relaxed = true).apply {
            every { state } returns MutableStateFlow(SendViewModel.UiState(lastSignature = signature))
            every { messages } returns flowOf()
        }

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = {},
                viewModel = signatureViewModel,
            )
        }

        composeTestRule.onNodeWithText(signature).assertIsDisplayed()
    }

    @Test
    fun checkRecipientAndAmountInputForwardedToViewModel() {
        val context = composeTestRule.activity

        every { viewModel.messages } returns flowOf()

        composeTestRule.setContent {
            SendScreen(
                onBack = {},
                onShowMessage = {},
                viewModel = viewModel,
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_recipient))
            .performTextInput("RecipientAddress123")

        composeTestRule
            .onNodeWithText(context.getString(R.string.send_amount_sol))
            .performTextInput("2.5")

        verify { viewModel.onRecipientChange(any()) }
        verify { viewModel.onAmountChange(any()) }
    }
}
