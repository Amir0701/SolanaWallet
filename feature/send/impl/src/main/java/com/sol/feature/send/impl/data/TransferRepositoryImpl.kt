package com.sol.feature.send.impl.data

import com.sol.core.wallet.WalletSessionManager
import com.sol.feature.send.impl.domain.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : TransferRepository {
    override suspend fun transfer(recipient: String, lamports: Long): Result<String> =
        withContext(Dispatchers.IO) { sessionManager.transfer(recipient, lamports) }
}
