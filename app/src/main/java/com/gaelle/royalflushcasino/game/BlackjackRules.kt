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
    BUST("Vous dépassez 21", "Perdu", 0.0),
    SURRENDER("Vous abandonnez la main", "Abandon", 0.5)
}

// Une main du joueur (2 après un split)
data class PlayerHand(
    val cards: List<Card> = emptyList(),
    val bet: Int,
    val fromSplit: Boolean = false, // main issue d'une séparation
    val splitAces: Boolean = false, // As séparés : une seule carte chacun
    val doubled: Boolean = false,   // mise doublée
    val stood: Boolean = false,
    val result: GameResult? = null  // déjà connu en cas d'abandon
) {
    val value: Int get() = handValue(cards)
    val isBust: Boolean get() = value > 21

    // La main n'attend plus de décision : restée, plus de 21, 21 pile, ou abandonnée
    val isDone: Boolean get() = stood || value >= 21 || result != null

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

// --- Séparer ---

// Le joueur a une paire séparable (même valeur), qu'il ait assez de jetons ou non
fun hasSplittablePair(hands: List<PlayerHand>): Boolean {
    if (hands.size != 1) return false
    val hand = hands[0]
    return hand.cards.size == 2 &&
            !hand.isDone &&
            hand.cards[0].points() == hand.cards[1].points()
}

// ... et il a assez de jetons pour la 2e mise
fun canSplit(hands: List<PlayerHand>, balance: Int): Boolean =
    hasSplittablePair(hands) && balance >= hands[0].bet

// --- Doubler ---

// La règle autorise à doubler (2 cartes, pas d'As séparés), qu'il ait assez de jetons ou non
fun doubleAllowed(hand: PlayerHand): Boolean =
    hand.cards.size == 2 && !hand.splitAces && !hand.isDone

// ... et il a assez de jetons pour doubler
fun canDouble(hand: PlayerHand, balance: Int): Boolean =
    doubleAllowed(hand) && balance >= hand.bet

// --- Abandonner ---

// Seulement sur la main de départ, avant toute autre action
fun canSurrender(hands: List<PlayerHand>): Boolean =
    hands.size == 1 && !hands[0].fromSplit && hands[0].cards.size == 2 && !hands[0].isDone

// Compare une main du joueur à celle du croupier
fun computeResult(hand: PlayerHand, dealer: List<Card>): GameResult {
    hand.result?.let { return it } // abandon : le résultat est déjà décidé
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