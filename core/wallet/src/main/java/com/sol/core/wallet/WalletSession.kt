package com.sol.core.wallet

/**
 * Общее состояние сессии кошелька, разделяемое между фичами.
 */
data class WalletSession(
    val hasWallet: Boolean = false,
    val publicKey: String? = null,
    val useDevnet: Boolean = true,
)
