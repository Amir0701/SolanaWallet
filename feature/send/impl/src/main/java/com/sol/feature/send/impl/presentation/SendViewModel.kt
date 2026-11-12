package com.sol.feature.send.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.feature.send.impl.domain.model.SendSolResult
import com.sol.feature.send.impl.domain.usecase.SendSolUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val sendSol: SendSolUseCase,
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
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            when (val result = sendSol(current.recipient, current.amountSol)) {
                is SendSolResult.Success -> {
                    _state.update { UiState(lastSignature = result.signature) }
                    _messages.trySend(SendMessage.Success)
                }
                SendSolResult.InvalidAmount -> {
                    _state.update { it.copy(isSending = false) }
                    _messages.trySend(SendMessage.BadAmount)
                }
                is SendSolResult.Failed -> {
                    _state.update { it.copy(isSending = false) }
                    _messages.trySend(result.reason?.let(SendMessage::Plain) ?: SendMessage.Failed)
                }
            }
        }
    }
}
