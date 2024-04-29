package com.vegas.geo.ui.navigation

interface AppNavigationDestination {
    val route: String
    val destination: String

    fun routeTo(): String = route
}