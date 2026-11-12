package com.sol.feature.send.impl.domain.repository

interface TransferRepository {
    /** Main-safe. Возвращает сигнатуру транзакции. */
    suspend fun transfer(recipient: String, lamports: Long): Result<String>
}
