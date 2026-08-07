package com.sol.feature.auth.api

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Точка входа в фичу авторизации: только маршрут и навигация на него.
 * Реализация экрана живёт в feature:auth:impl.
 */
object AuthFeature {
    const val ROUTE = "auth"
}

fun NavController.navigateToAuth(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(AuthFeature.ROUTE, builder)
}
