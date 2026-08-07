package com.sol.feature.receive.api

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

object ReceiveFeature {
    const val ROUTE = "receive"
}

fun NavController.navigateToReceive(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(ReceiveFeature.ROUTE, builder)
}
