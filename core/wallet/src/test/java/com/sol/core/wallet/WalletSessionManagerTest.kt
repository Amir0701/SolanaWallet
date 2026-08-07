package com.sol.core.wallet

import com.sol.crypto.InvalidMnemonicException
import com.sol.crypto.Wallet
import com.sol.crypto.WalletManager
import com.sol.network.SolanaCluster
import com.sol.network.SolanaRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.p2p.solanaj.core.Account

class WalletSessionManagerTest {

    private lateinit var walletManager: WalletManager
    private lateinit var repositoryFactory: SolanaRepository.Factory
    private lateinit var repository: SolanaRepository

    private val publicKey = "PublicKey111111111111111111111111111111111"

    @Before
    fun setUp() {
        walletManager = mockk(relaxed = true)
        repositoryFactory = mockk()
        repository = mockk()
        // Defaults for the init block of the manager (no wallet yet).
        every { walletManager.hasWallet() } returns false
        every { walletManager.getWallet() } returns null
        every { repositoryFactory.create(any()) } returns repository
    }

    private fun newManager() = WalletSessionManager(walletManager, repositoryFactory)

    @Test
    fun `initial session reflects existing wallet`() {
        every { walletManager.hasWallet() } returns true
        every { walletManager.getWallet() } returns Wallet(publicKey)

        val manager = newManager()

        val session = manager.session.value
        assertTrue(session.hasWallet)
        assertEquals(publicKey, session.publicKey)
        assertTrue(session.useDevnet)
    }

    @Test
    fun `initial session is empty when no wallet`() {
        val manager = newManager()

        val session = manager.session.value
        assertFalse(session.hasWallet)
        assertNull(session.publicKey)
    }

    @Test
    fun `toggleNetwork flips devnet flag`() {
        val manager = newManager()
        assertTrue(manager.session.value.useDevnet)

        manager.toggleNetwork()
        assertFalse(manager.session.value.useDevnet)

        manager.toggleNetwork()
        assertTrue(manager.session.value.useDevnet)
    }

    @Test
    fun `createWallet updates session and returns mnemonic`() {
        val mnemonic = listOf("word1", "word2", "word3")
        every { walletManager.createWallet() } returns (Wallet(publicKey) to mnemonic)

        val manager = newManager()
        val result = manager.createWallet()

        assertEquals(mnemonic, result)
        assertTrue(manager.session.value.hasWallet)
        assertEquals(publicKey, manager.session.value.publicKey)
    }

    @Test
    fun `importWallet success updates session`() {
        every { walletManager.importWallet(any()) } returns Wallet(publicKey)

        val manager = newManager()
        val result = manager.importWallet(listOf("a", "b"))

        assertEquals(ImportResult.Success, result)
        assertTrue(manager.session.value.hasWallet)
        assertEquals(publicKey, manager.session.value.publicKey)
    }

    @Test
    fun `importWallet maps invalid mnemonic exception`() {
        every { walletManager.importWallet(any()) } throws InvalidMnemonicException()

        val manager = newManager()
        val result = manager.importWallet(listOf("bad"))

        assertEquals(ImportResult.InvalidMnemonic, result)
        assertFalse(manager.session.value.hasWallet)
    }

    @Test
    fun `importWallet maps generic exception to Error with message`() {
        every { walletManager.importWallet(any()) } throws RuntimeException("boom")

        val manager = newManager()
        val result = manager.importWallet(listOf("x"))

        assertEquals(ImportResult.Error("boom"), result)
    }

    @Test
    fun `importWallet uses class name when exception message is null`() {
        every { walletManager.importWallet(any()) } throws RuntimeException()

        val manager = newManager()
        val result = manager.importWallet(listOf("x"))

        assertEquals(ImportResult.Error("RuntimeException"), result)
    }

    @Test
    fun `deleteWallet clears keys and resets session`() {
        every { walletManager.hasWallet() } returns true
        every { walletManager.getWallet() } returns Wallet(publicKey)

        val manager = newManager()
        manager.deleteWallet()

        verify { walletManager.clearWallet() }
        assertFalse(manager.session.value.hasWallet)
        assertNull(manager.session.value.publicKey)
    }

    @Test
    fun `getBalanceLamports fails when no wallet`() {
        val manager = newManager()

        val result = manager.getBalanceLamports()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `getBalanceLamports uses devnet endpoint by default`() {
        every { walletManager.hasWallet() } returns true
        every { walletManager.getWallet() } returns Wallet(publicKey)
        every { repository.getBalanceLamports(publicKey) } returns Result.success(42L)

        val manager = newManager()
        val result = manager.getBalanceLamports()

        assertEquals(42L, result.getOrNull())
        verify { repositoryFactory.create(SolanaCluster.DEVNET_RPC) }
        verify { repository.getBalanceLamports(publicKey) }
    }

    @Test
    fun `getBalanceLamports uses mainnet endpoint after toggle`() {
        every { walletManager.hasWallet() } returns true
        every { walletManager.getWallet() } returns Wallet(publicKey)
        every { repository.getBalanceLamports(publicKey) } returns Result.success(7L)

        val manager = newManager()
        manager.toggleNetwork()
        manager.getBalanceLamports()

        verify { repositoryFactory.create(SolanaCluster.MAINNET_RPC) }
    }

    @Test
    fun `getBalanceLamports propagates repository failure`() {
        every { walletManager.hasWallet() } returns true
        every { walletManager.getWallet() } returns Wallet(publicKey)
        val error = RuntimeException("rpc down")
        every { repository.getBalanceLamports(publicKey) } returns Result.failure(error)

        val manager = newManager()
        val result = manager.getBalanceLamports()

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `transfer fails when no signer available`() {
        every { walletManager.getSignerAccount() } returns null

        val manager = newManager()
        val result = manager.transfer("recipient", 100L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `transfer delegates to repository with signer`() {
        val signer = mockk<Account>()
        every { walletManager.getSignerAccount() } returns signer
        every { repository.transferSol(signer, "recipient", 100L) } returns Result.success("signature")

        val manager = newManager()
        val result = manager.transfer("recipient", 100L)

        assertEquals("signature", result.getOrNull())
        verify { repository.transferSol(signer, "recipient", 100L) }
        verify { repositoryFactory.create(SolanaCluster.DEVNET_RPC) }
    }

    @Test
    fun `transfer propagates repository failure`() {
        val signer = mockk<Account>()
        every { walletManager.getSignerAccount() } returns signer
        val error = RuntimeException("send failed")
        every { repository.transferSol(any(), any(), any()) } returns Result.failure(error)

        val manager = newManager()
        val result = manager.transfer("recipient", 100L)

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }
}
