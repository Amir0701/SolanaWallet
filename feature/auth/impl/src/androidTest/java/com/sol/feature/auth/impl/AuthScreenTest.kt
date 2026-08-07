package com.sol.feature.auth.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun fakeViewModel(
        state: AuthViewModel.UiState = AuthViewModel.UiState(),
        authenticated: kotlinx.coroutines.flow.Flow<Unit> = flowOf(),
    ): AuthViewModel = mockk<AuthViewModel>(relaxed = true).also { viewModel ->
        every { viewModel.state } returns MutableStateFlow(state)
        every { viewModel.authenticated } returns authenticated
    }

    @Test
    fun authScreen_displaysOnboardingContent() {
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = {}, viewModel = fakeViewModel())
        }

        val context = composeTestRule.activity
        composeTestRule.onNodeWithText(context.getString(R.string.auth_welcome_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.auth_welcome_subtitle)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.auth_create_wallet)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.auth_import_action)).assertIsDisplayed()
    }

    @Test
    fun authScreen_clickingCreate_invokesCreateWallet() {
        val viewModel = fakeViewModel()
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = {}, viewModel = viewModel)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.auth_create_wallet))
            .performClick()

        verify { viewModel.createWallet() }
    }

    @Test
    fun authScreen_typingImportText_invokesOnImportTextChange() {
        val viewModel = fakeViewModel(state = AuthViewModel.UiState(importText = "seed words here"))
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = {}, viewModel = viewModel)
        }

        composeTestRule
            .onNodeWithText("seed words here")
            .performTextInput("more")

        verify { viewModel.onImportTextChange(any()) }
    }

    @Test
    fun authScreen_clickingImport_invokesImportWallet() {
        val viewModel = fakeViewModel()
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = {}, viewModel = viewModel)
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.auth_import_action))
            .performClick()

        verify { viewModel.importWallet() }
    }

    @Test
    fun authScreen_invalidMnemonicError_showsMessageAndClearsError() {
        val viewModel = fakeViewModel(state = AuthViewModel.UiState(error = AuthError.InvalidMnemonic))
        var shownMessage: String? = null
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = { shownMessage = it }, viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        assertEquals(
            composeTestRule.activity.getString(R.string.auth_error_invalid_mnemonic),
            shownMessage,
        )
        verify { viewModel.clearError() }
    }

    @Test
    fun authScreen_plainError_showsRawMessage() {
        val viewModel = fakeViewModel(state = AuthViewModel.UiState(error = AuthError.Plain("boom")))
        var shownMessage: String? = null
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = { shownMessage = it }, viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        assertEquals("boom", shownMessage)
    }

    @Test
    fun authScreen_withMnemonic_showsDialogAndConfirms() {
        val viewModel = fakeViewModel(
            state = AuthViewModel.UiState(mnemonic = listOf("alpha", "bravo", "charlie")),
        )
        composeTestRule.setContent {
            AuthScreen(onAuthenticated = {}, onShowMessage = {}, viewModel = viewModel)
        }

        val context = composeTestRule.activity
        composeTestRule.onNodeWithText(context.getString(R.string.auth_mnemonic_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.auth_mnemonic_body)).assertIsDisplayed()
        composeTestRule.onNodeWithText("alpha bravo charlie").assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.auth_mnemonic_confirm))
            .performClick()

        verify { viewModel.confirmMnemonicSaved() }
    }

    @Test
    fun authScreen_authenticatedEmission_invokesOnAuthenticated() {
        var authenticated = false
        composeTestRule.setContent {
            AuthScreen(
                onAuthenticated = { authenticated = true },
                onShowMessage = {},
                viewModel = fakeViewModel(authenticated = flowOf(Unit)),
            )
        }

        composeTestRule.waitForIdle()

        assertTrue(authenticated)
    }
}
