package com.sol.cryprowallet.wallet

import com.sol.core.wallet.WalletSession
import com.sol.core.wallet.WalletSessionManager
import com.sol.cryprowallet.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val publicKey = "PubKey1111111111111111111111111111111111111"

    private lateinit var sessionManager: WalletSessionManager

    private fun sessionFlow(session: WalletSession) = MutableStateFlow(session).also {
        sessionManager = mockk(relaxed = true)
        every { sessionManager.session } returns it
    }

    private fun awaitState(predicate: (HomeViewModel.UiState) -> Boolean): HomeViewModel.UiState =
        runBlocking { withTimeout(3_000) { viewModelState.first(predicate) } }

    private lateinit var viewModelState: kotlinx.coroutines.flow.StateFlow<HomeViewModel.UiState>

    @Test
    fun `init loads balance for existing wallet`() {
        sessionFlow(WalletSession(hasWallet = true, publicKey = publicKey))
        every { sessionManager.getBalanceLamports() } returns Result.success(2_000_000_000L)

        val viewModel = HomeViewModel(sessionManager)
        viewModelState = viewModel.state

        val state = awaitState { it.balanceLamports != null }
        assertEquals(publicKey, state.publicKey)
        assertEquals(2_000_000_000L, state.balanceLamports)
        assertFalse(state.isLoadingBalance)
    }

    @Test
    fun `init does not load balance when no wallet`() {
        sessionFlow(WalletSession(hasWallet = false, publicKey = null))

        val viewModel = HomeViewModel(sessionManager)
        viewModelState = viewModel.state

        assertNull(viewModel.state.value.publicKey)
        assertNull(viewModel.state.value.balanceLamports)
        assertFalse(viewModel.state.value.isLoadingBalance)
        verify(exactly = 0) { sessionManager.getBalanceLamports() }
    }

    @Test
    fun `loadBalance keeps balance null on failure and stops loading`() {
        sessionFlow(WalletSession(hasWallet = true, publicKey = publicKey))
        every { sessionManager.getBalanceLamports() } returns Result.failure(RuntimeException("rpc"))

        val viewModel = HomeViewModel(sessionManager)
        viewModelState = viewModel.state

        // The RPC was triggered (loading flag set true beforehand)...
        verify(timeout = 3_000) { sessionManager.getBalanceLamports() }
        // ...and once it returns, loading settles back to false with no balance.
        val state = awaitState { !it.isLoadingBalance }
        assertFalse(state.isLoadingBalance)
        assertNull(state.balanceLamports)
    }

    @Test
    fun `manual loadBalance refreshes the balance`() {
        sessionFlow(WalletSession(hasWallet = true, publicKey = publicKey))
        every { sessionManager.getBalanceLamports() } returnsMany listOf(
            Result.success(1L),
            Result.success(5L),
        )

        val viewModel = HomeViewModel(sessionManager)
        viewModelState = viewModel.state
        awaitState { it.balanceLamports == 1L }

        viewModel.loadBalance()

        val state = awaitState { it.balanceLamports == 5L }
        assertEquals(5L, state.balanceLamports)
    }
}
