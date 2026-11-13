package com.gaelle.royalflushcasino.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaelle.royalflushcasino.ui.theme.ActionOrange
import com.gaelle.royalflushcasino.ui.theme.Bronze
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.ChipBlue
import com.gaelle.royalflushcasino.ui.theme.ChipPurple
import com.gaelle.royalflushcasino.ui.theme.FeltGreenDark
import com.gaelle.royalflushcasino.ui.theme.FeltGreenLight
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import com.gaelle.royalflushcasino.ui.theme.PokerRed
import com.gaelle.royalflushcasino.ui.theme.Silver
import kotlinx.coroutines.delay

// Fond commun du hall, de "Qui joue ?" et du classement : du vert du tapis vers le noir
val CasinoBackground: Brush = Brush.verticalGradient(listOf(FeltGreenDark, CasinoBlack, CasinoBlack))

// Couleurs possibles des avatars
private val AVATAR_COLORS = listOf(CasinoRed, ChipBlue, ChipPurple, FeltGreenLight, PokerRed, Bronze, ActionOrange)

// Couleur d'un joueur : calculée à partir de son pseudo, donc toujours la même pour lui
fun avatarColor(name: String): Color {
    val index = (name.lowercase().hashCode() and Int.MAX_VALUE) % AVATAR_COLORS.size
    return AVATAR_COLORS[index]
}

// Couleur du podium : or, argent, bronze
fun medalColor(rank: Int): Color = when (rank) {
    1 -> Gold
    2 -> Silver
    else -> Bronze
}

// Avatar d'un joueur : un jeton de casino à sa couleur, avec l'initiale de son pseudo
@Composable
fun PlayerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        ChipFace(color = avatarColor(name), modifier = Modifier.size(size))
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = Ivory,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

// Fait apparaître un élément en glissant vers le haut, avec un léger décalage selon sa position
@Composable
fun AppearingItem(index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(8) * 70L) // au-delà de 8 éléments, pas d'attente supplémentaire
        progress.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress.value
            translationY = 32.dp.toPx() * (1f - progress.value)
        }
    ) {
        content()
    }
}