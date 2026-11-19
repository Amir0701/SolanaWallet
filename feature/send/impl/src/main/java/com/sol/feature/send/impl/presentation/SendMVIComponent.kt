package com.sol.feature.send.impl.presentation

data class SendScreenUiState(
    val recipient: String = "",
    val amountSol: String = "",
    val isSending: Boolean = false,
    val lastSignature: String? = null,
)

sealed class SendIntent {
    data class ChangeRecipient(val value: String) : SendIntent()
    data class ChangeAmount(val value: String) : SendIntent()
    data object Send : SendIntent()
}

sealed interface SendMessage {
    data object Success : SendMessage
    data object BadAmount : SendMessage
    data object Failed : SendMessage
    data class Plain(val text: String) : SendMessage
}
