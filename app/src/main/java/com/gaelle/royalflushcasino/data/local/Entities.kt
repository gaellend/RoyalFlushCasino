package com.gaelle.royalflushcasino.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gaelle.royalflushcasino.game.STARTING_BALANCE

// Un joueur du casino
@Entity(
    tableName = "players",
    indices = [Index(value = ["name"], unique = true)] // deux joueurs ne peuvent pas avoir le même pseudo
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Room choisit l'identifiant tout seul
    val name: String,
    val balance: Int = STARTING_BALANCE,      // solde actuel
    val bestBalance: Int = STARTING_BALANCE,  // record : le plus haut solde jamais atteint
    val timesBroke: Int = 0,                  // nombre de fois où le joueur a été ruiné
    val createdAt: Long = System.currentTimeMillis()
)

// Une main jouée par un joueur (après un split, chaque main a sa propre ligne)
@Entity(
    tableName = "hands",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE // si on supprime un joueur, ses mains disparaissent aussi
        )
    ],
    indices = [Index(value = ["playerId"])]
)
data class HandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,                       // à quel joueur appartient cette main
    val playedAt: Long = System.currentTimeMillis(),
    val bet: Int,                             // mise de la main
    val net: Int,                             // gain (positif) ou perte (négatif)
    val result: String,                       // nom du résultat : "WIN", "BLACKJACK", "LOSE"…
    val playerScore: Int,
    val dealerScore: Int
)