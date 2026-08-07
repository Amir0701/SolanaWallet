package com.sol.cryprowallet.wallet.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sol.cryprowallet.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val publicKey = "PubKey1111111111111111111111111111111111111"

    private fun setHomeScreen(
        publicKey: String = this.publicKey,
        balanceLamports: Long? = 1_500_000_000L,
        isLoadingBalance: Boolean = false,
        onRefresh: () -> Unit = {},
        onSend: () -> Unit = {},
        onReceive: () -> Unit = {},
        onShowMessage: (String) -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            HomeScreen(
                publicKey = publicKey,
                balanceLamports = balanceLamports,
                isLoadingBalance = isLoadingBalance,
                onRefresh = onRefresh,
                onSend = onSend,
                onReceive = onReceive,
                onShowMessage = onShowMessage,
                onDelete = onDelete,
            )
        }
    }

    @Test
    fun homeScreen_displaysFormattedBalanceAndAddress() {
        setHomeScreen(balanceLamports = 1_500_000_000L)

        composeTestRule.onNodeWithText("1.5 SOL").assertIsDisplayed()
        composeTestRule.onNodeWithText(publicKey).assertIsDisplayed()
    }

    @Test
    fun homeScreen_nullBalance_showsDash() {
        setHomeScreen(balanceLamports = null, isLoadingBalance = false)

        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }

    @Test
    fun homeScreen_loadingBalance_hidesAmount() {
        setHomeScreen(balanceLamports = 1_500_000_000L, isLoadingBalance = true)

        composeTestRule.onNodeWithText("1.5 SOL").assertDoesNotExist()
        composeTestRule.onNodeWithText("—").assertDoesNotExist()
    }

    @Test
    fun homeScreen_clickingRefresh_invokesOnRefresh() {
        var refreshed = false
        setHomeScreen(onRefresh = { refreshed = true })

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.refresh))
            .performClick()

        assertTrue(refreshed)
    }

    @Test
    fun homeScreen_clickingCopy_showsCopiedMessage() {
        var shownMessage: String? = null
        setHomeScreen(onShowMessage = { shownMessage = it })

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.copy))
            .performClick()

        assertEquals(composeTestRule.activity.getString(R.string.copied), shownMessage)
    }

    @Test
    fun homeScreen_clickingSend_invokesOnSend() {
        var sent = false
        setHomeScreen(onSend = { sent = true })

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.send))
            .performClick()

        assertTrue(sent)
    }

    @Test
    fun homeScreen_clickingReceive_invokesOnReceive() {
        var received = false
        setHomeScreen(onReceive = { received = true })

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.receive))
            .performClick()

        assertTrue(received)
    }

    @Test
    fun homeScreen_clickingDelete_invokesOnDelete() {
        var deleted = false
        setHomeScreen(onDelete = { deleted = true })

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.delete_wallet))
            .performClick()

        assertTrue(deleted)
    }
}
