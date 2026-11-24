package com.example.dascamp.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DasCampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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

/**
 * Applies the admin accent colors while preserving the surrounding theme's
 * other colors and typography.
 */
@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    val currentScheme = MaterialTheme.colorScheme
    val adminColorScheme = currentScheme.copy(
        primary = Color(0xFFB3261E),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFB3261E),
        onPrimaryContainer = Color.White
    )

    MaterialTheme(
        colorScheme = adminColorScheme,
        content = content
    )
}

/**
 * Applies the owner accent colors while preserving the surrounding theme's
 * other colors and typography.
 */
@Composable
fun OwnerTheme(content: @Composable () -> Unit) {
    val currentScheme = MaterialTheme.colorScheme
    val ownerColorScheme = currentScheme.copy(
        primary = Color(0xFF2E7D32),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2E7D32),
        onPrimaryContainer = Color.White
    )

    MaterialTheme(
        colorScheme = ownerColorScheme,
        content = content
    )
}
