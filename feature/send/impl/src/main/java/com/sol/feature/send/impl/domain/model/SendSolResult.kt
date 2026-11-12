package com.sol.feature.send.impl.domain.model

sealed interface SendSolResult {
    data class Success(val signature: String) : SendSolResult
    data object InvalidAmount : SendSolResult
    data class Failed(val reason: String?) : SendSolResult
}
