package com.sol.core.wallet

import com.sol.crypto.InvalidMnemonicException
import com.sol.crypto.WalletManager
import com.sol.network.SolanaCluster
import com.sol.network.SolanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единый источник правды о текущем кошельке и выбранной сети.
 *
 * Инкапсулирует [WalletManager] (ключи) и [SolanaRepository] (RPC), чтобы фичевые
 * модули зависели только от [com.sol.core.wallet] и не тянули crypto/network напрямую.
 */
@Singleton
class WalletSessionManager @Inject constructor(
    private val walletManager: WalletManager,
    private val repositoryFactory: SolanaRepository.Factory,
) {
    private val _session = MutableStateFlow(
        WalletSession(
            hasWallet = walletManager.hasWallet(),
            publicKey = walletManager.getWallet()?.publicKey,
            useDevnet = true,
        ),
    )
    val session: StateFlow<WalletSession> = _session.asStateFlow()

    private fun currentRepository(): SolanaRepository = repositoryFactory.create(
        if (_session.value.useDevnet) SolanaCluster.DEVNET_RPC else SolanaCluster.MAINNET_RPC,
    )

    fun toggleNetwork() {
        _session.update { it.copy(useDevnet = !it.useDevnet) }
    }

    fun createWallet(): List<String> {
        val (wallet, mnemonic) = walletManager.createWallet()
        _session.update { it.copy(hasWallet = true, publicKey = wallet.publicKey) }
        return mnemonic
    }

    fun importWallet(words: List<String>): ImportResult {
        return try {
            val wallet = walletManager.importWallet(words)
            _session.update { it.copy(hasWallet = true, publicKey = wallet.publicKey) }
            ImportResult.Success
        } catch (e: InvalidMnemonicException) {
            ImportResult.InvalidMnemonic
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    fun deleteWallet() {
        walletManager.clearWallet()
        _session.update { it.copy(hasWallet = false, publicKey = null) }
    }

    fun getBalanceLamports(): Result<Long> {
        val publicKey = _session.value.publicKey
            ?: return Result.failure(IllegalStateException("No wallet"))
        return currentRepository().getBalanceLamports(publicKey)
    }

    fun transfer(recipientBase58: String, lamports: Long): Result<String> {
        val signer = walletManager.getSignerAccount()
            ?: return Result.failure(IllegalStateException("No wallet"))
        return currentRepository().transferSol(signer, recipientBase58, lamports)
    }
}
