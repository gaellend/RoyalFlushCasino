package com.gaelle.royalflushcasino.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gaelle.royalflushcasino.data.Card

const val CARD_BACK_URL = "https://deckofcardsapi.com/static/img/back.png"

// Une carte, face visible ou cachée
@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    faceDown: Boolean = false
) {
    AsyncImage(
        model = if (faceDown) CARD_BACK_URL else card.image,
        contentDescription = if (faceDown) "Carte cachée" else "${card.value} of ${card.suit}",
        modifier = modifier
            .width(80.dp)
            .aspectRatio(226f / 314f) // format d'une carte à jouer
    )
}

// Une main de cartes qui se chevauchent, comme sur une vraie table
@Composable
fun HandRow(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    hideSecondCard: Boolean = false
) {
    Row(
        modifier = modifier.height(112.dp), // place réservée même sans cartes
        horizontalArrangement = Arrangement.spacedBy((-40).dp) // espacement négatif = chevauchement
    ) {
        cards.forEachIndexed { index, card ->
            PlayingCard(card = card, faceDown = hideSecondCard && index == 1)
        }
    }
}