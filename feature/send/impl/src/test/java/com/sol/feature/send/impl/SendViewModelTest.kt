package com.sol.feature.send.impl

import com.sol.core.wallet.WalletSessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sessionManager: WalletSessionManager
    private lateinit var viewModel: SendViewModel

    @Before
    fun setUp() {
        sessionManager = mockk(relaxed = true)
        viewModel = SendViewModel(sessionManager)
    }

    private fun firstMessage(): SendMessage = runBlocking {
        withTimeout(3_000) { viewModel.messages.first() }
    }

    @Test
    fun `onRecipientChange updates state`() {
        viewModel.onRecipientChange("recipient-address")
        assertEquals("recipient-address", viewModel.state.value.recipient)
    }

    @Test
    fun `onAmountChange updates state`() {
        viewModel.onAmountChange("1.5")
        assertEquals("1.5", viewModel.state.value.amountSol)
    }

    @Test
    fun `send with empty amount emits BadAmount and does not transfer`() {
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("")

        viewModel.send()

        assertEquals(SendMessage.BadAmount, firstMessage())
        verify(exactly = 0) { sessionManager.transfer(any(), any()) }
    }

    @Test
    fun `send with zero amount emits BadAmount`() {
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("0")

        viewModel.send()

        assertEquals(SendMessage.BadAmount, firstMessage())
        verify(exactly = 0) { sessionManager.transfer(any(), any()) }
    }

    @Test
    fun `send with non-numeric amount emits BadAmount`() {
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("abc")

        viewModel.send()

        assertEquals(SendMessage.BadAmount, firstMessage())
        verify(exactly = 0) { sessionManager.transfer(any(), any()) }
    }

    @Test
    fun `send success emits Success and resets the form`() {
        every { sessionManager.transfer("recipient", 1_500_000_000L) } returns Result.success("sig123")
        viewModel.onRecipientChange("  recipient  ")
        viewModel.onAmountChange(" 1.5 ")

        viewModel.send()

        assertEquals(SendMessage.Success, firstMessage())
        val state = viewModel.state.value
        assertEquals("sig123", state.lastSignature)
        assertEquals("", state.recipient)
        assertEquals("", state.amountSol)
        assertFalse(state.isSending)
        verify { sessionManager.transfer("recipient", 1_500_000_000L) }
    }

    @Test
    fun `send failure with message emits Plain`() {
        every { sessionManager.transfer(any(), any()) } returns Result.failure(RuntimeException("insufficient funds"))
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1")

        viewModel.send()

        assertEquals(SendMessage.Plain("insufficient funds"), firstMessage())
        assertFalse(viewModel.state.value.isSending)
        assertNull(viewModel.state.value.lastSignature)
    }

    @Test
    fun `send failure with blank message emits Failed`() {
        every { sessionManager.transfer(any(), any()) } returns Result.failure(RuntimeException(""))
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1")

        viewModel.send()

        assertEquals(SendMessage.Failed, firstMessage())
        assertFalse(viewModel.state.value.isSending)
    }
}
