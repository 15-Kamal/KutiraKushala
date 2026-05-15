package com.kutirakushala.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Saffron       = Color(0xFFE07B39)
val SaffronDark   = Color(0xFFB85C1A)
val SaffronLight  = Color(0xFFFFDDBB)
val Turmeric      = Color(0xFFF5C842)
val Leaf          = Color(0xFF4CAF50)
val Clay          = Color(0xFF6D4C41)
val Cream         = Color(0xFFFFF8F0)
val OnSaffron     = Color(0xFFFFFFFF)
val CapacityRed   = Color(0xFFD32F2F)

private val LightColors = lightColorScheme(
    primary            = Saffron,
    onPrimary          = OnSaffron,
    primaryContainer   = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary          = Turmeric,
    onSecondary        = Color(0xFF3E2E00),
    background         = Cream,
    onBackground       = Color(0xFF1C1B1F),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF1C1B1F),
    surfaceVariant     = Color(0xFFF3E9DF),
    outline            = Color(0xFFB8A99A)
)

@Composable
fun KutiraKushalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}