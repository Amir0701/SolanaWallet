package com.sol.feature.receive.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.core.wallet.WalletSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    sessionManager: WalletSessionManager,
) : ViewModel() {

    val publicKey: StateFlow<String> = sessionManager.session
        .map { it.publicKey.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = sessionManager.session.value.publicKey.orEmpty(),
        )
}
