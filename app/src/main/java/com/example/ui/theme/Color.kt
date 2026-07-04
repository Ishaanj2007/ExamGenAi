package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Static / Fallback Bento Grid Theme Colors (used in Theme.kt)
val StaticBentoBg = Color(0xFF1C1B1F)
val StaticBentoSurface = Color(0xFF2B2930)
val StaticBentoSurfaceVariant = Color(0xFF333138)
val StaticBentoBorder = Color(0xFF49454F)
val StaticBentoPrimary = Color(0xFFD0BCFF)
val StaticBentoOnPrimary = Color(0xFF381E72)
val StaticBentoSecondary = Color(0xFFE8DEF8)
val StaticBentoOnSecondary = Color(0xFF1D192B)
val StaticBentoTertiary = Color(0xFFEADDFF)
val StaticBentoOnTertiary = Color(0xFF21005D)
val StaticBentoTextLight = Color(0xFFE6E1E5)
val StaticBentoTextMedium = Color(0xFFCAC4D0)
val StaticBentoTextMuted = Color(0xFF938F99)
val StaticBentoGold = Color(0xFFBDB191)
val StaticBentoAmber = Color(0xFFFFD54F)

// Dynamic Composable Getters to allow real-time Theme toggling & Dynamic System matching
val BentoBg: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val BentoSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val BentoSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val BentoBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val BentoPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val BentoOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val BentoSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondary

val BentoOnSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondary

val BentoTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val BentoOnTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onTertiary

val BentoTextLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val BentoTextMedium: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val BentoTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val BentoGold: Color
    @Composable
    @ReadOnlyComposable
    get() = StaticBentoGold

val BentoAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = StaticBentoAmber

