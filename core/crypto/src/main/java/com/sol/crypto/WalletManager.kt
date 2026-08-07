package com.sol.crypto

import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.p2p.solanaj.core.Account
import java.security.SecureRandom

/** Доменное исключение: сид-фраза не прошла проверку. Скрывает зависимость от bitcoinj. */
class InvalidMnemonicException(cause: Throwable? = null) : Exception(cause)

class WalletManager(
    private val secureStorage: SecureStorage,
) {

    fun createWallet(): Pair<Wallet, List<String>> {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        val mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy)
        secureStorage.saveMnemonic(mnemonic)
        val account = deriveSigningAccount(mnemonic)
        return Wallet(publicKey = account.publicKey.toBase58()) to mnemonic
    }

    @Throws(InvalidMnemonicException::class)
    fun importWallet(words: List<String>): Wallet {
        try {
            MnemonicCode.INSTANCE.check(words)
        } catch (e: MnemonicException) {
            throw InvalidMnemonicException(e)
        }
        secureStorage.saveMnemonic(words)
        val account = deriveSigningAccount(words)
        return Wallet(publicKey = account.publicKey.toBase58())
    }

    fun hasWallet(): Boolean = secureStorage.getMnemonic() != null

    fun getWallet(): Wallet? {
        val words = secureStorage.getMnemonic() ?: return null
        val account = deriveSigningAccount(words)
        return Wallet(publicKey = account.publicKey.toBase58())
    }

    fun getSignerAccount(): Account? {
        val words = secureStorage.getMnemonic() ?: return null
        return deriveSigningAccount(words)
    }

    fun clearWallet() {
        secureStorage.clearMnemonic()
    }

    private fun deriveSigningAccount(words: List<String>): Account =
        Account.fromBip44MnemonicWithChange(words, "")
}
