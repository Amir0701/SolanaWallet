package com.sol.core.wallet

sealed interface ImportResult {
    data object Success : ImportResult
    data object InvalidMnemonic : ImportResult
    data class Error(val message: String) : ImportResult
}
