package com.gaelle.royalflushcasino.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Hall : NavKey

// Choix du joueur avant d'aller à la table
@Serializable
data object Players : NavKey

// La table reçoit l'identifiant du joueur : c'est un paramètre de navigation
@Serializable
data class BlackjackTable(val playerId: Long) : NavKey

// Classement des joueurs (liste/détails)
@Serializable
data object Leaderboard : NavKey