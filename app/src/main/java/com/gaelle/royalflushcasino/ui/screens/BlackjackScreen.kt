package com.gaelle.royalflushcasino.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.data.Card
import com.gaelle.royalflushcasino.game.CHIP_VALUES
import com.gaelle.royalflushcasino.game.GamePhase
import com.gaelle.royalflushcasino.game.GameResult
import com.gaelle.royalflushcasino.game.PlayerHand
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
import com.gaelle.royalflushcasino.ui.components.ChipFace
import com.gaelle.royalflushcasino.ui.components.Confetti
import com.gaelle.royalflushcasino.ui.components.FeltInscription
import com.gaelle.royalflushcasino.ui.components.HandRow
import com.gaelle.royalflushcasino.ui.components.HorizontalBetStack
import com.gaelle.royalflushcasino.ui.components.ResultBanner
import com.gaelle.royalflushcasino.ui.components.ScorePill
import com.gaelle.royalflushcasino.ui.components.TableHeader
import com.gaelle.royalflushcasino.ui.components.formatChips
import com.gaelle.royalflushcasino.ui.theme.ActionBlue
import com.gaelle.royalflushcasino.ui.theme.ActionDark
import com.gaelle.royalflushcasino.ui.theme.ActionGreen
import com.gaelle.royalflushcasino.ui.theme.ActionGrey
import com.gaelle.royalflushcasino.ui.theme.ActionOrange
import com.gaelle.royalflushcasino.ui.theme.ActionRed
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.FeltGreenDark
import com.gaelle.royalflushcasino.ui.theme.FeltGreenLight
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import com.gaelle.royalflushcasino.viewmodel.BlackjackViewModel
import kotlinx.coroutines.delay

// Durée d'affichage du message de ruine avant le retour au hall
private const val RUIN_DELAY_MS = 3500L

// Toutes les valeurs dont la table a besoin pour s'afficher, regroupées en un seul objet
private data class TableUi(
    val hands: List<PlayerHand>,
    val activeHand: Int,
    val dealerCards: List<Card>,
    val phase: GamePhase,
    val isBusy: Boolean,
    val error: String?,
    val balance: Int,
    val bet: Int,
    val lastNet: Int?,
    val remaining: Int?,
    val isShuffling: Boolean,
    val ruined: Boolean
) {
    val hideDealerCard: Boolean get() = phase == GamePhase.DEALING || phase == GamePhase.PLAYER_TURN
    val bettingTime: Boolean
        get() = !isBusy && !ruined && (phase == GamePhase.WAITING || phase == GamePhase.FINISHED)
    val playerTurn: Boolean get() = phase == GamePhase.PLAYER_TURN
    val currentHand: PlayerHand? get() = hands.getOrNull(activeHand)

    // Mise affichée : celle qu'on compose entre deux mains, sinon tout ce qui est sur la table
    val shownBet: Int get() = if (bettingTime || hands.isEmpty()) bet else hands.sumOf { it.bet }

    val blackjackWin: Boolean get() = phase == GamePhase.FINISHED && hands.any { it.result == GameResult.BLACKJACK }
}

@Composable
fun BlackjackScreen(
    playerId: Long,
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit,
    onRuined: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Un ViewModel par joueur (clé), créé avec son identifiant (factory)
    val viewModel: BlackjackViewModel = viewModel(
        key = "blackjack-$playerId",
        factory = BlackjackViewModel.factory(playerId)
    )
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
    val ruined by viewModel.ruined.collectAsStateWithLifecycle()

    val ui = TableUi(
        hands, activeHand, dealerCards, phase, isBusy, error,
        balance, bet, lastNet, remaining, isShuffling, ruined
    )

    // Ruiné : on laisse le temps de lire le message, puis retour au hall
    LaunchedEffect(ruined) {
        if (ruined) {
            delay(RUIN_DELAY_MS)
            onRuined()
        }
    }

    // Position du sabot à l'écran : point de départ des cartes distribuées
    var shoePosition by remember { mutableStateOf<Offset?>(null) }

    // Comme dans le cours : on choisit la mise en page selon les classes de taille d'écran
    val landscape = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
    val cardWidth = when {
        landscape -> 80.dp                                                                // ← cartes en paysage
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED -> 100.dp    // tablette
        else -> 80.dp                                                                     // téléphone en portrait
    }

    // Marges de sécurité (barres système, encoche de l'appareil photo), en pixels
    val insets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insetLeft = insets.getLeft(density, layoutDirection).toFloat()
    val insetRight = insets.getRight(density, layoutDirection).toFloat()
    val insetTop = insets.getTop(density).toFloat()
    val insetBottom = insets.getBottom(density).toFloat()

    // Décalage vertical de la lumière : remonte le rond en paysage
    val lightShiftPx = with(density) { (if (landscape) 24.dp else 0.dp).toPx() } // ← plus grand = plus haut

    Box(
        modifier = modifier
            .fillMaxSize()
            // Tapis : la lumière est centrée sur la table (le contenu), pas sur l'écran
            .drawBehind {
                val center = Offset(
                    x = (size.width + insetLeft - insetRight) / 2f,
                    y = (size.height + insetTop - insetBottom) / 2f - lightShiftPx
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(FeltGreenLight, FeltGreenDark),
                        center = center,
                        radius = minOf(size.width, size.height) / 2f // ← taille du rond de lumière
                    )
                )
            }
    ) {
        // Le contenu évite les barres système et l'encoche de l'appareil photo
        val content = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)

        if (landscape) {
            LandscapeTable(
                ui = ui,
                viewModel = viewModel,
                cardWidth = cardWidth,
                shoePosition = shoePosition,
                onShoePositioned = { shoePosition = it },
                onBack = onBack,
                modifier = content
            )
        } else {
            PortraitTable(
                ui = ui,
                viewModel = viewModel,
                cardWidth = cardWidth,
                shoePosition = shoePosition,
                onShoePositioned = { shoePosition = it },
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 600.dp)
                    .then(content)
            )
        }

        if (ui.blackjackWin) {
            Confetti(modifier = Modifier.fillMaxSize())
        }
    }
}

// ==================== Portrait ====================

@Composable
private fun PortraitTable(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    cardWidth: Dp,
    shoePosition: Offset?,
    onShoePositioned: (Offset) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TableHeader(balance = ui.balance, onBack = onBack)

        Spacer(Modifier.weight(0.6f))

        // Croupier au centre ; à droite, le sabot avec la pile de mise en dessous
        Box(modifier = Modifier.fillMaxWidth()) {
            DealerHand(
                ui = ui,
                cardWidth = cardWidth,
                scoreGap = 12.dp,
                shoePosition = shoePosition,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 40.dp) // ← descend la main du croupier : plus grand = plus bas
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-70).dp), // ← hauteur du sabot (la pile suit)
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CardShoe(
                    remaining = ui.remaining,
                    isShuffling = ui.isShuffling,
                    onPositioned = onShoePositioned
                )
                Spacer(Modifier.height(28.dp)) // ← écart entre le sabot et la pile de mise
                BetStack(
                    amount = ui.shownBet,
                    onClick = { viewModel.removeTopChip() },
                    enabled = ui.bettingTime,
                    chipSize = 32.dp, // ← taille des jetons de la pile en portrait
                    step = 4.dp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Centre : message ou bannière de résultat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            TableMessage(
                ui = ui,
                onRetry = { viewModel.retryDealer() }
            )
        }

        Spacer(Modifier.height(20.dp))

        // Main(s) du joueur au centre, Séparer à droite
        Box(modifier = Modifier.fillMaxWidth()) {
            PlayerHands(
                ui = ui,
                cardWidth = cardWidth,
                scoreGap = 12.dp,
                shoePosition = shoePosition,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 5.dp)
            )
            SplitButton(
                ui = ui,
                onSplit = { viewModel.split() },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Spacer(Modifier.weight(1f))

        ChipsRow(ui = ui, viewModel = viewModel)

        Spacer(Modifier.height(28.dp))

        ActionButtons(ui = ui, viewModel = viewModel)
    }
}

// ==================== Paysage ====================
// Disposition symétrique :
//   haut     : retour + solde   |  croupier  |  sabot
//   milieu   : jetons           |  message   |
//   bas      : pile de mise     |  joueur    |  boutons
@Composable
private fun LandscapeTable(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    cardWidth: Dp,
    shoePosition: Offset?,
    onShoePositioned: (Offset) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        // --- En haut à gauche : retour + solde ---
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-12).dp, y = (-8).dp), // ← position de la flèche
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Retour",
                    tint = Ivory
                )
            }
            CompactBalance(balance = ui.balance)
        }

        // --- En haut à droite : le sabot ---
        CardShoe(
            remaining = ui.remaining,
            isShuffling = ui.isShuffling,
            onPositioned = onShoePositioned,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // --- À gauche : jetons à mi-chemin entre le solde (en haut) et la pile de mise (en bas) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
        ) {
            Spacer(Modifier.height(40.dp)) // place réservée à la ligne flèche + solde
            Spacer(Modifier.weight(1f))
            VerticalChips(ui = ui, viewModel = viewModel)
            Spacer(Modifier.weight(1f))
            HorizontalBetStack(
                amount = ui.shownBet,
                onClick = { viewModel.removeTopChip() },
                enabled = ui.bettingTime
            )
            Spacer(Modifier.height(8.dp)) // ← remonte la pile de mise
        }

        // --- Au centre : croupier, message, joueur ---
        // Deux espaces de même poids autour du message : il est toujours à égale distance des deux mains
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DealerHand(
                ui = ui,
                cardWidth = cardWidth,
                scoreGap = 10.dp, // ← écart entre la pastille et les cartes
                shoePosition = shoePosition,
                pillOnSide = true
            )
            Spacer(Modifier.weight(1f))
            TableMessage(
                ui = ui,
                onRetry = { viewModel.retryDealer() },
                compact = true
            )
            Spacer(Modifier.weight(1f))
            PlayerHands(
                ui = ui,
                cardWidth = cardWidth,
                scoreGap = 10.dp, // ← écart entre la pastille et les cartes
                shoePosition = shoePosition,
                pillOnSide = true
            )
        }

        // --- En bas à droite : les boutons ---
        LandscapeActions(
            ui = ui,
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

// Paysage : hauteur de la rangée des gros boutons
private val LANDSCAPE_BIG_ROW = 80.dp

// Boutons du paysage, calés à droite :
//            (Séparer)
//     (Doubler)    (Abandonner)
//     (Tirer)      (Rester)
@Composable
private fun LandscapeActions(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    modifier: Modifier = Modifier
) {
    val currentHand = ui.currentHand
    val slot = 66.dp      // ← largeur de chaque colonne de boutons
    val gap = 8.dp        // ← écart entre les deux colonnes
    val big = 56.dp       // ← taille des gros boutons
    val small = 44.dp     // ← taille des petits boutons

    Column(modifier = modifier, horizontalAlignment = Alignment.End) {

        // Séparer : centré au-dessus des deux colonnes, place toujours réservée
        Box(
            modifier = Modifier
                .width(slot * 2 + gap)
                .height(66.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SplitButton(
                ui = ui,
                onSplit = { viewModel.split() },
                shortLabel = true,
                size = small
            )
        }

        // Petits boutons : Doubler au-dessus de Tirer, Abandonner au-dessus de Rester
        Row(
            modifier = Modifier.height(66.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.width(slot), contentAlignment = Alignment.BottomCenter) {
                if (ui.playerTurn && currentHand != null && doubleAllowed(currentHand)) {
                    ActionButton(
                        iconText = "×2",
                        label = "Doubler",
                        color = ActionOrange,
                        onClick = { viewModel.doubleDown() },
                        enabled = !ui.isBusy && canDouble(currentHand, ui.balance),
                        size = small
                    )
                }
            }
            Box(modifier = Modifier.width(slot), contentAlignment = Alignment.BottomCenter) {
                if (ui.playerTurn && canSurrender(ui.hands)) {
                    ActionButton(
                        iconRes = R.drawable.ic_flag,
                        label = "Abandonner",
                        color = ActionDark,
                        onClick = { viewModel.surrender() },
                        enabled = !ui.isBusy,
                        size = small
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Gros boutons : Tirer / Rester pendant la main, Effacer / Distribuer entre deux mains
        Row(
            modifier = Modifier.height(LANDSCAPE_BIG_ROW),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.width(slot), contentAlignment = Alignment.TopCenter) {
                if (ui.playerTurn) {
                    ActionButton(
                        iconRes = R.drawable.ic_cards,
                        label = "Tirer",
                        color = ActionGreen,
                        onClick = { viewModel.hit() },
                        enabled = !ui.isBusy,
                        size = big
                    )
                } else {
                    ActionButton(
                        iconRes = R.drawable.ic_close,
                        label = "Effacer",
                        color = ActionGrey,
                        onClick = { viewModel.clearBet() },
                        enabled = ui.bettingTime && ui.bet > 0,
                        size = big
                    )
                }
            }
            Box(modifier = Modifier.width(slot), contentAlignment = Alignment.TopCenter) {
                if (ui.playerTurn) {
                    ActionButton(
                        iconRes = R.drawable.ic_hand,
                        label = "Rester",
                        color = ActionRed,
                        onClick = { viewModel.stand() },
                        enabled = !ui.isBusy,
                        size = big
                    )
                } else {
                    ActionButton(
                        iconRes = R.drawable.ic_deal,
                        label = if (ui.phase == GamePhase.FINISHED) "Rejouer" else "Distribuer",
                        color = Gold,
                        iconTint = CasinoBlack,
                        onClick = { viewModel.deal() },
                        enabled = ui.bettingTime && ui.bet > 0,
                        size = big
                    )
                }
            }
        }
    }
}

// Jetons empilés verticalement (paysage) : place réservée, invisibles pendant la main
@Composable
private fun VerticalChips(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.alpha(if (ui.bettingTime) 1f else 0f),
        verticalArrangement = Arrangement.spacedBy(8.dp), // ← écart entre les jetons
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CHIP_VALUES.forEach { value ->
            CasinoChip(
                value = value,
                enabled = ui.bettingTime && ui.bet + value <= ui.balance,
                onClick = { viewModel.addChip(value) },
                size = 44.dp // ← taille des jetons en paysage
            )
        }
    }
}

// Solde compact (paysage) : petit jeton + montant qui défile quand il change
@Composable
private fun CompactBalance(balance: Int) {
    val shownBalance by animateIntAsState(
        targetValue = balance,
        animationSpec = tween(700),
        label = "balance"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        ChipFace(color = CasinoRed, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatChips(shownBalance),
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold
            ),
            color = Ivory
        )
    }
}

// ==================== Morceaux communs ====================

// Largeur fixe des pastilles quand elles sont à côté des mains (paysage)
private val SIDE_PILL_WIDTH = 52.dp

// Main du croupier et son score : au-dessus en portrait, à gauche en paysage
@Composable
private fun DealerHand(
    ui: TableUi,
    cardWidth: Dp,
    scoreGap: Dp,
    shoePosition: Offset?,
    modifier: Modifier = Modifier,
    pillOnSide: Boolean = false
) {
    val scoreText = if (ui.hideDealerCard) "${handValue(ui.dealerCards.take(1))}" else "${handValue(ui.dealerCards)}"
    val visible = ui.dealerCards.isNotEmpty()

    if (pillOnSide) {
        Row(
            modifier = modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScorePill(text = scoreText, visible = visible, modifier = Modifier.width(SIDE_PILL_WIDTH))
            Spacer(Modifier.width(scoreGap))
            HandRow(
                cards = ui.dealerCards,
                hideSecondCard = ui.hideDealerCard,
                shoePosition = shoePosition,
                cardWidth = cardWidth
            )
            // Espace invisible de la largeur de la pastille : la main reste centrée
            Spacer(Modifier.width(SIDE_PILL_WIDTH + scoreGap))
        }
    } else {
        Column(
            modifier = modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HandRow(
                cards = ui.dealerCards,
                hideSecondCard = ui.hideDealerCard,
                shoePosition = shoePosition,
                cardWidth = cardWidth
            )
            Spacer(Modifier.height(scoreGap))
            ScorePill(text = scoreText, visible = visible)
        }
    }
}

// Message au centre de la table : erreur, remélange, bannière de résultat (ou de ruine) ou inscriptions
@Composable
private fun TableMessage(
    ui: TableUi,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val net = ui.lastNet
        when {
            ui.error != null -> Column(
                modifier = Modifier.widthIn(max = 230.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(ui.error, color = Ivory, textAlign = TextAlign.Center)
                if (ui.phase == GamePhase.DEALER_TURN && !ui.isBusy) {
                    Button(onClick = onRetry) { Text("Réessayer") }
                }
            }
            ui.isShuffling -> Text(
                "Remélange du sabot…",
                style = MaterialTheme.typography.titleMedium,
                color = Gold
            )
            ui.phase == GamePhase.FINISHED && net != null -> {
                val message = when {
                    ui.ruined -> "Ruiné ! Retour au hall…"
                    ui.hands.size == 1 -> ui.hands[0].result?.message ?: ""
                    else -> ui.hands.mapIndexed { i, h -> "Main ${i + 1} : ${h.result?.short ?: ""}" }
                        .joinToString("  ·  ")
                }
                ResultBanner(
                    message = message,
                    net = net,
                    compact = compact,
                    modifier = Modifier.widthIn(max = if (compact) 340.dp else 270.dp)
                )
            }
            else -> FeltInscription()
        }
    }
}

// Main(s) du joueur, avec leur score : au-dessus en portrait, à gauche en paysage
@Composable
private fun PlayerHands(
    ui: TableUi,
    cardWidth: Dp,
    scoreGap: Dp,
    shoePosition: Offset?,
    modifier: Modifier = Modifier,
    pillOnSide: Boolean = false
) {
    val handsGap = 16.dp // écart entre deux mains après un split

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (pillOnSide) handsGap else 12.dp),
        verticalAlignment = if (pillOnSide) Alignment.CenterVertically else Alignment.Top
    ) {
        if (ui.hands.isEmpty()) {
            if (pillOnSide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScorePill(text = "", visible = false, modifier = Modifier.width(SIDE_PILL_WIDTH))
                    Spacer(Modifier.width(scoreGap))
                    HandRow(cards = emptyList(), shoePosition = shoePosition, cardWidth = cardWidth)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ScorePill(text = "", visible = false)
                    Spacer(Modifier.height(scoreGap))
                    HandRow(cards = emptyList(), shoePosition = shoePosition, cardWidth = cardWidth)
                }
            }
        }

        ui.hands.forEachIndexed { index, hand ->
            // Après un split, la main qui n'est pas en cours est estompée
            val dimmed = ui.hands.size > 1 && ui.playerTurn && index != ui.activeHand
            val cards = @Composable {
                HandRow(
                    cards = hand.cards,
                    shoePosition = shoePosition,
                    cardWidth = cardWidth,
                    overlap = if (ui.hands.size > 1) cardWidth * -0.69f else cardWidth * -0.5f,
                    lastCardSideways = hand.doubled
                )
            }
            val splitResult = @Composable {
                if (ui.hands.size > 1) {
                    Text(
                        text = hand.result?.short ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gold
                    )
                }
            }

            if (pillOnSide) {
                Row(
                    modifier = Modifier.alpha(if (dimmed) 0.5f else 1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScorePill(
                        text = "${hand.value}",
                        visible = hand.cards.isNotEmpty(),
                        modifier = Modifier.width(SIDE_PILL_WIDTH)
                    )
                    Spacer(Modifier.width(scoreGap))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        cards()
                        splitResult()
                    }
                }
            } else {
                Column(
                    modifier = Modifier.alpha(if (dimmed) 0.5f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScorePill(text = "${hand.value}", visible = hand.cards.isNotEmpty())
                    Spacer(Modifier.height(scoreGap))
                    cards()
                    splitResult()
                }
            }
        }

        // Paysage : espace invisible de la largeur d'une pastille, pour garder la ou les mains centrées
        if (pillOnSide) {
            Spacer(Modifier.width((SIDE_PILL_WIDTH + scoreGap - handsGap).coerceAtLeast(0.dp)))
        }
    }
}

// Bouton Séparer : n'apparaît que si le joueur a une paire
@Composable
private fun SplitButton(
    ui: TableUi,
    onSplit: () -> Unit,
    modifier: Modifier = Modifier,
    shortLabel: Boolean = false,
    size: Dp = 48.dp
) {
    if (ui.playerTurn && hasSplittablePair(ui.hands)) {
        val affordable = canSplit(ui.hands, ui.balance)
        ActionButton(
            iconRes = R.drawable.ic_split,
            label = if (affordable || shortLabel) "Séparer" else "Pas assez\nde jetons",
            color = ActionBlue,
            onClick = onSplit,
            enabled = !ui.isBusy && affordable,
            size = size,
            modifier = modifier
        )
    }
}

// Rangée de jetons pour miser (portrait) : place toujours réservée, invisibles pendant la main
@Composable
private fun ChipsRow(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 56.dp
) {
    Box(
        modifier = modifier.height(rowHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier.alpha(if (ui.bettingTime) 1f else 0f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CHIP_VALUES.forEach { value ->
                CasinoChip(
                    value = value,
                    enabled = ui.bettingTime && ui.bet + value <= ui.balance,
                    onClick = { viewModel.addChip(value) }
                )
            }
        }
    }
}

// Boutons (portrait) : Doubler | principal | principal | Abandonner, dans 4 emplacements de même largeur
@Composable
private fun ActionButtons(
    ui: TableUi,
    viewModel: BlackjackViewModel,
    modifier: Modifier = Modifier,
    slotWidth: Dp = 76.dp,     // largeur de chaque emplacement
    bigSize: Dp = 64.dp,       // Tirer / Rester / Effacer / Distribuer
    smallSize: Dp = 48.dp,     // Doubler / Abandonner
    rowHeight: Dp = 100.dp,    // hauteur réservée à la rangée
    shortLabels: Boolean = false // vrai : pas de "Pas assez de jetons" sur 2 lignes
) {
    val currentHand = ui.currentHand
    val smallTop = (bigSize - smallSize) / 2 // aligne le centre des petits boutons sur celui des gros

    Row(
        modifier = modifier.height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Doubler
        Box(modifier = Modifier.width(slotWidth), contentAlignment = Alignment.TopCenter) {
            if (ui.playerTurn && currentHand != null && doubleAllowed(currentHand)) {
                val affordable = canDouble(currentHand, ui.balance)
                ActionButton(
                    iconText = "×2",
                    label = if (affordable || shortLabels) "Doubler" else "Pas assez\nde jetons",
                    color = ActionOrange,
                    onClick = { viewModel.doubleDown() },
                    enabled = !ui.isBusy && affordable,
                    size = smallSize,
                    modifier = Modifier.padding(top = smallTop)
                )
            }
        }

        // Tirer / Effacer
        Box(modifier = Modifier.width(slotWidth), contentAlignment = Alignment.TopCenter) {
            if (ui.playerTurn) {
                ActionButton(
                    iconRes = R.drawable.ic_cards,
                    label = "Tirer",
                    color = ActionGreen,
                    onClick = { viewModel.hit() },
                    enabled = !ui.isBusy,
                    size = bigSize
                )
            } else {
                ActionButton(
                    iconRes = R.drawable.ic_close,
                    label = "Effacer",
                    color = ActionGrey,
                    onClick = { viewModel.clearBet() },
                    enabled = ui.bettingTime && ui.bet > 0,
                    size = bigSize
                )
            }
        }

        // Rester / Distribuer
        Box(modifier = Modifier.width(slotWidth), contentAlignment = Alignment.TopCenter) {
            if (ui.playerTurn) {
                ActionButton(
                    iconRes = R.drawable.ic_hand,
                    label = "Rester",
                    color = ActionRed,
                    onClick = { viewModel.stand() },
                    enabled = !ui.isBusy,
                    size = bigSize
                )
            } else {
                ActionButton(
                    iconRes = R.drawable.ic_deal,
                    label = if (ui.phase == GamePhase.FINISHED) "Rejouer" else "Distribuer",
                    color = Gold,
                    iconTint = CasinoBlack,
                    onClick = { viewModel.deal() },
                    enabled = ui.bettingTime && ui.bet > 0,
                    size = bigSize
                )
            }
        }

        // Abandonner
        Box(modifier = Modifier.width(slotWidth), contentAlignment = Alignment.TopCenter) {
            if (ui.playerTurn && canSurrender(ui.hands)) {
                ActionButton(
                    iconRes = R.drawable.ic_flag,
                    label = "Abandonner",
                    color = ActionDark,
                    onClick = { viewModel.surrender() },
                    enabled = !ui.isBusy,
                    size = smallSize,
                    modifier = Modifier.padding(top = smallTop)
                )
            }
        }
    }
}