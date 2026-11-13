package com.sol.feature.send.impl.domain.usecase

import com.sol.core.wallet.SolFormatting
import com.sol.feature.send.impl.domain.model.SendSolResult
import com.sol.feature.send.impl.domain.repository.TransferRepository
import javax.inject.Inject

class SendSolUseCase @Inject constructor(
    private val repository: TransferRepository,
) {
    suspend operator fun invoke(recipient: String, amountSol: String): SendSolResult {
        // parseSolToLamports не suspend, поэтому runCatching здесь не глотает отмену
        val lamports = runCatching { SolFormatting.parseSolToLamports(amountSol) }.getOrNull()
        if (lamports == null || lamports <= 0L) return SendSolResult.InvalidAmount

        return repository.transfer(recipient.trim(), lamports).fold(
            onSuccess = { SendSolResult.Success(it) },
            onFailure = { SendSolResult.Failed(it.message?.takeIf(String::isNotBlank)) },
        )
    }
}
