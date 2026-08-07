package com.sol.feature.receive.impl

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sol.feature.receive.api.ReceiveFeature

fun NavGraphBuilder.receiveScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable(ReceiveFeature.ROUTE) {
        ReceiveScreen(
            onBack = onBack,
            onShowMessage = onShowMessage,
            modifier = modifier,
        )
    }
}
