package com.sol.feature.send.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.core.wallet.SolFormatting
import com.sol.core.wallet.WalletSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SendMessage {
    data object Success : SendMessage
    data object BadAmount : SendMessage
    data object Failed : SendMessage
    data class Plain(val text: String) : SendMessage
}

@HiltViewModel
class SendViewModel @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : ViewModel() {

    data class UiState(
        val recipient: String = "",
        val amountSol: String = "",
        val isSending: Boolean = false,
        val lastSignature: String? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _messages = Channel<SendMessage>(capacity = Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun onRecipientChange(value: String) {
        _state.update { it.copy(recipient = value) }
    }

    fun onAmountChange(value: String) {
        _state.update { it.copy(amountSol = value) }
    }

    fun send() {
        val recipient = _state.value.recipient.trim()
        val amountSol = _state.value.amountSol.trim()
        viewModelScope.launch(Dispatchers.IO) {
            val lamports = runCatching { SolFormatting.parseSolToLamports(amountSol) }.getOrNull()
            if (lamports == null || lamports <= 0L) {
                _messages.trySend(SendMessage.BadAmount)
                return@launch
            }
            _state.update { it.copy(isSending = true) }
            val result = sessionManager.transfer(recipient, lamports)
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isSending = false,
                        lastSignature = result.getOrNull(),
                        recipient = "",
                        amountSol = "",
                    )
                }
                _messages.trySend(SendMessage.Success)
            } else {
                _state.update { it.copy(isSending = false) }
                val text = result.exceptionOrNull()?.message
                _messages.trySend(
                    if (text.isNullOrBlank()) SendMessage.Failed else SendMessage.Plain(text),
                )
            }
        }
    }
}
