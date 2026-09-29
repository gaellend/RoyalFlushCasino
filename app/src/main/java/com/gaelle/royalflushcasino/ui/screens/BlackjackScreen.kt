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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.handValue
import com.gaelle.royalflushcasino.ui.components.CasinoChip
import com.gaelle.royalflushcasino.ui.components.HandRow
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.FeltGreen
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.viewmodel.BlackjackViewModel

@Composable
fun BlackjackScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<BlackjackViewModel>()
    val playerCards by viewModel.playerCards.collectAsStateWithLifecycle()
    val dealerCards by viewModel.dealerCards.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val bet by viewModel.bet.collectAsStateWithLifecycle()
    val lastNet by viewModel.lastNet.collectAsStateWithLifecycle()

    val hideDealerCard = phase == GamePhase.DEALING || phase == GamePhase.PLAYER_TURN
    val bettingTime = !isBusy && (phase == GamePhase.WAITING || phase == GamePhase.FINISHED)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FeltGreen)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Solde ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Solde : $balance", color = Gold, fontWeight = FontWeight.Bold)
            Text("Mise : $bet", color = Gold, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        // --- Croupier ---
        Text("Croupier", style = MaterialTheme.typography.titleMedium, color = Gold)
        val dealerScore = when {
            dealerCards.isEmpty() -> ""
            hideDealerCard -> "Score : ${handValue(dealerCards.take(1))} + ?"
            else -> "Score : ${handValue(dealerCards)}"
        }
        Text(dealerScore)
        Spacer(Modifier.height(8.dp))
        HandRow(cards = dealerCards, hideSecondCard = hideDealerCard)

        Spacer(Modifier.weight(1f))

        // --- Centre de la table ---
        Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
            val currentError = error
            val currentResult = result
            val net = lastNet
            when {
                currentError != null -> Text(currentError, textAlign = TextAlign.Center)
                currentResult != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentResult.message,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        textAlign = TextAlign.Center
                    )
                    if (net != null) {
                        Text(
                            text = when {
                                net > 0 -> "+$net jetons"
                                net < 0 -> "$net jetons"
                                else -> "Mise remboursée"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                phase == GamePhase.DEALING || phase == GamePhase.DEALER_TURN ->
                    CircularProgressIndicator(color = Gold)
                phase == GamePhase.WAITING -> Text("Placez votre mise puis distribuez")
            }
        }

        Spacer(Modifier.weight(1f))

        // --- Joueur ---
        HandRow(cards = playerCards)
        Spacer(Modifier.height(8.dp))
        Text("Vous", style = MaterialTheme.typography.titleMedium, color = Gold)
        Text(if (playerCards.isEmpty()) "" else "Score : ${handValue(playerCards)}")

        Spacer(Modifier.height(16.dp))

        // --- Zone de mise (seulement entre deux mains) ---
        if (bettingTime) {
            if (balance == 0 && bet == 0) {
                Text("Vous n'avez plus de jetons !")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.refill() }) {
                    Text("Le casino vous offre 1000 jetons")
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CasinoChip(10, Ivory, CasinoBlack, bet + 10 <= balance, { viewModel.addChip(10) })
                    CasinoChip(50, CasinoRed, Ivory, bet + 50 <= balance, { viewModel.addChip(50) })
                    CasinoChip(100, CasinoBlack, Ivory, bet + 100 <= balance, { viewModel.addChip(100) })
                    CasinoChip(500, Gold, CasinoBlack, bet + 500 <= balance, { viewModel.addChip(500) })
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- Boutons ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (phase == GamePhase.PLAYER_TURN) {
                Button(onClick = { viewModel.hit() }, enabled = !isBusy) { Text("Tirer") }
                Button(onClick = { viewModel.stand() }, enabled = !isBusy) { Text("Rester") }
            } else {
                Button(
                    onClick = { viewModel.deal() },
                    enabled = bettingTime && bet > 0
                ) {
                    Text(if (phase == GamePhase.FINISHED) "Nouvelle main" else "Distribuer")
                }
                OutlinedButton(
                    onClick = { viewModel.clearBet() },
                    enabled = bettingTime && bet > 0
                ) {
                    Text("Effacer")
                }
            }
            OutlinedButton(onClick = onBack) { Text("Hall") }
        }
    }
}