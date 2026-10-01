package com.gaelle.royalflushcasino.viewmodel

import com.gaelle.royalflushcasino.game.chipsFor
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.gaelle.royalflushcasino.data.Card
import com.gaelle.royalflushcasino.data.DeckRepository
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.PlayerHand
import com.gaelle.royalflushcasino.game.canSplit
import com.gaelle.royalflushcasino.game.computeResult
import com.gaelle.royalflushcasino.game.handValue
import com.gaelle.royalflushcasino.game.isBlackjack
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

const val STARTING_BALANCE = 1000
const val RESHUFFLE_LIMIT = 60

class BlackjackViewModel(application: Application) : AndroidViewModel(application) {

    // --- Partie ---
    val hands = MutableStateFlow<List<PlayerHand>>(emptyList()) // 1 main, ou 2 après un split
    val activeHand = MutableStateFlow(0)                         // main en cours de jeu
    val dealerCards = MutableStateFlow<List<Card>>(emptyList())
    val phase = MutableStateFlow(GamePhase.WAITING)
    val isBusy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    // --- Sabot (API) ---
    val remaining = MutableStateFlow<Int?>(null)
    val isShuffling = MutableStateFlow(false)

    // --- Argent ---
    val balance = MutableStateFlow(STARTING_BALANCE)
    val bet = MutableStateFlow(0)
    val lastNet = MutableStateFlow<Int?>(null)

    private val repository = DeckRepository()
    private var deckId: String? = null

    init {
        viewModelScope.launch {
            isBusy.value = true
            try {
                getDeckId()
            } catch (e: Exception) {
                // on réessaiera à la première distribution
            } finally {
                isBusy.value = false
            }
        }
    }

    // ---------- Outils ----------

    private suspend fun preload(card: Card) {
        val context = getApplication<Application>()
        val request = ImageRequest.Builder(context).data(card.image).build()
        SingletonImageLoader.get(context).execute(request)
    }

    private suspend fun getDeckId(): String {
        deckId?.let { return it }
        val response = repository.newDeck()
        deckId = response.deckId
        remaining.value = response.remaining
        return response.deckId
    }

    private suspend fun drawOne(): Card {
        val response = repository.draw(getDeckId(), 1)
        remaining.value = response.remaining
        val card = response.cards.first()
        preload(card)
        return card
    }

    // Modifie une seule main (en créant une nouvelle liste pour que le StateFlow prévienne l'interface)
    private fun updateHand(index: Int, transform: (PlayerHand) -> PlayerHand) {
        hands.value = hands.value.mapIndexed { i, hand -> if (i == index) transform(hand) else hand }
    }

    // Exécute une action du joueur : vérifie que c'est son tour, gère "occupé" et les erreurs réseau
    private fun playerAction(block: suspend () -> Unit) {
        if (isBusy.value || phase.value != GamePhase.PLAYER_TURN) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            try {
                block()
            } catch (e: Exception) {
                error.value = "Connexion perdue, réessayez."
            } finally {
                isBusy.value = false
            }
        }
    }

    // ---------- Mises ----------

    private fun canBet(): Boolean =
        !isBusy.value && (phase.value == GamePhase.WAITING || phase.value == GamePhase.FINISHED)

    fun addChip(value: Int) {
        if (canBet() && bet.value + value <= balance.value) bet.value += value
    }

    fun clearBet() {
        if (canBet()) bet.value = 0
    }

    // Retire le jeton du dessus de la pile (le plus petit de la décomposition)
    fun removeTopChip() {
        if (!canBet() || bet.value <= 0) return
        val chips = chipsFor(bet.value)
        bet.value = if (chips.isEmpty()) 0 else bet.value - chips.last()
    }

    fun refill() {
        if (canBet() && balance.value == 0) balance.value = STARTING_BALANCE
    }

    // ---------- Distribution ----------

    fun deal() {
        if (!canBet() || bet.value <= 0 || bet.value > balance.value) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            lastNet.value = null
            hands.value = listOf(PlayerHand(bet = bet.value))
            activeHand.value = 0
            dealerCards.value = emptyList()
            balance.value -= bet.value
            phase.value = GamePhase.DEALING
            try {
                val id = getDeckId()
                if ((remaining.value ?: 0) < RESHUFFLE_LIMIT) {
                    isShuffling.value = true
                    val response = repository.reshuffle(id)
                    delay(1200)
                    remaining.value = response.remaining
                    isShuffling.value = false
                }

                var index = 0
                repository.dealCards(id, 4)
                    .onEach { drawn -> preload(drawn.card) }
                    .collect { drawn ->
                        remaining.value = drawn.remaining
                        if (index % 2 == 0) updateHand(0) { it.copy(cards = it.cards + drawn.card) }
                        else dealerCards.value = dealerCards.value + drawn.card
                        index++
                    }

                if (isBlackjack(hands.value[0].cards) || isBlackjack(dealerCards.value)) {
                    finishRound()
                } else {
                    phase.value = GamePhase.PLAYER_TURN
                }
            } catch (e: Exception) {
                error.value = "Connexion au casino impossible. Vérifiez votre connexion."
                isShuffling.value = false
                balance.value += bet.value
                hands.value = emptyList()
                dealerCards.value = emptyList()
                phase.value = GamePhase.WAITING
            } finally {
                isBusy.value = false
            }
        }
    }

    // ---------- Actions du joueur ----------

    fun hit() = playerAction {
        val i = activeHand.value
        val card = drawOne()
        updateHand(i) { it.copy(cards = it.cards + card) }
        if (hands.value[i].isDone) nextHand() // plus de 21 ou 21 pile : main suivante
    }

    fun stand() = playerAction {
        updateHand(activeHand.value) { it.copy(stood = true) }
        nextHand()
    }

    fun split() = playerAction {
        val current = hands.value
        if (!canSplit(current, balance.value)) return@playerAction
        val hand = current[0]
        val aces = hand.cards[0].value == "ACE"

        // On pioche d'abord : si le réseau coupe, rien n'a été payé ni modifié
        val newCard1 = drawOne()
        val newCard2 = drawOne()
        balance.value -= hand.bet

        // Les 2 cartes deviennent 2 mains avec la même mise
        hands.value = listOf(
            PlayerHand(cards = listOf(hand.cards[0]), bet = hand.bet, fromSplit = true, splitAces = aces),
            PlayerHand(cards = listOf(hand.cards[1]), bet = hand.bet, fromSplit = true, splitAces = aces)
        )
        activeHand.value = 0
        delay(300)
        updateHand(0) { it.copy(cards = it.cards + newCard1) }
        delay(450)
        updateHand(1) { it.copy(cards = it.cards + newCard2) }

        // As séparés : une seule carte chacun, les deux mains sont terminées
        if (aces) {
            updateHand(0) { it.copy(stood = true) }
            updateHand(1) { it.copy(stood = true) }
        }
        if (hands.value[0].isDone) nextHand()
    }

    // Passe à la main suivante, ou au croupier si toutes les mains sont jouées
    private suspend fun nextHand() {
        val next = hands.value.indices.firstOrNull { it > activeHand.value && !hands.value[it].isDone }
        if (next != null) {
            activeHand.value = next
            return
        }
        if (hands.value.all { it.isBust }) {
            // Toutes les mains ont dépassé 21 : le croupier retourne juste sa carte
            phase.value = GamePhase.DEALER_TURN
            delay(700)
            finishRound()
        } else {
            dealerPlay()
        }
    }

    // ---------- Croupier ----------

    private suspend fun dealerPlay() {
        phase.value = GamePhase.DEALER_TURN
        delay(700)
        while (handValue(dealerCards.value) < 17) {
            dealerCards.value = dealerCards.value + drawOne()
            delay(600)
        }
        finishRound()
    }

    // Si le réseau a coupé pendant le tour du croupier
    fun retryDealer() {
        if (isBusy.value || phase.value != GamePhase.DEALER_TURN) return
        viewModelScope.launch {
            isBusy.value = true
            error.value = null
            try {
                dealerPlay()
            } catch (e: Exception) {
                error.value = "Connexion perdue, réessayez."
            } finally {
                isBusy.value = false
            }
        }
    }

    // ---------- Fin de la main ----------

    private fun finishRound() {
        val dealer = dealerCards.value
        val resolved = hands.value.map { it.copy(result = computeResult(it, dealer)) }
        hands.value = resolved

        val payout = resolved.sumOf { hand -> (hand.bet * (hand.result?.payout ?: 0.0)).toInt() }
        val staked = resolved.sumOf { it.bet }
        balance.value += payout
        lastNet.value = payout - staked
        phase.value = GamePhase.FINISHED
        if (bet.value > balance.value) bet.value = balance.value
    }
}