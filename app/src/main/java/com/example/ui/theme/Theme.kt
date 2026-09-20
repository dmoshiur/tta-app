package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SoftGold,
    secondary = GoldenGold,
    tertiary = SoftGold,
    background = DarkSurface,
    surface = DarkSurface,
    onPrimary = DeepNavy,
    onSecondary = DeepNavy,
    onBackground = WarmCream,
    onSurface = WarmCream,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = GoldenGold,
    tertiary = SoftGold,
    background = WarmCream,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = InkBody,
    onBackground = InkBody,
    onSurface = InkBody,
    outline = BorderColor,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors by default to preserve strict brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
