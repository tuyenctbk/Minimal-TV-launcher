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
    primary = ElegantIndigo,
    secondary = ElegantIndigoLight,
    tertiary = ElegantEmerald,
    background = ElegantZincBg,
    surface = ElegantZincSurface,
    surfaceVariant = ElegantZincBorder,
    onPrimary = Color.White,
    onBackground = Color(0xFFF4F4F5), // zinc-100
    onSurface = Color(0xFFF4F4F5),     // zinc-100
    onSurfaceVariant = Color(0xFFA1A1AA)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElegantIndigo,
    secondary = Purple40,
    tertiary = ElegantEmerald,
    background = LivingRoomLightBg,
    surface = LivingRoomLightSurface,
    surfaceVariant = LivingRoomLightSurfaceVariant,
    onPrimary = Color.White,
    onBackground = LivingRoomLightText,
    onSurface = LivingRoomLightText,
    onSurfaceVariant = LivingRoomLightTextMuted
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
