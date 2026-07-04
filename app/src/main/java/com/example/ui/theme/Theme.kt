package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StaticBentoPrimary,
    onPrimary = StaticBentoOnPrimary,
    secondary = StaticBentoSecondary,
    onSecondary = StaticBentoOnSecondary,
    tertiary = StaticBentoTertiary,
    onTertiary = StaticBentoOnTertiary,
    background = StaticBentoBg,
    onBackground = StaticBentoTextLight,
    surface = StaticBentoSurface,
    onSurface = StaticBentoTextLight,
    surfaceVariant = StaticBentoSurfaceVariant,
    onSurfaceVariant = StaticBentoTextMedium,
    outline = StaticBentoBorder,
    outlineVariant = StaticBentoBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4), // Rich Royal Purple primary for M3 light mode
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFEF7FF), // Soft clean off-white
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFFFFFF), // Pure white cards & surfaces
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
