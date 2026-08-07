package com.sol.feature.receive.impl

import app.cash.turbine.test
import com.sol.core.wallet.WalletSession
import com.sol.core.wallet.WalletSessionManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReceiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val publicKey = "PubKey1111111111111111111111111111111111111"

    private fun viewModelWith(session: MutableStateFlow<WalletSession>): ReceiveViewModel {
        val sessionManager = mockk<WalletSessionManager>()
        every { sessionManager.session } returns session
        return ReceiveViewModel(sessionManager)
    }

    @Test
    fun `publicKey initial value reflects the session`() {
        val session = MutableStateFlow(WalletSession(hasWallet = true, publicKey = publicKey))
        val viewModel = viewModelWith(session)

        assertEquals(publicKey, viewModel.publicKey.value)
    }

    @Test
    fun `publicKey is empty string when session has no key`() {
        val session = MutableStateFlow(WalletSession(hasWallet = false, publicKey = null))
        val viewModel = viewModelWith(session)

        assertEquals("", viewModel.publicKey.value)
    }

    @Test
    fun `publicKey emits updates from the session`() = runBlocking {
        val session = MutableStateFlow(WalletSession(hasWallet = false, publicKey = null))
        val viewModel = viewModelWith(session)

        viewModel.publicKey.test {
            assertEquals("", awaitItem())
            session.value = WalletSession(hasWallet = true, publicKey = publicKey)
            assertEquals(publicKey, awaitItem())
        }
    }
}
