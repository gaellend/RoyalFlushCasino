package com.gaelle.royalflushcasino.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.ui.theme.Ivory

// Le sabot du croupier : une pile de dos de cartes + le nombre de cartes restantes
@Composable
fun CardShoe(
    remaining: Int?,
    isShuffling: Boolean,
    onPositioned: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // Plus le sabot se vide, moins on voit de cartes empilées
    val visibleCards = when {
        remaining == null -> 3
        remaining > 200 -> 5
        remaining > 120 -> 4
        remaining > 60 -> 3
        remaining > 0 -> 2
        else -> 0
    }

    val transition = rememberInfiniteTransition(label = "shuffle")
    val wiggle by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(120), RepeatMode.Reverse),
        label = "wiggle"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 80.dp)
                .graphicsLayer { rotationZ = if (isShuffling) wiggle else 0f }
        ) {
            repeat(visibleCards) { i ->
                Image(
                    painter = painterResource(R.drawable.card_back),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (i * 2).dp, y = (i * 2).dp)
                        .width(50.dp)
                        .aspectRatio(226f / 314f)
                )
            }
            // Repère invisible au sommet de la pile : point de départ des cartes
            val top = (maxOf(visibleCards - 1, 0) * 2).dp
            Box(
                modifier = Modifier
                    .offset(x = top, y = top)
                    .width(50.dp)
                    .aspectRatio(226f / 314f)
                    .onGloballyPositioned { onPositioned(it.positionInRoot()) }
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = when {
                isShuffling -> "Remélange…"
                remaining == null -> "…"
                else -> "$remaining cartes"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Ivory.copy(alpha = 0.8f)
        )
    }
}