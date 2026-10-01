package com.gaelle.royalflushcasino.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gaelle.royalflushcasino.R

// Police des titres : élégante, style casino
val Playfair = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

// Police du texte : moderne et lisible
val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val base = Typography()

val Typography = Typography(
    // Grands titres : Playfair
    displayLarge = base.displayLarge.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Playfair, fontWeight = FontWeight.Bold),
    // Petits titres, texte et boutons : Montserrat
    titleMedium = base.titleMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(fontFamily = Montserrat),
    bodyMedium = base.bodyMedium.copy(fontFamily = Montserrat),
    bodySmall = base.bodySmall.copy(fontFamily = Montserrat),
    labelLarge = base.labelLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.Medium)
)