package com.gaelle.royalflushcasino.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaelle.royalflushcasino.data.Card
import com.gaelle.royalflushcasino.data.DeckRepository
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.GameResult
import com.gaelle.royalflushcasino.game.computeResult
import com.gaelle.royalflushcasino.game.handValue
import com.gaelle.royalflushcasino.game.isBlackjack
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BlackjackViewModel : ViewModel() {

    // États observés par l'interface
    val playerCards = MutableStateFlow<List<Card>>(emptyList())
    val dealerCards = MutableStateFlow<List<Card>>(emptyList())
    val phase = MutableStateFlow(GamePhase.WAITING)
    val result = MutableStateFlow<GameResult?>(null)
    val isBusy = MutableStateFlow(false) // comme onRoll dans le TP : une action est en cours
    val error = MutableStateFlow<String?>(null)

    private val repository = DeckRepository()
    private var deckId: String? = null
    private var cardsUsed = 0

    // Récupère le paquet en cours, ou en crée un au premier appel
    private suspend fun getDeckId(): String {
        deckId?.let { return it }
        val newId = repository.newDeck().deckId
        deckId = newId
        cardsUsed = 0
        return newId
    }

    // Nouvelle main : 2 cartes au joueur, 2 au croupier, en alternance
    fun deal() {
        if (isBusy.value) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            result.value = null
            playerCards.value = emptyList()
            dealerCards.value = emptyList()
            phase.value = GamePhase.DEALING
            try {
                val id = getDeckId()
                // Sabot presque vide (6 jeux = 312 cartes) : on remélange
                if (cardsUsed > 200) {
                    repository.reshuffle(id)
                    cardsUsed = 0
                }
                var index = 0
                repository.dealCards(id, 4).collect { card ->
                    if (index % 2 == 0) playerCards.value = playerCards.value + card
                    else dealerCards.value = dealerCards.value + card
                    index++
                }
                cardsUsed += 4

                // Un Blackjack d'entrée termine la main tout de suite
                if (isBlackjack(playerCards.value) || isBlackjack(dealerCards.value)) {
                    finishRound()
                } else {
                    phase.value = GamePhase.PLAYER_TURN
                }
            } catch (e: Exception) {
                error.value = "Connexion au casino impossible. Vérifiez votre connexion."
                phase.value = GamePhase.WAITING
            } finally {
                isBusy.value = false
            }
        }
    }

    // Le joueur tire une carte
    fun hit() {
        if (isBusy.value || phase.value != GamePhase.PLAYER_TURN) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            try {
                val card = repository.draw(getDeckId(), 1).cards.first()
                cardsUsed++
                playerCards.value = playerCards.value + card
                val total = handValue(playerCards.value)
                if (total > 21) finishRound()   // perdu
                else if (total == 21) dealerPlay() // 21 : on reste automatiquement
            } catch (e: Exception) {
                error.value = "Connexion perdue, réessayez."
            } finally {
                isBusy.value = false
            }
        }
    }

    // Le joueur reste : c'est au croupier
    fun stand() {
        if (isBusy.value || phase.value != GamePhase.PLAYER_TURN) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            try {
                dealerPlay()
            } catch (e: Exception) {
                error.value = "Connexion perdue, réessayez."
                phase.value = GamePhase.PLAYER_TURN
            } finally {
                isBusy.value = false
            }
        }
    }

    // Le croupier retourne sa carte puis tire tant qu'il a moins de 17
    private suspend fun dealerPlay() {
        phase.value = GamePhase.DEALER_TURN
        delay(600)
        while (handValue(dealerCards.value) < 17) {
            val card = repository.draw(getDeckId(), 1).cards.first()
            cardsUsed++
            dealerCards.value = dealerCards.value + card
            delay(600)
        }
        finishRound()
    }

    private fun finishRound() {
        result.value = computeResult(playerCards.value, dealerCards.value)
        phase.value = GamePhase.FINISHED
    }
}