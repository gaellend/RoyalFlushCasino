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

const val STARTING_BALANCE = 1000

class BlackjackViewModel : ViewModel() {

    // États de la partie
    val playerCards = MutableStateFlow<List<Card>>(emptyList())
    val dealerCards = MutableStateFlow<List<Card>>(emptyList())
    val phase = MutableStateFlow(GamePhase.WAITING)
    val result = MutableStateFlow<GameResult?>(null)
    val isBusy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    // États de l'argent
    val balance = MutableStateFlow(STARTING_BALANCE) // jetons du joueur
    val bet = MutableStateFlow(0)                     // mise en cours
    val lastNet = MutableStateFlow<Int?>(null)        // gain ou perte de la dernière main

    private val repository = DeckRepository()
    private var deckId: String? = null
    private var cardsUsed = 0

    // On ne peut miser qu'entre deux mains
    private fun canBet(): Boolean =
        !isBusy.value && (phase.value == GamePhase.WAITING || phase.value == GamePhase.FINISHED)

    fun addChip(value: Int) {
        if (canBet() && bet.value + value <= balance.value) {
            bet.value += value
        }
    }

    fun clearBet() {
        if (canBet()) bet.value = 0
    }

    // Recharge si le joueur n'a plus rien (remplacé plus tard par l'écran "Fauché")
    fun refill() {
        if (canBet() && balance.value == 0) {
            balance.value = STARTING_BALANCE
        }
    }

    private suspend fun getDeckId(): String {
        deckId?.let { return it }
        val newId = repository.newDeck().deckId
        deckId = newId
        cardsUsed = 0
        return newId
    }

    fun deal() {
        if (!canBet() || bet.value <= 0 || bet.value > balance.value) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            result.value = null
            lastNet.value = null
            playerCards.value = emptyList()
            dealerCards.value = emptyList()
            balance.value -= bet.value // la mise est posée sur la table
            phase.value = GamePhase.DEALING
            try {
                val id = getDeckId()
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

                if (isBlackjack(playerCards.value) || isBlackjack(dealerCards.value)) {
                    finishRound()
                } else {
                    phase.value = GamePhase.PLAYER_TURN
                }
            } catch (e: Exception) {
                error.value = "Connexion au casino impossible. Vérifiez votre connexion."
                balance.value += bet.value // on rend la mise
                playerCards.value = emptyList()
                dealerCards.value = emptyList()
                phase.value = GamePhase.WAITING
            } finally {
                isBusy.value = false
            }
        }
    }

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
                if (total > 21) finishRound()
                else if (total == 21) dealerPlay()
            } catch (e: Exception) {
                error.value = "Connexion perdue, réessayez."
            } finally {
                isBusy.value = false
            }
        }
    }

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
        val r = computeResult(playerCards.value, dealerCards.value)
        val payout = (bet.value * r.payout).toInt()
        balance.value += payout
        lastNet.value = payout - bet.value
        result.value = r
        phase.value = GamePhase.FINISHED
        // On garde la même mise pour la main suivante si le solde le permet
        if (bet.value > balance.value) bet.value = balance.value
    }
}