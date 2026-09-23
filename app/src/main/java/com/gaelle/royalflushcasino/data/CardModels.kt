package com.gaelle.royalflushcasino.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Réponse quand on crée ou mélange un paquet
@Serializable
data class DeckResponse(
    val success: Boolean,
    @SerialName("deck_id") val deckId: String,
    val shuffled: Boolean = false,
    val remaining: Int
)

// Réponse quand on pioche des cartes
@Serializable
data class DrawResponse(
    val success: Boolean,
    @SerialName("deck_id") val deckId: String,
    val cards: List<Card>,
    val remaining: Int
)

// Une carte
@Serializable
data class Card(
    val code: String,   // ex : "KH" = roi de cœur
    val image: String,  // URL de l'image PNG
    val value: String,  // "ACE", "2"... "10", "JACK", "QUEEN", "KING"
    val suit: String    // "HEARTS", "SPADES", "DIAMONDS", "CLUBS"
)