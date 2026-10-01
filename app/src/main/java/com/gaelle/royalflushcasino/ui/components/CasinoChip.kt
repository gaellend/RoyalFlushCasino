package com.gaelle.royalflushcasino.ui.components

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

// Jeton cliquable pour miser
@Composable
fun CasinoChip(
    value: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ChipFace(color = chipColor(value), modifier = Modifier.size(56.dp))
        Text(text = "$value", color = Ivory, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// Un jeton de la pile, qui tombe avec un petit rebond quand il apparaît
@Composable
private fun StackedChip(value: Int, modifier: Modifier = Modifier) {
    val drop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drop.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))
    }
    ChipFace(
        color = chipColor(value),
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                translationY = -40.dp.toPx() * (1f - drop.value)
                alpha = drop.value.coerceIn(0f, 1f)
            }
            .shadow(2.dp, CircleShape)
    )
}

// La pile de jetons de la mise, avec le montant en dessous.
// Un appui retire le jeton du dessus (seulement entre deux mains).
@Composable
fun BetStack(
    amount: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val chips = chipsFor(amount)
    Column(
        modifier = modifier.clickable(
            enabled = enabled && amount > 0,
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // pas d'effet de surbrillance : c'est le jeton qui disparaît
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(width = 44.dp, height = 90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            chips.forEachIndexed { index, value ->
                StackedChip(value = value, modifier = Modifier.offset(y = (-5 * index).dp))
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