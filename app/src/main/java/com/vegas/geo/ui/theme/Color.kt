package com.vegas.geo.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

////////////////////////////////////////////////////////////////////////////////////////////////////
//

val LightColors = lightColors(
    primary = Color(0xFF1F7192),
    primaryVariant = Color(0xFF1F7192),
    secondary = Color(0xFF006591),
    secondaryVariant = Color(0xFF006591),
    background = Color(0xFFF4F2EC),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFE93832),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onError = Color(0xFFFFFFFF),
)

val DarkColors = darkColors(
    primary = Color(0xFFB67B43),
    primaryVariant = Color(0xFFB67B43),
    secondary = Color(0xFF9D7B5A),
    secondaryVariant = Color(0xFF9D7B5A),
    background = Color(0xFF26262E),
    surface = Color(0xFF313139),
    error = Color(0xFFE93832),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onError = Color(0xFFFFFFFF),
)

val Colors.onSurfaceSecondary: Color
    get() = if (isLight) Color.Gray else Color.LightGray

val Colors.bottomNavBack: Color
    get() = if (isLight) primary else surface

val Colors.bottomNavContent: Color
    get() = if (isLight) Color(0xFFFFFFFF) else Color(0xFFFFFFFF)


