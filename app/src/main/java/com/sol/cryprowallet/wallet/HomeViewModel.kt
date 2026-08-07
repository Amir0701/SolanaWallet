package com.sol.cryprowallet.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.core.wallet.WalletSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : ViewModel() {

    data class UiState(
        val publicKey: String? = null,
        val balanceLamports: Long? = null,
        val isLoadingBalance: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        sessionManager.session
            .map { it.publicKey to it.useDevnet }
            .distinctUntilChanged()
            .onEach { (publicKey, _) ->
                _state.update { it.copy(publicKey = publicKey) }
                loadBalance()
            }
            .launchIn(viewModelScope)
    }

    fun loadBalance() {
        if (sessionManager.session.value.publicKey == null) return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoadingBalance = true) }
            val result = sessionManager.getBalanceLamports()
            _state.update {
                it.copy(
                    isLoadingBalance = false,
                    balanceLamports = result.getOrNull(),
                )
            }
        }
    }
}
