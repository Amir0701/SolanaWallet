package com.sol.cryprowallet.wallet

import com.sol.core.wallet.WalletSession
import com.sol.core.wallet.WalletSessionManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletAppViewModelTest {

    private val session = MutableStateFlow(WalletSession())

    private lateinit var sessionManager: WalletSessionManager

    private fun build(initial: WalletSession = WalletSession()): WalletAppViewModel {
        session.value = initial
        sessionManager = mockk(relaxed = true)
        every { sessionManager.session } returns session
        every { sessionManager.toggleNetwork() } just Runs
        every { sessionManager.deleteWallet() } just Runs
        return WalletAppViewModel(sessionManager)
    }

    @Test
    fun `session exposes manager session flow`() {
        val viewModel = build()
        assertSame(session, viewModel.session)
    }

    @Test
    fun `hasWalletInitially is true when session starts with a wallet`() {
        val viewModel = build(WalletSession(hasWallet = true))
        assertTrue(viewModel.hasWalletInitially)
    }

    @Test
    fun `hasWalletInitially is false when session starts empty`() {
        val viewModel = build(WalletSession(hasWallet = false))
        assertFalse(viewModel.hasWalletInitially)
    }

    @Test
    fun `toggleNetwork delegates to session manager`() {
        val viewModel = build()
        viewModel.toggleNetwork()
        verify(exactly = 1) { sessionManager.toggleNetwork() }
    }

    @Test
    fun `deleteWallet delegates to session manager`() {
        val viewModel = build()
        viewModel.deleteWallet()
        verify(exactly = 1) { sessionManager.deleteWallet() }
    }
}
