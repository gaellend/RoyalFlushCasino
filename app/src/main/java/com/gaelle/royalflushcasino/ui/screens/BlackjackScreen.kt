package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.game.CHIP_VALUES
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.canDouble
import com.gaelle.royalflushcasino.game.canSplit
import com.gaelle.royalflushcasino.game.canSurrender
import com.gaelle.royalflushcasino.game.doubleAllowed
import com.gaelle.royalflushcasino.game.handValue
import com.gaelle.royalflushcasino.game.hasSplittablePair
import com.gaelle.royalflushcasino.ui.components.ActionButton
import com.gaelle.royalflushcasino.ui.components.BetStack
import com.gaelle.royalflushcasino.ui.components.CardShoe
import com.gaelle.royalflushcasino.ui.components.CasinoChip
import com.gaelle.royalflushcasino.ui.components.FeltInscription
import com.gaelle.royalflushcasino.ui.components.HandRow
import com.gaelle.royalflushcasino.ui.components.ResultBanner
import com.gaelle.royalflushcasino.ui.components.ScorePill
import com.gaelle.royalflushcasino.ui.components.TableHeader
import com.gaelle.royalflushcasino.ui.theme.ActionBlue
import com.gaelle.royalflushcasino.ui.theme.ActionDark
import com.gaelle.royalflushcasino.ui.theme.ActionGreen
import com.gaelle.royalflushcasino.ui.theme.ActionGrey
import com.gaelle.royalflushcasino.ui.theme.ActionOrange
import com.gaelle.royalflushcasino.ui.theme.ActionRed
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.FeltGreenDark
import com.gaelle.royalflushcasino.ui.theme.FeltGreenLight
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.viewmodel.BlackjackViewModel

// Largeur de chaque emplacement de bouton en bas : tous identiques pour des écarts symétriques
private val SLOT_WIDTH = 76.dp

// Décalage vers le bas des petits boutons pour que leur centre soit aligné sur celui des gros
// (gros : 64 dp, petits : 48 dp → (64 - 48) / 2 = 8 dp)
private val SMALL_BUTTON_TOP = 8.dp

@Composable
fun BlackjackScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<BlackjackViewModel>()
    val hands by viewModel.hands.collectAsStateWithLifecycle()
    val activeHand by viewModel.activeHand.collectAsStateWithLifecycle()
    val dealerCards by viewModel.dealerCards.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val bet by viewModel.bet.collectAsStateWithLifecycle()
    val lastNet by viewModel.lastNet.collectAsStateWithLifecycle()
    val remaining by viewModel.remaining.collectAsStateWithLifecycle()
    val isShuffling by viewModel.isShuffling.collectAsStateWithLifecycle()

    var shoePosition by remember { mutableStateOf<Offset?>(null) }

    val hideDealerCard = phase == GamePhase.DEALING || phase == GamePhase.PLAYER_TURN
    val bettingTime = !isBusy && (phase == GamePhase.WAITING || phase == GamePhase.FINISHED)
    val playerTurn = phase == GamePhase.PLAYER_TURN
    val currentHand = hands.getOrNull(activeHand)

    // Mise affichée : celle qu'on compose entre deux mains, sinon tout ce qui est sur la table
    val shownBet = if (bettingTime || hands.isEmpty()) bet else hands.sumOf { it.bet }

    val felt = Brush.radialGradient(listOf(FeltGreenLight, FeltGreenDark))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(felt)             // le tapis remonte jusqu'en haut de l'écran
            .systemBarsPadding()          // le contenu évite l'heure / la batterie
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- En-tête : flèche retour + solde ---
        TableHeader(balance = balance, onBack = onBack)

        Spacer(Modifier.weight(0.6f)) // espace libre en haut

        // --- Ligne du croupier : cartes au centre, sabot à droite ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HandRow(cards = dealerCards, hideSecondCard = hideDealerCard, shoePosition = shoePosition)
                Spacer(Modifier.height(12.dp))
                ScorePill(
                    text = if (hideDealerCard) "${handValue(dealerCards.take(1))}" else "${handValue(dealerCards)}",
                    visible = dealerCards.isNotEmpty()
                )
            }
            CardShoe(
                remaining = remaining,
                isShuffling = isShuffling,
                onPositioned = { shoePosition = it },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-70).dp) // sabot remonté
            )
        }

        Spacer(Modifier.height(20.dp))

        // --- Centre du tapis : pile de mise à gauche, message / résultat au centre ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
        ) {
            BetStack(
                amount = shownBet,
                onClick = { viewModel.removeTopChip() },
                enabled = bettingTime,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = 120.dp) // ← hauteur de la pile de mise : plus grand = plus bas
            )

            Box(modifier = Modifier.align(Alignment.Center)) {
                val currentError = error
                val net = lastNet
                when {
                    currentError != null -> Column(
                        modifier = Modifier.widthIn(max = 230.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentError, color = Ivory, textAlign = TextAlign.Center)
                        if (phase == GamePhase.DEALER_TURN && !isBusy) {
                            Button(onClick = { viewModel.retryDealer() }) { Text("Réessayer") }
                        }
                    }
                    isShuffling -> Text(
                        "Remélange du sabot…",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold
                    )
                    phase == GamePhase.FINISHED && net != null -> {
                        // 1 main : sa phrase ; 2 mains : le résultat de chacune
                        val message = if (hands.size == 1) {
                            hands[0].result?.message ?: ""
                        } else {
                            hands.mapIndexed { i, h -> "Main ${i + 1} : ${h.result?.short ?: ""}" }
                                .joinToString("  ·  ")
                        }
                        // Largeur limitée pour ne jamais chevaucher la pile de mise
                        ResultBanner(
                            message = message,
                            net = net,
                            modifier = Modifier.widthIn(max = 230.dp)
                        )
                    }
                    else -> FeltInscription()
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- Joueur : main(s) au centre, bouton Séparer à droite quand on a une paire ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (hands.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ScorePill(text = "", visible = false)
                        Spacer(Modifier.height(12.dp))
                        HandRow(cards = emptyList(), shoePosition = shoePosition)
                    }
                }
                hands.forEachIndexed { index, hand ->
                    // Après un split, la main qui n'est pas en cours est estompée
                    val dimmed = hands.size > 1 && playerTurn && index != activeHand
                    Column(
                        modifier = Modifier.alpha(if (dimmed) 0.5f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScorePill(text = "${hand.value}", visible = hand.cards.isNotEmpty())
                        Spacer(Modifier.height(12.dp))
                        HandRow(
                            cards = hand.cards,
                            shoePosition = shoePosition,
                            overlap = if (hands.size > 1) (-55).dp else (-40).dp
                        )
                        if (hands.size > 1) {
                            Text(
                                text = hand.result?.short ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = Gold
                            )
                        }
                    }
                }
            }

            // Séparer : apparaît à droite de la main, seulement avec une paire
            if (playerTurn && hasSplittablePair(hands)) {
                val affordable = canSplit(hands, balance)
                ActionButton(
                    iconRes = R.drawable.ic_split,
                    label = if (affordable) "Séparer" else "Pas assez\nde jetons",
                    color = ActionBlue,
                    onClick = { viewModel.split() },
                    enabled = !isBusy && affordable,
                    size = 48.dp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        Spacer(Modifier.weight(1f)) // espace libre entre le jeu et les jetons

        // --- Jetons pour miser : place toujours réservée, invisibles pendant la main ---
        Box(
            modifier = Modifier.height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (bettingTime && balance == 0 && bet == 0) {
                Button(onClick = { viewModel.refill() }) { Text("Plus de jetons ? Le casino vous offre 1 000") }
            } else {
                Row(
                    modifier = Modifier.alpha(if (bettingTime) 1f else 0f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CHIP_VALUES.forEach { value ->
                        CasinoChip(
                            value = value,
                            enabled = bettingTime && bet + value <= balance,
                            onClick = { viewModel.addChip(value) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp)) // jetons un peu plus hauts

        // --- Boutons : 4 emplacements de même largeur → Doubler | principal | principal | Abandonner ---
        // Alignés par le haut : un libellé sur 2 lignes s'étend vers le bas sans faire monter le bouton
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emplacement 1 : Doubler (vide si non disponible)
            Box(modifier = Modifier.width(SLOT_WIDTH), contentAlignment = Alignment.TopCenter) {
                if (playerTurn && currentHand != null && doubleAllowed(currentHand)) {
                    val affordable = canDouble(currentHand, balance)
                    ActionButton(
                        iconText = "×2",
                        label = if (affordable) "Doubler" else "Pas assez\nde jetons",
                        color = ActionOrange,
                        onClick = { viewModel.doubleDown() },
                        enabled = !isBusy && affordable,
                        size = 48.dp,
                        modifier = Modifier.padding(top = SMALL_BUTTON_TOP)
                    )
                }
            }

            // Emplacements 2 et 3 : les boutons principaux, toujours à la même place
            Box(modifier = Modifier.width(SLOT_WIDTH), contentAlignment = Alignment.TopCenter) {
                if (playerTurn) {
                    ActionButton(
                        iconRes = R.drawable.ic_cards,
                        label = "Tirer",
                        color = ActionGreen,
                        onClick = { viewModel.hit() },
                        enabled = !isBusy
                    )
                } else {
                    ActionButton(
                        iconRes = R.drawable.ic_close,
                        label = "Effacer",
                        color = ActionGrey,
                        onClick = { viewModel.clearBet() },
                        enabled = bettingTime && bet > 0
                    )
                }
            }
            Box(modifier = Modifier.width(SLOT_WIDTH), contentAlignment = Alignment.TopCenter) {
                if (playerTurn) {
                    ActionButton(
                        iconRes = R.drawable.ic_hand,
                        label = "Rester",
                        color = ActionRed,
                        onClick = { viewModel.stand() },
                        enabled = !isBusy
                    )
                } else {
                    ActionButton(
                        iconRes = R.drawable.ic_deal,
                        label = if (phase == GamePhase.FINISHED) "Rejouer" else "Distribuer",
                        color = Gold,
                        iconTint = CasinoBlack,
                        onClick = { viewModel.deal() },
                        enabled = bettingTime && bet > 0
                    )
                }
            }

            // Emplacement 4 : Abandonner (vide si non disponible)
            Box(modifier = Modifier.width(SLOT_WIDTH), contentAlignment = Alignment.TopCenter) {
                if (playerTurn && canSurrender(hands)) {
                    ActionButton(
                        iconRes = R.drawable.ic_flag,
                        label = "Abandonner",
                        color = ActionDark,
                        onClick = { viewModel.surrender() },
                        enabled = !isBusy,
                        size = 48.dp,
                        modifier = Modifier.padding(top = SMALL_BUTTON_TOP)
                    )
                }
            }
        }
    }
}