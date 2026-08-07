package com.sol.feature.auth.impl

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sol.feature.auth.api.AuthFeature

/**
 * Регистрирует экран авторизации в графе навигации хоста.
 *
 * @param onAuthenticated вызывается после успешного создания/импорта кошелька.
 */
fun NavGraphBuilder.authScreen(
    onAuthenticated: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable(AuthFeature.ROUTE) {
        AuthScreen(
            onAuthenticated = onAuthenticated,
            onShowMessage = onShowMessage,
            modifier = modifier,
        )
    }
}
