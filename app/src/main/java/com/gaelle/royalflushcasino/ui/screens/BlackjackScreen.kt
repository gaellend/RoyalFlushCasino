package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.gaelle.royalflushcasino.ui.components.HandRow
import com.gaelle.royalflushcasino.ui.theme.FeltGreen
import com.gaelle.royalflushcasino.ui.theme.Gold
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

    // La 2e carte du croupier reste cachée tant que le joueur n'a pas fini
    val hideDealerCard = phase == GamePhase.DEALING || phase == GamePhase.PLAYER_TURN

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FeltGreen)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        // --- Centre de la table : message, résultat ou chargement ---
        Box(modifier = Modifier.height(64.dp), contentAlignment = Alignment.Center) {
            val currentError = error
            val currentResult = result
            when {
                currentError != null -> Text(currentError, textAlign = TextAlign.Center)
                currentResult != null -> Text(
                    text = currentResult.message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                    textAlign = TextAlign.Center
                )
                phase == GamePhase.DEALING || phase == GamePhase.DEALER_TURN ->
                    CircularProgressIndicator(color = Gold)
                phase == GamePhase.WAITING -> Text("Appuyez sur Distribuer pour commencer")
            }
        }

        Spacer(Modifier.weight(1f))

        // --- Joueur ---
        HandRow(cards = playerCards)
        Spacer(Modifier.height(8.dp))
        Text("Vous", style = MaterialTheme.typography.titleMedium, color = Gold)
        Text(if (playerCards.isEmpty()) "" else "Score : ${handValue(playerCards)}")

        Spacer(Modifier.height(24.dp))

        // --- Boutons ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (phase == GamePhase.PLAYER_TURN) {
                Button(onClick = { viewModel.hit() }, enabled = !isBusy) { Text("Tirer") }
                Button(onClick = { viewModel.stand() }, enabled = !isBusy) { Text("Rester") }
            } else {
                Button(
                    onClick = { viewModel.deal() },
                    enabled = phase == GamePhase.WAITING || phase == GamePhase.FINISHED
                ) {
                    Text(if (phase == GamePhase.FINISHED) "Nouvelle main" else "Distribuer")
                }
            }
            OutlinedButton(onClick = onBack) { Text("Hall") }
        }
    }
}