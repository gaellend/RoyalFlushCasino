package com.gaelle.royalflushcasino.ui.components

import androidx.compose.ui.text.font.FontWeight
import com.gaelle.royalflushcasino.ui.theme.Montserrat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaelle.royalflushcasino.R
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.CasinoRed
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.Ivory
import java.util.Locale

// Formate un nombre de jetons à la française : 1000 → "1 000"
fun formatChips(amount: Int): String = String.format(Locale.FRANCE, "%,d", amount)

// En-tête de la table : flèche retour + solde au centre (qui défile quand il change)
@Composable
fun TableHeader(
    balance: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shownBalance by animateIntAsState(
        targetValue = balance,
        animationSpec = tween(700),
        label = "balance"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Retour",
                tint = Ivory
            )
        }
        Spacer(Modifier.weight(1f))
        ChipFace(color = CasinoRed, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatChips(shownBalance),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold
            ),
            color = Ivory
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "jetons",
            style = MaterialTheme.typography.labelMedium,
            color = Ivory.copy(alpha = 0.7f)
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(48.dp)) // équilibre la flèche pour bien centrer le solde
    }
}

// En-tête simple : flèche retour + titre (pour les écrans hors table)
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Retour",
                tint = Gold
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Gold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(48.dp))
    }
}

// Bouton d'action rond avec icône et libellé (style Hit / Stand)
@Composable
fun ActionButton(
    iconRes: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: Color = Color.White
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "press")

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(color)
                .clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = Ivory)
    }
}

// Pastille blanche avec le score (invisible mais présente si "visible" est faux, pour éviter les sauts)
@Composable
fun ScorePill(
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .alpha(if (visible) 1f else 0f)
            .shadow(3.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Ivory)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = CasinoBlack)
    }
}

// Inscriptions imprimées sur le tapis, comme sur une vraie table
@Composable
fun FeltInscription(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(Modifier.width(220.dp), thickness = 1.dp, color = Gold.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "BLACKJACK PAIE 3 CONTRE 2",
            style = MaterialTheme.typography.labelLarge,
            color = Gold.copy(alpha = 0.85f),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Le croupier reste à 17",
            style = MaterialTheme.typography.labelMedium,
            color = Ivory.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(Modifier.width(220.dp), thickness = 1.dp, color = Gold.copy(alpha = 0.5f))
    }
}

// Bannière de fin : le gain en grand, la phrase en petit, avec un effet de zoom
@Composable
fun ResultBanner(
    message: String,
    net: Int,
    modifier: Modifier = Modifier
) {
    val visibleState = remember { MutableTransitionState(false) }.apply { targetState = true }

    val won = net > 0
    val lost = net < 0
    val background = when {
        won -> Gold
        lost -> CasinoBlack
        else -> Ivory
    }
    val textColor = if (lost) Ivory else CasinoBlack
    val shape = RoundedCornerShape(16.dp)

    AnimatedVisibility(
        visibleState = visibleState,
        enter = scaleIn(initialScale = 0.6f) + fadeIn(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .shadow(8.dp, shape)
                .clip(shape)
                .background(background)
                .border(1.dp, if (lost) CasinoRed else Color.Transparent, shape)
                .padding(horizontal = 28.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    won -> "+${formatChips(net)} jetons"
                    lost -> "−${formatChips(-net)} jetons"
                    else -> "Mise remboursée"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}