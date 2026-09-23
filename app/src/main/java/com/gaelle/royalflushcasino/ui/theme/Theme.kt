package com.gaelle.royalflushcasino.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Un seul thème, toujours sombre : l'ambiance casino ne dépend pas du téléphone
private val CasinoColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = CasinoBlack,
    secondary = CasinoRed,
    onSecondary = Ivory,
    tertiary = FeltGreen,
    onTertiary = Ivory,
    background = CasinoBlack,
    onBackground = Ivory,
    surface = CasinoSurface,
    onSurface = Ivory
)

@Composable
fun RoyalFlushCasinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CasinoColorScheme,
        typography = Typography,
        content = content
    )
}