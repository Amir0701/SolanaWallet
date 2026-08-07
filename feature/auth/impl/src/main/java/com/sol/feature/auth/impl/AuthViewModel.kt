package com.sol.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.core.wallet.ImportResult
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

sealed interface AuthError {
    data object InvalidMnemonic : AuthError
    data class Plain(val text: String) : AuthError
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : ViewModel() {

    data class UiState(
        val importText: String = "",
        val mnemonic: List<String>? = null,
        val error: AuthError? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<Unit>(capacity = Channel.BUFFERED)
    val authenticated = _events.receiveAsFlow()

    fun onImportTextChange(value: String) {
        _state.update { it.copy(importText = value) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun createWallet() {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching { sessionManager.createWallet() }
                .onSuccess { mnemonic ->
                    _state.update { it.copy(mnemonic = mnemonic, error = null) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(error = AuthError.Plain(e.message ?: e.javaClass.simpleName))
                    }
                }
        }
    }

    fun confirmMnemonicSaved() {
        _state.update { it.copy(mnemonic = null) }
        _events.trySend(Unit)
    }

    fun importWallet() {
        val words = _state.value.importText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        viewModelScope.launch(Dispatchers.Default) {
            when (val result = sessionManager.importWallet(words)) {
                ImportResult.Success -> {
                    _state.update { it.copy(importText = "", error = null) }
                    _events.trySend(Unit)
                }
                ImportResult.InvalidMnemonic ->
                    _state.update { it.copy(error = AuthError.InvalidMnemonic) }
                is ImportResult.Error ->
                    _state.update { it.copy(error = AuthError.Plain(result.message)) }
            }
        }
    }
}
