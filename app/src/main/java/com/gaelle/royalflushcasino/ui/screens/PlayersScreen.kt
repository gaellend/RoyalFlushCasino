package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.ui.components.BackHeader
import com.gaelle.royalflushcasino.ui.components.ChipFace
import com.gaelle.royalflushcasino.ui.components.formatChips
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.CasinoSurface
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.viewmodel.MAX_NAME_LENGTH
import com.gaelle.royalflushcasino.viewmodel.PlayersViewModel
import com.gaelle.royalflushcasino.game.isRuined

@Composable
fun PlayersScreen(
    onBack: () -> Unit,
    onPlayerSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<PlayersViewModel>()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Texte en cours de saisie (état local de l'écran)
    var newName by remember { mutableStateOf("") }

    // Joueur dont on demande la suppression (null = pas de fenêtre de confirmation)
    var playerToDelete by remember { mutableStateOf<PlayerEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding() // évite aussi le clavier quand il s'ouvre
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackHeader(title = "Qui joue ?", onBack = onBack)

        Column(modifier = Modifier.widthIn(max = 500.dp)) {
            Spacer(Modifier.height(16.dp))

            // --- Nouveau joueur ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        if (it.length <= MAX_NAME_LENGTH) newName = it
                        viewModel.clearError()
                    },
                    label = { Text("Nouveau pseudo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.createPlayer(newName) { id -> onPlayerSelected(id) } }
                ) {
                    Text("Jouer")
                }
            }
            error?.let {
                Text(it, color = CasinoRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            // --- Joueurs existants ---
            if (players.isEmpty()) {
                Text(
                    "Aucun joueur pour l'instant : créez le premier !",
                    color = Ivory.copy(alpha = 0.7f)
                )
            } else {
                Text("Joueurs", style = MaterialTheme.typography.titleMedium, color = Gold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(players, key = { it.id }) { player ->
                        PlayerRow(
                            player = player,
                            onClick = { viewModel.selectPlayer(player) { id -> onPlayerSelected(id) } },
                            onDelete = { playerToDelete = player }
                        )
                    }
                }
            }
        }
    }

    // --- Fenêtre de confirmation avant suppression ---
    playerToDelete?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToDelete = null }, // toucher à côté = annuler
            title = { Text("Supprimer ${player.name} ?") },
            text = { Text("Son solde, son record et toutes ses mains seront définitivement effacés.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlayer(player.id)
                        playerToDelete = null
                    }
                ) {
                    Text("Supprimer", color = CasinoRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// Une ligne de joueur : pseudo et record à gauche, solde et corbeille à droite.
// Un joueur ruiné peut recommencer avec le solde de départ (son record est conservé).
@Composable
private fun PlayerRow(
    player: PlayerEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val broke = isRuined(player.balance)
    val shape = RoundedCornerShape(12.dp)
    Surface(
        color = CasinoSurface,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ivory
                )
                Text(
                    text = "Record : ${formatChips(player.bestBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gold.copy(alpha = 0.8f)
                )
            }
            if (broke) {
                Text("Ruiné · Recommencer", color = CasinoRed, fontWeight = FontWeight.Bold)
            } else {
                ChipFace(color = CasinoRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(formatChips(player.balance), color = Ivory, fontWeight = FontWeight.Bold)
            }
            // Corbeille : ouvre la confirmation (ne lance pas la partie)
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Supprimer ${player.name}",
                    tint = Ivory.copy(alpha = 0.6f)
                )
            }
        }
    }
}