package com.sol.cryprowallet.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sol.cryprowallet.wallet.ui.HomeScreen
import com.sol.feature.auth.api.AuthFeature
import com.sol.feature.auth.impl.authScreen
import com.sol.feature.receive.api.navigateToReceive
import com.sol.feature.receive.impl.receiveScreen
import com.sol.feature.send.api.navigateToSend
import com.sol.feature.send.impl.sendScreen

const val HOME_ROUTE = "home"

@Composable
fun WalletNavHost(
    navController: NavHostController,
    startDestination: String,
    onShowMessage: (String) -> Unit,
    onDeleteWallet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authScreen(
            onAuthenticated = {
                navController.navigate(HOME_ROUTE) {
                    popUpTo(AuthFeature.ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onShowMessage = onShowMessage,
        )

        composable(HOME_ROUTE) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val homeState by homeViewModel.state.collectAsStateWithLifecycle()
            HomeScreen(
                publicKey = homeState.publicKey.orEmpty(),
                balanceLamports = homeState.balanceLamports,
                isLoadingBalance = homeState.isLoadingBalance,
                onRefresh = homeViewModel::loadBalance,
                onSend = { navController.navigateToSend() },
                onReceive = { navController.navigateToReceive() },
                onShowMessage = onShowMessage,
                onDelete = {
                    onDeleteWallet()
                    navController.navigate(AuthFeature.ROUTE) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        sendScreen(
            onBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )

        receiveScreen(
            onBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }
}
