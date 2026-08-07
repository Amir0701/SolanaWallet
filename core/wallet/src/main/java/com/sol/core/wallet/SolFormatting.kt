package com.sol.core.wallet

import java.math.BigDecimal
import java.math.RoundingMode

object SolFormatting {
    private const val LAMPORTS_PER_SOL = 1_000_000_000L

    fun parseSolToLamports(amount: String): Long {
        val normalized = amount.trim().replace(",", ".")
        if (normalized.isEmpty()) {
            throw IllegalArgumentException("empty")
        }
        val lamports = BigDecimal(normalized)
            .multiply(BigDecimal.valueOf(LAMPORTS_PER_SOL))
            .setScale(0, RoundingMode.DOWN)
        return lamports.longValueExact()
    }

    fun formatLamportsAsSol(lamports: Long): String {
        val sol = BigDecimal.valueOf(lamports)
            .divide(BigDecimal.valueOf(LAMPORTS_PER_SOL), 9, RoundingMode.DOWN)
            .stripTrailingZeros()
        return sol.toPlainString()
    }
}
