package com.gaelle.royalflushcasino.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaelle.royalflushcasino.data.local.HandEntity
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.game.GameResult
import com.gaelle.royalflushcasino.ui.components.AppearingItem
import com.gaelle.royalflushcasino.ui.components.BackHeader
import com.gaelle.royalflushcasino.ui.components.CasinoBackground
import com.gaelle.royalflushcasino.ui.components.PlayerAvatar
import com.gaelle.royalflushcasino.ui.components.formatChips
import com.gaelle.royalflushcasino.ui.components.medalColor
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.CasinoSurface
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import com.gaelle.royalflushcasino.ui.theme.WinGreen
import com.gaelle.royalflushcasino.viewmodel.LeaderboardViewModel
import com.gaelle.royalflushcasino.viewmodel.PlayerDetail
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<LeaderboardViewModel>()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()

    // Comme dans le cours : le navigateur gère quel(s) panneau(x) afficher selon la taille d'écran
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val scope = rememberCoroutineScope()
    val selectedId = navigator.currentDestination?.contentKey

    LaunchedEffect(selectedId) {
        if (selectedId != null) viewModel.select(selectedId)
    }

    // Sur téléphone, le geste retour ramène du détail à la liste
    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            BackHeader(
                title = "Classement",
                onBack = {
                    if (navigator.canNavigateBack()) scope.launch { navigator.navigateBack() }
                    else onBack()
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        LeaderboardList(
                            players = players,
                            selectedId = selectedId,
                            onSelect = { id ->
                                scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
                            }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val current = detail
                        when {
                            selectedId == null -> EmptyDetail()
                            current == null || current.player.id != selectedId -> LoadingDetail()
                            else -> PlayerDetailPane(
                                detail = current,
                                rank = players.indexOfFirst { it.id == selectedId } + 1
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==================== La liste ====================

@Composable
private fun LeaderboardList(
    players: List<PlayerEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (players.isEmpty()) {
            item {
                Text(
                    "Aucun joueur pour l'instant : allez jouer !",
                    color = Ivory.copy(alpha = 0.7f)
                )
            }
        } else {
            // Les trois premiers sur le podium
            item(key = "podium") {
                Podium(top = players.take(3), selectedId = selectedId, onSelect = onSelect)
            }
        }
        // Les suivants, à partir du 4e
        itemsIndexed(players.drop(3), key = { _, player -> player.id }) { index, player ->
            AppearingItem(index = index) {
                LeaderboardRow(
                    rank = index + 4,
                    player = player,
                    selected = player.id == selectedId,
                    onClick = { onSelect(player.id) }
                )
            }
        }
    }
}

// Podium : 2e à gauche, 1er au centre (plus haut), 3e à droite
@Composable
private fun Podium(
    top: List<PlayerEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(1, 0, 2).forEach { position -> // indices dans "top" : 2e, 1er, 3e
            val player = top.getOrNull(position)
            Box(modifier = Modifier.weight(1f)) {
                if (player != null) {
                    AppearingItem(index = position) {
                        PodiumStep(
                            rank = position + 1,
                            player = player,
                            selected = player.id == selectedId,
                            onClick = { onSelect(player.id) }
                        )
                    }
                }
            }
        }
    }
}

// Une marche du podium : avatar, pseudo, record, puis la marche avec le rang
@Composable
private fun PodiumStep(
    rank: Int,
    player: PlayerEntity,
    selected: Boolean,
    onClick: () -> Unit
) {
    val medal = medalColor(rank)
    val stepHeight = when (rank) {
        1 -> 88.dp
        2 -> 64.dp
        else -> 48.dp
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerAvatar(
            name = player.name,
            size = if (rank == 1) 60.dp else 50.dp,
            modifier = Modifier.border(2.dp, if (selected) Gold else Color.Transparent, CircleShape)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleSmall,
            color = Ivory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatChips(player.bestBalance),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
            color = medal
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(stepHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(Brush.verticalGradient(listOf(medal, medal.copy(alpha = 0.35f)))),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
                color = CasinoBlack,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// Une ligne du classement (à partir du 4e) : rang, avatar, pseudo et infos, record
@Composable
private fun LeaderboardRow(
    rank: Int,
    player: PlayerEntity,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        color = CasinoSurface,
        shape = shape,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Gold else Gold.copy(alpha = 0.25f),
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                color = Ivory.copy(alpha = 0.7f),
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )
            Spacer(Modifier.width(6.dp))
            PlayerAvatar(name = player.name, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium, color = Ivory)
                val broke = if (player.timesBroke > 0) "  ·  ruiné ${player.timesBroke} fois" else ""
                Text(
                    text = "Solde : ${formatChips(player.balance)}$broke",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ivory.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("RECORD", style = MaterialTheme.typography.labelSmall, color = Gold)
                Text(
                    text = formatChips(player.bestBalance),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
                    color = Ivory
                )
            }
        }
    }
}

// ==================== Le détail ====================

@Composable
private fun EmptyDetail() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Choisissez un joueur pour voir ses statistiques",
            color = Ivory.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun LoadingDetail() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Gold)
    }
}

@Composable
private fun PlayerDetailPane(
    detail: PlayerDetail,
    rank: Int
) {
    val player = detail.player
    val stats = detail.stats
    val winRate = if (stats.handsPlayed > 0) stats.wins * 100 / stats.handsPlayed else 0
    val rankLabel = if (rank == 1) "1er du classement" else "${rank}e du classement"

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- En-tête : avatar, pseudo, rang, record ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(name = player.name, size = 64.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(player.name, style = MaterialTheme.typography.headlineSmall, color = Ivory)
                    Text(
                        text = rankLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (rank in 1..3) medalColor(rank) else Ivory.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Record : ${formatChips(player.bestBalance)} jetons",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold
                    )
                }
            }
        }

        // --- Statistiques, en grille de 2 colonnes ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Solde actuel", formatChips(player.balance), Modifier.weight(1f))
                    StatCard("Mains jouées", "${stats.handsPlayed}", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Victoires", "$winRate %", Modifier.weight(1f))
                    StatCard("Blackjacks", "${stats.blackjacks}", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val biggest = stats.biggestWin?.takeIf { it > 0 }?.let { "+${formatChips(it)}" } ?: "—"
                    StatCard("Plus gros gain", biggest, Modifier.weight(1f))
                    StatCard("Ruines", "${player.timesBroke}", Modifier.weight(1f))
                }
            }
        }

        // --- Dernières mains ---
        item {
            Text(
                "Dernières mains",
                style = MaterialTheme.typography.titleMedium,
                color = Gold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (detail.hands.isEmpty()) {
            item { Text("Aucune main jouée", color = Ivory.copy(alpha = 0.6f)) }
        }
        items(detail.hands, key = { it.id }) { hand ->
            HandHistoryRow(hand = hand)
        }
    }
}

// Une carte de statistique : grande valeur, petit intitulé
@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = CasinoSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
                color = Ivory
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = Ivory.copy(alpha = 0.6f))
        }
    }
}

// Format des dates de l'historique : "13/11 15:42"
private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE)

// Une main de l'historique : résultat et scores à gauche, gain et date à droite
@Composable
private fun HandHistoryRow(hand: HandEntity) {
    val label = runCatching { GameResult.valueOf(hand.result).short }.getOrDefault(hand.result)
    val (netText, netColor) = when {
        hand.net > 0 -> "+${formatChips(hand.net)}" to WinGreen
        hand.net < 0 -> "−${formatChips(-hand.net)}" to CasinoRed
        else -> "±0" to Ivory
    }

    Surface(
        color = CasinoSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = Ivory)
                Text(
                    text = "${hand.playerScore} contre ${hand.dealerScore}  ·  mise ${formatChips(hand.bet)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ivory.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(netText, color = netColor, fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                Text(
                    text = dateFormat.format(Date(hand.playedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ivory.copy(alpha = 0.5f)
                )
            }
        }
    }
}