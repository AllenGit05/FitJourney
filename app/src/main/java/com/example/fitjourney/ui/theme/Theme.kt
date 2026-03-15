package com.example.fitjourney.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FitJourneyColorScheme = darkColorScheme(
    primary            = FJGold,
    onPrimary          = FJOnGold,
    primaryContainer   = FJGoldDark,
    onPrimaryContainer = FJOnGold,
    secondary          = FJSurfaceHigh,
    onSecondary        = FJTextPrimary,
    background         = FJBackground,
    onBackground       = FJTextPrimary,
    surface            = FJSurface,
    onSurface          = FJTextPrimary,
    surfaceVariant     = FJSurfaceHigh,
    onSurfaceVariant   = FJTextSecondary,
    error              = FJError,
    onError            = FJTextPrimary,
    outline            = FJDivider,
)

@Composable
fun FitJourneyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FitJourneyColorScheme,
        typography  = Typography,
        content     = content
    )
}
