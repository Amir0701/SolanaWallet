package com.sol.crypto

data class Wallet(
    val publicKey: String,
    val createdAt: Long = System.currentTimeMillis(),
)
