package com.gaelle.royalflushcasino.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.data.Card

// Une carte, face visible ou cachée, qui se retourne quand faceDown change
@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    faceDown: Boolean = false
) {
    val rotation by animateFloatAsState(
        targetValue = if (faceDown) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "flip"
    )
    val cardModifier = modifier
        .width(80.dp)
        .aspectRatio(226f / 314f)
        .graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }

    if (rotation > 90f) {
        Image(
            painter = painterResource(R.drawable.card_back),
            contentDescription = "Carte cachée",
            modifier = cardModifier
        )
    } else {
        AsyncImage(
            model = card.image,
            contentDescription = "${card.value} of ${card.suit}",
            placeholder = painterResource(R.drawable.card_back),
            error = painterResource(R.drawable.card_back),
            modifier = cardModifier
        )
    }
}

// Une carte qui part du sabot et glisse jusqu'à sa place
@Composable
private fun DealtCard(
    card: Card,
    faceDown: Boolean,
    shoePosition: Offset?
) {
    val progress = remember { Animatable(0f) }
    var startOffset by remember { mutableStateOf<Offset?>(null) }

    // L'animation démarre dès qu'on connaît l'écart entre le sabot et la place de la carte
    LaunchedEffect(startOffset) {
        if (startOffset != null) {
            progress.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
    }

    PlayingCard(
        card = card,
        faceDown = faceDown,
        modifier = Modifier
            .onGloballyPositioned { coords ->
                if (startOffset == null) {
                    startOffset = if (shoePosition != null) {
                        shoePosition - coords.positionInRoot()
                    } else {
                        Offset.Zero
                    }
                }
            }
            .graphicsLayer {
                val start = startOffset
                if (start == null) {
                    alpha = 0f // invisible tant que la position n'est pas connue
                } else {
                    val t = progress.value
                    translationX = start.x * (1f - t)
                    translationY = start.y * (1f - t)
                    val scale = 0.62f + 0.38f * t // taille d'une carte du sabot → taille normale
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
    )
}

// Une main de cartes qui se chevauchent
@Composable
// Une main de cartes qui se chevauchent
fun HandRow(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    hideSecondCard: Boolean = false,
    shoePosition: Offset? = null,
    overlap: Dp = (-40).dp // plus c'est négatif, plus les cartes se chevauchent
) {
    Row(
        modifier = modifier.height(112.dp),
        horizontalArrangement = Arrangement.spacedBy(overlap)
    ) {
        cards.forEachIndexed { index, card ->
            DealtCard(
                card = card,
                faceDown = hideSecondCard && index == 1,
                shoePosition = shoePosition
            )
        }
    }
}