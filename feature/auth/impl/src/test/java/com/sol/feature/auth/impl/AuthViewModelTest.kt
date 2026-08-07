package com.sol.feature.auth.impl

import com.sol.core.wallet.ImportResult
import com.sol.core.wallet.WalletSessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sessionManager: WalletSessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        sessionManager = mockk(relaxed = true)
        viewModel = AuthViewModel(sessionManager)
    }

    private fun <T> await(timeoutMs: Long = 3_000, block: suspend () -> T): T = runBlocking {
        withTimeout(timeoutMs) { block() }
    }

    @Test
    fun `onImportTextChange updates state`() {
        viewModel.onImportTextChange("word1 word2")
        assertEquals("word1 word2", viewModel.state.value.importText)
    }

    @Test
    fun `clearError resets error`() {
        every { sessionManager.importWallet(any()) } returns ImportResult.InvalidMnemonic
        viewModel.onImportTextChange("bad")
        viewModel.importWallet()
        await { viewModel.state.first { it.error != null } }

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `createWallet exposes generated mnemonic`() {
        val mnemonic = listOf("alpha", "beta", "gamma")
        every { sessionManager.createWallet() } returns mnemonic

        viewModel.createWallet()

        val state = await { viewModel.state.first { it.mnemonic != null } }
        assertEquals(mnemonic, state.mnemonic)
        assertNull(state.error)
    }

    @Test
    fun `createWallet failure surfaces error`() {
        every { sessionManager.createWallet() } throws RuntimeException("keystore error")

        viewModel.createWallet()

        val state = await { viewModel.state.first { it.error != null } }
        assertEquals(AuthError.Plain("keystore error"), state.error)
    }

    @Test
    fun `createWallet failure with null message uses class name`() {
        every { sessionManager.createWallet() } throws RuntimeException()

        viewModel.createWallet()

        val state = await { viewModel.state.first { it.error != null } }
        assertEquals(AuthError.Plain("RuntimeException"), state.error)
    }

    @Test
    fun `confirmMnemonicSaved clears mnemonic and authenticates`() {
        val mnemonic = listOf("alpha", "beta")
        every { sessionManager.createWallet() } returns mnemonic
        viewModel.createWallet()
        await { viewModel.state.first { it.mnemonic != null } }

        viewModel.confirmMnemonicSaved()

        await { viewModel.authenticated.first() }
        assertNull(viewModel.state.value.mnemonic)
    }

    @Test
    fun `importWallet success clears text and authenticates`() {
        every { sessionManager.importWallet(any()) } returns ImportResult.Success
        viewModel.onImportTextChange("  word1   word2  word3 ")

        viewModel.importWallet()

        await { viewModel.authenticated.first() }
        assertEquals("", viewModel.state.value.importText)
        assertNull(viewModel.state.value.error)
        verify { sessionManager.importWallet(listOf("word1", "word2", "word3")) }
    }

    @Test
    fun `importWallet invalid mnemonic sets InvalidMnemonic error`() {
        every { sessionManager.importWallet(any()) } returns ImportResult.InvalidMnemonic
        viewModel.onImportTextChange("bad words")

        viewModel.importWallet()

        val state = await { viewModel.state.first { it.error != null } }
        assertEquals(AuthError.InvalidMnemonic, state.error)
    }

    @Test
    fun `importWallet error result sets Plain error`() {
        every { sessionManager.importWallet(any()) } returns ImportResult.Error("disk full")
        viewModel.onImportTextChange("some words")

        viewModel.importWallet()

        val state = await { viewModel.state.first { it.error != null } }
        assertEquals(AuthError.Plain("disk full"), state.error)
    }
}
