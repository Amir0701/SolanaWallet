package com.sol.feature.send.impl.presentation

import app.cash.turbine.test
import com.sol.feature.send.impl.MainDispatcherRule
import com.sol.feature.send.impl.domain.model.SendSolResult
import com.sol.feature.send.impl.domain.usecase.SendSolUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendSol: SendSolUseCase = mockk()
    private val viewModel = SendViewModel(sendSol)

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
    fun `send passes form values to use case`() = runTest {
        coEvery { sendSol(any(), any()) } returns SendSolResult.Success("sig")
        viewModel.onRecipientChange("  recipient  ")
        viewModel.onAmountChange(" 1.5 ")

        viewModel.send()

        coVerify(exactly = 1) { sendSol("  recipient  ", " 1.5 ") }
    }

    @Test
    fun `InvalidAmount emits BadAmount and keeps the form`() = runTest {
        coEvery { sendSol(any(), any()) } returns SendSolResult.InvalidAmount
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("abc")

        viewModel.messages.test {
            viewModel.send()
            assertEquals(SendMessage.BadAmount, awaitItem())
        }
        val state = viewModel.state.value
        assertFalse(state.isSending)
        assertEquals("recipient", state.recipient)
        assertEquals("abc", state.amountSol)
    }

    @Test
    fun `Success emits Success and resets the form`() = runTest {
        coEvery { sendSol("recipient", "1.5") } returns SendSolResult.Success("sig123")
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1.5")

        viewModel.messages.test {
            viewModel.send()
            assertEquals(SendMessage.Success, awaitItem())
        }
        assertEquals(SendViewModel.UiState(lastSignature = "sig123"), viewModel.state.value)
    }

    @Test
    fun `Failed with reason emits Plain`() = runTest {
        coEvery { sendSol(any(), any()) } returns SendSolResult.Failed("insufficient funds")
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1")

        viewModel.messages.test {
            viewModel.send()
            assertEquals(SendMessage.Plain("insufficient funds"), awaitItem())
        }
        assertFalse(viewModel.state.value.isSending)
        assertNull(viewModel.state.value.lastSignature)
    }

    @Test
    fun `Failed without reason emits Failed`() = runTest {
        coEvery { sendSol(any(), any()) } returns SendSolResult.Failed(null)
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1")

        viewModel.messages.test {
            viewModel.send()
            assertEquals(SendMessage.Failed, awaitItem())
        }
        assertFalse(viewModel.state.value.isSending)
    }
}
