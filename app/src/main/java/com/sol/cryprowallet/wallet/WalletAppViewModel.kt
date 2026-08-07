package com.sol.cryprowallet.wallet

import androidx.lifecycle.ViewModel
import com.sol.core.wallet.WalletSession
import com.sol.core.wallet.WalletSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WalletAppViewModel @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : ViewModel() {

    val session: StateFlow<WalletSession> = sessionManager.session

    val hasWalletInitially: Boolean = sessionManager.session.value.hasWallet

    fun toggleNetwork() = sessionManager.toggleNetwork()

    fun deleteWallet() = sessionManager.deleteWallet()
}
