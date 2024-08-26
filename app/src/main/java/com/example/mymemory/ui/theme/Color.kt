package com.example.mymemory.ui.theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color



val PurplePrimary = Color(0xFF7D60A2)
val PurplePrimaryVariant = Color(0xFF3B2E5A)
val CoralSecondary = Color(0xFFFF758C)
val SpaceNavyBackground = Color(0xFF1B1A3D)
val SpaceNavySurface = Color(0xFF25274D)
val ErrorRed = Color(0xFFD93854)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF1B1A3D)
val OnBackground = Color(0xFFE0E1F3)

// For quick reference in MaterialTheme
val lightColors = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = OnPrimary,
    primaryContainer = PurplePrimaryVariant,
    secondary = CoralSecondary,
    onSecondary = OnSecondary,
    error = ErrorRed,
    background = SpaceNavyBackground,
    onBackground = OnBackground,
    surface = SpaceNavySurface,
    onSurface = OnBackground,
)

val darkColors = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = OnPrimary,
    primaryContainer = PurplePrimaryVariant,
    secondary = CoralSecondary,
    onSecondary = OnSecondary,
    error = ErrorRed,
    background = SpaceNavyBackground,
    onBackground = OnBackground,
    surface = SpaceNavySurface,
    onSurface = OnBackground,
)








