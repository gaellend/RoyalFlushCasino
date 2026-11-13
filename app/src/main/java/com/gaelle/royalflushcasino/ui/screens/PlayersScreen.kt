package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.game.STARTING_BALANCE
import com.gaelle.royalflushcasino.game.isRuined
import com.gaelle.royalflushcasino.ui.components.AppearingItem
import com.gaelle.royalflushcasino.ui.components.BackHeader
import com.gaelle.royalflushcasino.ui.components.CasinoBackground
import com.gaelle.royalflushcasino.ui.components.ChipFace
import com.gaelle.royalflushcasino.ui.components.PlayerAvatar
import com.gaelle.royalflushcasino.ui.components.formatChips
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.CasinoSurface
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import com.gaelle.royalflushcasino.viewmodel.MAX_NAME_LENGTH
import com.gaelle.royalflushcasino.viewmodel.PlayersViewModel

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(), // évite aussi le clavier quand il s'ouvre
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackHeader(
                title = "Qui joue ?",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- Création d'un joueur ---
                item {
                    NewPlayerCard(
                        name = newName,
                        onNameChange = {
                            if (it.length <= MAX_NAME_LENGTH) newName = it
                            viewModel.clearError()
                        },
                        error = error,
                        onCreate = { viewModel.createPlayer(newName) { id -> onPlayerSelected(id) } }
                    )
                }

                // --- Titre de la liste ---
                item {
                    Text(
                        text = if (players.isEmpty()) "Aucun joueur pour l'instant : créez le premier !"
                        else "Joueurs (${players.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (players.isEmpty()) Ivory.copy(alpha = 0.7f) else Gold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                    )
                }

                // --- Joueurs existants ---
                itemsIndexed(players, key = { _, player -> player.id }) { index, player ->
                    AppearingItem(index = index) {
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
            onDismissRequest = { playerToDelete = null },
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

// Carte de création : champ arrondi au liseré doré + bouton doré
@Composable
private fun NewPlayerCard(
    name: String,
    onNameChange: (String) -> Unit,
    error: String?,
    onCreate: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        color = CasinoSurface,
        shape = shape,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.35f), shape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nouveau joueur", style = MaterialTheme.typography.titleSmall, color = Gold)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Pseudo") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Gold.copy(alpha = 0.4f),
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Ivory.copy(alpha = 0.6f),
                        cursorColor = Gold,
                        focusedTextColor = Ivory,
                        unfocusedTextColor = Ivory
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onCreate,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = CasinoBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
                ) {
                    Text("Jouer", fontWeight = FontWeight.Bold)
                }
            }
            error?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = CasinoRed, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// Une ligne de joueur : avatar, pseudo et record, solde (ou ruine), corbeille
@Composable
private fun PlayerRow(
    player: PlayerEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val broke = isRuined(player.balance)
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        color = CasinoSurface,
        shape = shape,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.25f), shape)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(name = player.name, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium, color = Ivory)
                Text(
                    text = "Record : ${formatChips(player.bestBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gold.copy(alpha = 0.8f)
                )
            }
            if (broke) {
                Column(horizontalAlignment = Alignment.End) {
                    RuinedBadge()
                    Text(
                        text = "Recommencer à ${formatChips(STARTING_BALANCE)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Ivory.copy(alpha = 0.6f)
                    )
                }
            } else {
                ChipFace(color = CasinoRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatChips(player.balance),
                    color = Ivory,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Supprimer ${player.name}",
                    tint = Ivory.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Badge rouge "Ruiné"
@Composable
private fun RuinedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CasinoRed)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text("Ruiné", style = MaterialTheme.typography.labelSmall, color = Ivory, fontWeight = FontWeight.Bold)
    }
}