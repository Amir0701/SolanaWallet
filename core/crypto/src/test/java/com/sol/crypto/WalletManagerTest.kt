package com.sol.crypto

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WalletManagerTest {

    private lateinit var secureStorage: SecureStorage
    private lateinit var walletManager: WalletManager

    // Well-known valid 12-word BIP39 test mnemonic.
    private val validMnemonic = listOf(
        "abandon", "abandon", "abandon", "abandon", "abandon", "abandon",
        "abandon", "abandon", "abandon", "abandon", "abandon", "about",
    )

    @Before
    fun setUp() {
        secureStorage = mockk(relaxed = true)
        walletManager = WalletManager(secureStorage)
    }

    @Test
    fun `createWallet returns 12 word mnemonic and public key`() {
        every { secureStorage.saveMnemonic(any()) } returns Unit

        val (wallet, mnemonic) = walletManager.createWallet()

        assertEquals(12, mnemonic.size)
        assertTrue(wallet.publicKey.isNotBlank())
    }

    @Test
    fun `createWallet persists the generated mnemonic`() {
        val saved = slot<List<String>>()
        every { secureStorage.saveMnemonic(capture(saved)) } returns Unit

        val (_, mnemonic) = walletManager.createWallet()

        verify(exactly = 1) { secureStorage.saveMnemonic(any()) }
        assertEquals(mnemonic, saved.captured)
    }

    @Test
    fun `createWallet generates a unique mnemonic each call`() {
        every { secureStorage.saveMnemonic(any()) } returns Unit

        val (_, first) = walletManager.createWallet()
        val (_, second) = walletManager.createWallet()

        assertNotEquals(first, second)
    }

    @Test
    fun `importWallet accepts a valid mnemonic and persists it`() {
        every { secureStorage.saveMnemonic(any()) } returns Unit

        val wallet = walletManager.importWallet(validMnemonic)

        assertTrue(wallet.publicKey.isNotBlank())
        verify(exactly = 1) { secureStorage.saveMnemonic(validMnemonic) }
    }

    @Test
    fun `importWallet derives a deterministic public key`() {
        every { secureStorage.saveMnemonic(any()) } returns Unit

        val first = walletManager.importWallet(validMnemonic).publicKey
        val second = walletManager.importWallet(validMnemonic).publicKey

        assertEquals(first, second)
    }

    @Test
    fun `importWallet rejects invalid mnemonic and does not persist`() {
        val invalid = List(12) { "zzzz" }

        assertThrows(InvalidMnemonicException::class.java) {
            walletManager.importWallet(invalid)
        }
        verify(exactly = 0) { secureStorage.saveMnemonic(any()) }
    }

    @Test
    fun `importWallet rejects mnemonic with bad checksum`() {
        // 12 valid words but invalid checksum.
        val badChecksum = List(12) { "abandon" }

        assertThrows(InvalidMnemonicException::class.java) {
            walletManager.importWallet(badChecksum)
        }
    }

    @Test
    fun `hasWallet returns true when storage has mnemonic`() {
        every { secureStorage.getMnemonic() } returns validMnemonic
        assertTrue(walletManager.hasWallet())
    }

    @Test
    fun `hasWallet returns false when storage is empty`() {
        every { secureStorage.getMnemonic() } returns null
        assertFalse(walletManager.hasWallet())
    }

    @Test
    fun `getWallet returns null when no mnemonic stored`() {
        every { secureStorage.getMnemonic() } returns null
        assertNull(walletManager.getWallet())
    }

    @Test
    fun `getWallet derives public key from stored mnemonic`() {
        every { secureStorage.getMnemonic() } returns validMnemonic
        every { secureStorage.saveMnemonic(any()) } returns Unit

        val fromStorage = walletManager.getWallet()
        val fromImport = walletManager.importWallet(validMnemonic)

        assertNotNull(fromStorage)
        assertEquals(fromImport.publicKey, fromStorage!!.publicKey)
    }

    @Test
    fun `getSignerAccount returns null when no mnemonic stored`() {
        every { secureStorage.getMnemonic() } returns null
        assertNull(walletManager.getSignerAccount())
    }

    @Test
    fun `getSignerAccount matches derived wallet public key`() {
        every { secureStorage.getMnemonic() } returns validMnemonic

        val signer = walletManager.getSignerAccount()
        val wallet = walletManager.getWallet()

        assertNotNull(signer)
        assertEquals(wallet!!.publicKey, signer!!.publicKey.toBase58())
    }

    @Test
    fun `clearWallet delegates to storage`() {
        every { secureStorage.clearMnemonic() } returns Unit

        walletManager.clearWallet()

        verify(exactly = 1) { secureStorage.clearMnemonic() }
    }

    private fun assertNotEquals(expected: Any?, actual: Any?) {
        assertFalse("Expected values to differ but both were <$actual>", expected == actual)
    }
}
