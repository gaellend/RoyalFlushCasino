package com.gaelle.royalflushcasino.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.ChipBlue
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.GoldLight
import com.gaelle.royalflushcasino.ui.theme.Ivory
import kotlin.random.Random

// Un confetti : tous ses paramètres sont tirés au hasard une seule fois
private data class Particle(
    val startX: Float,  // position horizontale de départ (0 = gauche, 1 = droite)
    val delay: Float,   // départ décalé, pour que tout ne tombe pas en même temps
    val speed: Float,   // vitesse de chute
    val drift: Float,   // dérive horizontale pendant la chute
    val sizeDp: Float,  // taille
    val spin: Float,    // rotation totale pendant la chute
    val color: Color
)

private val CONFETTI_COLORS = listOf(Gold, GoldLight, CasinoRed, Ivory, ChipBlue)

// Pluie de confettis qui démarre dès l'affichage du composant
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val particles = remember {
        List(80) {
            Particle(
                startX = Random.nextFloat(),
                delay = Random.nextFloat() * 0.3f,
                speed = 0.8f + Random.nextFloat() * 0.5f,
                drift = (Random.nextFloat() - 0.5f) * 0.3f,
                sizeDp = 6f + Random.nextFloat() * 6f,
                spin = Random.nextFloat() * 720f - 360f,
                color = CONFETTI_COLORS.random()
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(4000, easing = LinearEasing)) // ← durée de la chute en ms
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        particles.forEach { p ->
            // Avancement propre à ce confetti (il démarre après son délai)
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach

            val x = (p.startX + p.drift * local) * size.width
            val y = -20f + local * p.speed * (size.height + 40f)
            val alpha = if (local > 0.85f) (1f - local) / 0.15f else 1f // disparaît en douceur à la fin
            val w = p.sizeDp * density
            val h = w / 2f

            rotate(degrees = p.spin * local, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - w / 2f, y - h / 2f),
                    size = Size(w, h)
                )
            }
        }
    }
}