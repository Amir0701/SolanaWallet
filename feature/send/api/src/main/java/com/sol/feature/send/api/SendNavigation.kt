package com.sol.feature.send.api

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

object SendFeature {
    const val ROUTE = "send"
}

fun NavController.navigateToSend(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(SendFeature.ROUTE, builder)
}
