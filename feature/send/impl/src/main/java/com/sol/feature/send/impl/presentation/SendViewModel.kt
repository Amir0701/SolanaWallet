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

@HiltViewModel
class SendViewModel @Inject constructor(
    private val sendSol: SendSolUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SendScreenUiState())
    val state: StateFlow<SendScreenUiState> = _state.asStateFlow()

    private val _messages = Channel<SendMessage>(capacity = Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun onIntent(intent: SendIntent) {
        when (intent) {
            is SendIntent.ChangeRecipient -> _state.update { it.copy(recipient = intent.value) }
            is SendIntent.ChangeAmount -> _state.update { it.copy(amountSol = intent.value) }
            SendIntent.Send -> send()
        }
    }

    private fun send() {
        val current = _state.value
        // Флаг ставится до launch: повторное нажатие не должно отправить перевод второй раз
        if (current.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = sendSol(current.recipient, current.amountSol)) {
                is SendSolResult.Success -> {
                    _state.update { SendScreenUiState(lastSignature = result.signature) }
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
