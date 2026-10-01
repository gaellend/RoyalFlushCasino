package com.gaelle.royalflushcasino.game

import com.gaelle.royalflushcasino.data.Card

// Les étapes d'une main
enum class GamePhase {
    WAITING,      // en attente de mise et de distribution
    DEALING,      // distribution en cours
    PLAYER_TURN,  // le joueur joue sa ou ses mains
    DEALER_TURN,  // le croupier joue
    FINISHED      // main terminée
}

// Résultat d'une main : phrase complète, version courte (sous une main séparée), multiplicateur
enum class GameResult(val message: String, val short: String, val payout: Double) {
    BLACKJACK("Blackjack !", "Blackjack", 2.5),
    WIN("Vous gagnez !", "Gagné", 2.0),
    DEALER_BUST("Le croupier dépasse 21 !", "Gagné", 2.0),
    PUSH("Égalité", "Égalité", 1.0),
    LOSE("Le croupier gagne", "Perdu", 0.0),
    BUST("Vous dépassez 21", "Perdu", 0.0)
}

// Une main du joueur (2 après un split)
data class PlayerHand(
    val cards: List<Card> = emptyList(),
    val bet: Int,
    val fromSplit: Boolean = false, // main issue d'une séparation
    val splitAces: Boolean = false, // As séparés : une seule carte chacun
    val stood: Boolean = false,
    val result: GameResult? = null
) {
    val value: Int get() = handValue(cards)
    val isBust: Boolean get() = value > 21

    // La main n'attend plus de décision : le joueur est resté, a dépassé 21 ou a 21
    val isDone: Boolean get() = stood || value >= 21

    // Blackjack "naturel" : seulement sur la main de départ, jamais après un split
    val isNatural: Boolean get() = !fromSplit && isBlackjack(cards)
}

// Valeur d'une carte (l'as compte 11 ici, on l'ajuste dans handValue)
fun Card.points(): Int = when (value) {
    "ACE" -> 11
    "KING", "QUEEN", "JACK" -> 10
    else -> value.toIntOrNull() ?: 0
}

// Valeur d'une main : les as passent de 11 à 1 si on dépasse 21
fun handValue(cards: List<Card>): Int {
    var total = cards.sumOf { it.points() }
    var aces = cards.count { it.value == "ACE" }
    while (total > 21 && aces > 0) {
        total -= 10
        aces--
    }
    return total
}

fun isBlackjack(cards: List<Card>): Boolean =
    cards.size == 2 && handValue(cards) == 21

// Séparer : une seule fois, 2 cartes de même valeur, et assez de jetons pour la 2e mise
fun canSplit(hands: List<PlayerHand>, balance: Int): Boolean {
    if (hands.size != 1) return false
    val hand = hands[0]
    return hand.cards.size == 2 &&
            !hand.isDone &&
            hand.cards[0].points() == hand.cards[1].points() &&
            balance >= hand.bet
}

// Compare une main du joueur à celle du croupier
fun computeResult(hand: PlayerHand, dealer: List<Card>): GameResult {
    val p = hand.value
    val d = handValue(dealer)
    val dealerNatural = isBlackjack(dealer)
    return when {
        p > 21 -> GameResult.BUST
        hand.isNatural && dealerNatural -> GameResult.PUSH
        hand.isNatural -> GameResult.BLACKJACK
        dealerNatural -> GameResult.LOSE
        d > 21 -> GameResult.DEALER_BUST
        p > d -> GameResult.WIN
        p < d -> GameResult.LOSE
        else -> GameResult.PUSH
    }
}