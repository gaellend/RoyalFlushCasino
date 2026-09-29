package com.gaelle.royalflushcasino.game

import com.gaelle.royalflushcasino.data.Card

// Les étapes d'une main
enum class GamePhase {
    WAITING,      // en attente de distribution
    DEALING,      // distribution en cours
    PLAYER_TURN,  // le joueur choisit : tirer ou rester
    DEALER_TURN,  // le croupier joue
    FINISHED      // main terminée, résultat affiché
}

// Les résultats possibles, avec le message à afficher
enum class GameResult(val message: String) {
    BLACKJACK("Blackjack ! Vous gagnez"),
    WIN("Vous gagnez !"),
    DEALER_BUST("Le croupier dépasse 21, vous gagnez !"),
    PUSH("Égalité"),
    LOSE("Le croupier gagne"),
    BUST("Vous dépassez 21, perdu !")
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

// Compare les deux mains et donne le résultat
fun computeResult(player: List<Card>, dealer: List<Card>): GameResult {
    val p = handValue(player)
    val d = handValue(dealer)
    return when {
        p > 21 -> GameResult.BUST
        isBlackjack(player) && isBlackjack(dealer) -> GameResult.PUSH
        isBlackjack(player) -> GameResult.BLACKJACK
        isBlackjack(dealer) -> GameResult.LOSE
        d > 21 -> GameResult.DEALER_BUST
        p > d -> GameResult.WIN
        p < d -> GameResult.LOSE
        else -> GameResult.PUSH
    }
}