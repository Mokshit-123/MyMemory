package com.example.mymemory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.mymemory.R


// Load the Orbitron font family
val Orbitron = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_medium, FontWeight.Medium),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

// Load the Rajdhani font family
val Rajdhani = FontFamily(
    Font(R.font.rajdhani_light, FontWeight.Light),
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

// Set up the typography
val AppTypography = Typography(
    // Title style using Orbitron
    displayLarge = TextStyle(
        fontFamily = Orbitron,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Orbitron,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Orbitron,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
    ),
    // Body style using Rajdhani
    bodyLarge = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
    ),
    // Button style using Rajdhani (for consistency)
    labelLarge = TextStyle(
        fontFamily = Rajdhani,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    ),
)
