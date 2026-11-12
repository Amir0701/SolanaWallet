package com.sol.feature.send.impl

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sol.feature.send.api.SendFeature
import com.sol.feature.send.impl.presentation.SendScreen

fun NavGraphBuilder.sendScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable(SendFeature.ROUTE) {
        SendScreen(
            onBack = onBack,
            onShowMessage = onShowMessage,
            modifier = modifier,
        )
    }
}
