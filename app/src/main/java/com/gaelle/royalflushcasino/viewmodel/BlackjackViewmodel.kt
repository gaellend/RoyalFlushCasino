package com.gaelle.royalflushcasino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.gaelle.royalflushcasino.data.CasinoRepository
import com.gaelle.royalflushcasino.data.Card
import com.gaelle.royalflushcasino.data.DeckRepository
import com.gaelle.royalflushcasino.data.local.HandEntity
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.GameResult
import com.gaelle.royalflushcasino.game.PlayerHand
import com.gaelle.royalflushcasino.game.canDouble
import com.gaelle.royalflushcasino.game.canSplit
import com.gaelle.royalflushcasino.game.canSurrender
import com.gaelle.royalflushcasino.game.chipsFor
import com.gaelle.royalflushcasino.game.computeResult
import com.gaelle.royalflushcasino.game.handValue
import com.gaelle.royalflushcasino.game.isBlackjack
import com.gaelle.royalflushcasino.game.isRuined
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

const val RESHUFFLE_LIMIT = 60

class BlackjackViewModel(
    application: Application,
    private val playerId: Long // le joueur assis à la table
) : AndroidViewModel(application) {

    // --- Partie ---
    val hands = MutableStateFlow<List<PlayerHand>>(emptyList())
    val activeHand = MutableStateFlow(0)
    val dealerCards = MutableStateFlow<List<Card>>(emptyList())
    val phase = MutableStateFlow(GamePhase.WAITING)
    val isBusy = MutableStateFlow(true) // occupé tant que le joueur n'est pas chargé
    val error = MutableStateFlow<String?>(null)

    // --- Sabot (API) ---
    val remaining = MutableStateFlow<Int?>(null)
    val isShuffling = MutableStateFlow(false)

    // --- Argent ---
    val balance = MutableStateFlow(0)       // chargé depuis la base dans init
    val bet = MutableStateFlow(0)
    val lastNet = MutableStateFlow<Int?>(null)
    val ruined = MutableStateFlow(false)    // le joueur ne peut plus miser

    private val deckRepository = DeckRepository()          // l'API de cartes
    private val casinoRepository = CasinoRepository(application) // la base Room
    private var deckId: String? = null

    init {
        viewModelScope.launch {
            // 1. Le solde du joueur, lu une seule fois dans la base
            //    (ensuite, c'est la table qui le fait évoluer et qui le sauvegarde)
            val player = casinoRepository.player(playerId).first()
            balance.value = player?.balance ?: 0
            ruined.value = isRuined(balance.value)

            // 2. Le sabot, via l'API
            try {
                getDeckId()
            } catch (e: Exception) {
                // on réessaiera à la première distribution
            } finally {
                isBusy.value = false
            }
        }
    }

    // Recette de fabrication du ViewModel : comment le créer avec l'identifiant du joueur
    companion object {
        fun factory(playerId: Long) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                BlackjackViewModel(app, playerId)
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
        val response = deckRepository.newDeck()
        deckId = response.deckId
        remaining.value = response.remaining
        return response.deckId
    }

    private suspend fun drawOne(): Card {
        val response = deckRepository.draw(getDeckId(), 1)
        remaining.value = response.remaining
        val card = response.cards.first()
        preload(card)
        return card
    }

    private fun updateHand(index: Int, transform: (PlayerHand) -> PlayerHand) {
        hands.value = hands.value.mapIndexed { i, hand -> if (i == index) transform(hand) else hand }
    }

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
        !isBusy.value && !ruined.value &&
                (phase.value == GamePhase.WAITING || phase.value == GamePhase.FINISHED)

    fun addChip(value: Int) {
        if (canBet() && bet.value + value <= balance.value) bet.value += value
    }

    fun clearBet() {
        if (canBet()) bet.value = 0
    }

    fun removeTopChip() {
        if (!canBet() || bet.value <= 0) return
        val chips = chipsFor(bet.value)
        bet.value = if (chips.isEmpty()) 0 else bet.value - chips.last()
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
            persistBalance()
            phase.value = GamePhase.DEALING
            try {
                val id = getDeckId()
                if ((remaining.value ?: 0) < RESHUFFLE_LIMIT) {
                    isShuffling.value = true
                    val response = deckRepository.reshuffle(id)
                    delay(1200)
                    remaining.value = response.remaining
                    isShuffling.value = false
                }

                var index = 0
                deckRepository.dealCards(id, 4)
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
                persistBalance() // la distribution a échoué : on rend la mise
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
        if (hands.value[i].isDone) nextHand()
    }

    fun stand() = playerAction {
        updateHand(activeHand.value) { it.copy(stood = true) }
        nextHand()
    }

    fun doubleDown() = playerAction {
        val i = activeHand.value
        val hand = hands.value[i]
        if (!canDouble(hand, balance.value)) return@playerAction
        val card = drawOne()
        balance.value -= hand.bet
        persistBalance()
        updateHand(i) {
            it.copy(cards = it.cards + card, bet = it.bet * 2, doubled = true, stood = true)
        }
        nextHand()
    }

    fun split() = playerAction {
        val current = hands.value
        if (!canSplit(current, balance.value)) return@playerAction
        val hand = current[0]
        val aces = hand.cards[0].value == "ACE"

        val newCard1 = drawOne()
        val newCard2 = drawOne()
        balance.value -= hand.bet
        persistBalance()

        hands.value = listOf(
            PlayerHand(cards = listOf(hand.cards[0]), bet = hand.bet, fromSplit = true, splitAces = aces),
            PlayerHand(cards = listOf(hand.cards[1]), bet = hand.bet, fromSplit = true, splitAces = aces)
        )
        activeHand.value = 0
        delay(300)
        updateHand(0) { it.copy(cards = it.cards + newCard1) }
        delay(450)
        updateHand(1) { it.copy(cards = it.cards + newCard2) }

        if (aces) {
            updateHand(0) { it.copy(stood = true) }
            updateHand(1) { it.copy(stood = true) }
        }
        if (hands.value[0].isDone) nextHand()
    }

    fun surrender() = playerAction {
        if (!canSurrender(hands.value)) return@playerAction
        updateHand(0) { it.copy(result = GameResult.SURRENDER) }
        finishRound()
    }

    private suspend fun nextHand() {
        val next = hands.value.indices.firstOrNull { it > activeHand.value && !hands.value[it].isDone }
        if (next != null) {
            activeHand.value = next
            return
        }
        if (hands.value.all { it.isBust }) {
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

        // Ruiné : on bloque la table tout de suite (l'écran renverra au hall)
        if (isRuined(balance.value)) ruined.value = true

        saveRound(resolved, dealer)
    }

    // Sauvegarde immédiate du solde quand des jetons sont posés sur la table :
    // quitter la table en pleine main ne rend pas la mise
    private fun persistBalance() {
        val current = balance.value
        viewModelScope.launch {
            casinoRepository.updateBalance(playerId, current)
        }
    }

    // Sauvegarde dans la base : nouveau solde, mains jouées, et ruine éventuelle
    private fun saveRound(resolved: List<PlayerHand>, dealer: List<Card>) {
        val newBalance = balance.value
        val dealerScore = handValue(dealer)
        viewModelScope.launch {
            casinoRepository.updateBalance(playerId, newBalance) // met aussi à jour le record

            resolved.forEach { hand ->
                val result = hand.result ?: return@forEach
                val handPayout = (hand.bet * result.payout).toInt()
                casinoRepository.recordHand(
                    HandEntity(
                        playerId = playerId,
                        bet = hand.bet,
                        net = handPayout - hand.bet,
                        result = result.name, // "WIN", "BLACKJACK"…
                        playerScore = hand.value,
                        dealerScore = dealerScore
                    )
                )
            }

            if (isRuined(newBalance)) casinoRepository.markBroke(playerId)
        }
    }
}