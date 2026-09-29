package com.gaelle.royalflushcasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaelle.royalflushcasino.ui.theme.Ivory

// Un jeton de casino rond et cliquable
@Composable
fun CasinoChip(
    value: Int,
    color: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .alpha(if (enabled) 1f else 0.4f) // grisé si on ne peut pas miser ce jeton
            .clip(CircleShape)
            .background(color)
            .border(4.dp, Ivory.copy(alpha = 0.8f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$value", color = textColor, fontWeight = FontWeight.Bold)
    }
}