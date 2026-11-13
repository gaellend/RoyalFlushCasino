package com.gaelle.royalflushcasino.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaelle.royalflushcasino.game.chipsFor
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.ChipBlue
import com.gaelle.royalflushcasino.ui.theme.ChipPurple
import com.gaelle.royalflushcasino.ui.theme.Ivory

// Couleur d'un jeton selon sa valeur
fun chipColor(value: Int): Color = when (value) {
    10 -> ChipBlue
    50 -> CasinoRed
    100 -> CasinoBlack
    else -> ChipPurple
}

// Dessin d'un jeton de casino : corps coloré, encoches alternées sur le bord, anneau pointillé
@Composable
fun ChipFace(
    color: Color,
    modifier: Modifier = Modifier,
    edgeColor: Color = Ivory
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f

        drawCircle(color = color, radius = r)

        val stroke = r * 0.26f
        val arcRadius = r - stroke / 2f
        for (i in 0 until 8) {
            drawArc(
                color = edgeColor,
                startAngle = i * 45f,
                sweepAngle = 22.5f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = stroke)
            )
        }

        drawCircle(
            color = edgeColor.copy(alpha = 0.8f),
            radius = r * 0.6f,
            style = Stroke(
                width = r * 0.05f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.12f, r * 0.08f))
            )
        )

        drawCircle(
            color = Color.Black.copy(alpha = 0.25f),
            radius = r * 0.98f,
            style = Stroke(width = r * 0.04f)
        )
    }
}

// Jeton cliquable pour miser (taille réglable, 56 dp par défaut)
@Composable
fun CasinoChip(
    value: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        ChipFace(color = chipColor(value), modifier = Modifier.size(size))
        Text(
            text = "$value",
            color = Ivory,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.23f).sp // texte proportionnel à la taille du jeton
        )
    }
}

// Un jeton de la pile, qui tombe avec un petit rebond quand il apparaît (taille réglable)
@Composable
private fun StackedChip(
    value: Int,
    modifier: Modifier = Modifier,
    chipSize: Dp = 44.dp
) {
    val drop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drop.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))
    }
    ChipFace(
        color = chipColor(value),
        modifier = modifier
            .size(chipSize)
            .graphicsLayer {
                translationY = -chipSize.toPx() * (1f - drop.value) // tombe d'une hauteur de jeton
                alpha = drop.value.coerceIn(0f, 1f)
            }
            .shadow(2.dp, CircleShape)
    )
}

// Pile de mise verticale (portrait), avec le montant en dessous.
// Un appui retire le jeton du dessus (seulement entre deux mains).
@Composable
fun BetStack(
    amount: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    chipSize: Dp = 44.dp,   // taille des jetons
    step: Dp = 5.dp         // décalage vertical entre deux jetons
) {
    val chips = chipsFor(amount)
    Column(
        modifier = modifier.clickable(
            enabled = enabled && amount > 0,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hauteur réservée pour 10 jetons : la pile ne fait pas bouger le reste en grandissant
        Box(
            modifier = Modifier.size(width = chipSize, height = chipSize + step * 9),
            contentAlignment = Alignment.BottomCenter
        ) {
            chips.forEachIndexed { index, value ->
                StackedChip(
                    value = value,
                    chipSize = chipSize,
                    modifier = Modifier.offset(y = step * (-index))
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (amount > 0) formatChips(amount) else "",
            style = MaterialTheme.typography.labelLarge,
            color = Ivory,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
    }
}

// Pile de mise horizontale (paysage) : les jetons se chevauchent de gauche à droite,
// le montant est à droite de la pile. Un appui retire le dernier jeton.
@Composable
fun HorizontalBetStack(
    amount: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    chipSize: Dp = 30.dp,   // ← taille des jetons de la pile
    step: Dp = 6.dp         // ← décalage entre deux jetons
) {
    val chips = chipsFor(amount)
    val stackWidth = chipSize + step * (chips.size - 1).coerceAtLeast(0)

    Row(
        modifier = modifier.clickable(
            enabled = enabled && amount > 0,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(width = stackWidth, height = chipSize)) {
            chips.forEachIndexed { index, value ->
                StackedChip(
                    value = value,
                    chipSize = chipSize,
                    modifier = Modifier.offset(x = step * index)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (amount > 0) formatChips(amount) else "",
            style = MaterialTheme.typography.labelLarge,
            color = Ivory
        )
    }
}
