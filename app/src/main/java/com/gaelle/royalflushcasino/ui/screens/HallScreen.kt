package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.ui.components.formatChips
import com.gaelle.royalflushcasino.ui.theme.Bronze
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoSurface
import com.gaelle.royalflushcasino.ui.theme.FeltGreenDark
import com.gaelle.royalflushcasino.ui.theme.FeltGreenLight
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import com.gaelle.royalflushcasino.ui.theme.PokerRed
import com.gaelle.royalflushcasino.ui.theme.PokerRedDark
import com.gaelle.royalflushcasino.ui.theme.Silver
import com.gaelle.royalflushcasino.viewmodel.HallViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import com.gaelle.royalflushcasino.ui.components.AppearingItem
import com.gaelle.royalflushcasino.ui.components.CasinoBackground

// Images des cartes, fournies par l'API Deck of Cards
private fun cardImage(code: String) = "https://deckofcardsapi.com/static/img/$code.png"

@Composable
fun HallScreen(
    onBlackjack: () -> Unit,
    onLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<HallViewModel>()
    val topPlayers by viewModel.topPlayers.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            // Du vert du tapis (en haut) vers le noir : rappel de la table de jeu
            .background(CasinoBackground)
    ) {
        // Mise en page "flux" du cours : la grille choisit le nombre de colonnes selon la largeur
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // L'en-tête prend toute la largeur, quel que soit le nombre de colonnes
            item(span = { GridItemSpan(maxLineSpan) }) {
                HallHeader()
            }

            item {
                AppearingItem(index = 0) {
                    GameCard(
                        title = "Blackjack",
                        subtitle = "Battez le croupier sans dépasser 21",
                        tag = "Paie 3 contre 2",
                        background = Brush.linearGradient(listOf(FeltGreenLight, FeltGreenDark)),
                        cardCodes = listOf("AS", "KH"),
                        onClick = onBlackjack
                    )
                }
            }

            item {
                AppearingItem(index = 1) {
                    GameCard(
                        title = "Vidéo Poker",
                        subtitle = "Visez la quinte flush royale",
                        tag = "Bientôt",
                        background = Brush.linearGradient(listOf(PokerRed, PokerRedDark)),
                        cardCodes = listOf("0H", "JH", "QH", "KH", "AH"),
                        onClick = null // pas encore disponible
                    )
                }
            }

            item {
                AppearingItem(index = 2) {
                    LeaderboardCard(topPlayers = topPlayers, onClick = onLeaderboard)
                }
            }
        }
    }
}

// ==================== En-tête ====================

@Composable
private fun HallHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "♠  ♥  ♣  ♦",
            style = MaterialTheme.typography.titleMedium,
            color = Gold.copy(alpha = 0.8f),
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Royal Flush",
            style = MaterialTheme.typography.displaySmall,
            color = Gold
        )
        Text(
            text = "CASINO",
            style = MaterialTheme.typography.labelLarge,
            color = Ivory.copy(alpha = 0.8f),
            letterSpacing = 8.sp
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(Modifier.width(80.dp), thickness = 1.dp, color = Gold.copy(alpha = 0.5f))
    }
}

// ==================== Cartes du hall ====================



// Base commune des cartes du hall : coins arrondis, ombre, liseré doré, effet d'appui
@Composable
private fun CasinoCard(
    background: Brush,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "press")
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .alpha(if (onClick != null) 1f else 0.55f) // estompée si pas encore disponible
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(10.dp, shape)
            .clip(shape)
            .background(background)
            .border(1.dp, Gold.copy(alpha = 0.35f), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

// Carte d'un jeu : texte à gauche, éventail de cartes à droite
@Composable
private fun GameCard(
    title: String,
    subtitle: String,
    tag: String,
    background: Brush,
    cardCodes: List<String>,
    onClick: (() -> Unit)?
) {
    CasinoCard(background = background, onClick = onClick) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .fillMaxWidth(0.55f)
        ) {
            Tag(text = tag, highlighted = onClick != null)
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Ivory)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ivory.copy(alpha = 0.75f))
        }
        CardFan(
            codes = cardCodes,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        )
    }
}

// Petite étiquette arrondie ("Paie 3 contre 2", "Bientôt"…)
@Composable
private fun Tag(text: String, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (highlighted) Gold else Ivory.copy(alpha = 0.85f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CasinoBlack,
            fontWeight = FontWeight.Bold
        )
    }
}

// Éventail de cartes : chaque carte est tournée et décalée autour du centre
@Composable
private fun CardFan(codes: List<String>, modifier: Modifier = Modifier) {
    val middle = (codes.size - 1) / 2f
    Box(
        modifier = modifier.size(width = 130.dp, height = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        codes.forEachIndexed { index, code ->
            val offset = index - middle // négatif à gauche, positif à droite
            AsyncImage(
                model = cardImage(code),
                contentDescription = null,
                placeholder = painterResource(R.drawable.card_back),
                modifier = Modifier
                    .width(58.dp)
                    .aspectRatio(226f / 314f)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1f) // tourne autour du bas de la carte
                        rotationZ = offset * 12f
                        translationX = offset * 16.dp.toPx()
                        translationY = abs(offset) * 4.dp.toPx()
                    }
                    .shadow(4.dp, RoundedCornerShape(4.dp))
            )
        }
    }
}

// Carte du classement : aperçu du podium en direct
@Composable
private fun LeaderboardCard(
    topPlayers: List<PlayerEntity>,
    onClick: () -> Unit
) {
    CasinoCard(
        background = Brush.linearGradient(listOf(CasinoSurface, CasinoBlack)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Tag(text = "Records", highlighted = true)
                Spacer(Modifier.height(10.dp))
                Text("Classement", style = MaterialTheme.typography.headlineSmall, color = Ivory)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Les meilleurs joueurs du casino",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ivory.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.width(140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (topPlayers.isEmpty()) {
                    Text("Soyez le premier !", style = MaterialTheme.typography.bodyMedium, color = Gold)
                }
                topPlayers.forEachIndexed { index, player ->
                    PodiumLine(rank = index + 1, player = player)
                }
            }
        }
    }
}

// Une ligne du podium : pastille de couleur, pseudo, record
@Composable
private fun PodiumLine(rank: Int, player: PlayerEntity) {
    val medal: Color = when (rank) {
        1 -> Gold
        2 -> Silver
        else -> Bronze
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(medal),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", color = CasinoBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Ivory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = formatChips(player.bestBalance),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = Montserrat),
            color = Gold
        )
    }
}