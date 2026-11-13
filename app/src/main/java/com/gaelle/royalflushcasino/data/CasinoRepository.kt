package com.gaelle.royalflushcasino.data

import android.content.Context
import com.gaelle.royalflushcasino.data.local.AppDatabase
import com.gaelle.royalflushcasino.data.local.HandEntity
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.data.local.PlayerStats
import com.gaelle.royalflushcasino.game.STARTING_BALANCE
import kotlinx.coroutines.flow.Flow
import java.text.Normalizer

class CasinoRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val playerDao = database.playerDao()
    private val handDao = database.handDao()

    // --- Joueurs ---
    fun players(): Flow<List<PlayerEntity>> = playerDao.getAllByName()
    fun leaderboard(): Flow<List<PlayerEntity>> = playerDao.getLeaderboard()
    fun player(id: Long): Flow<PlayerEntity?> = playerDao.getPlayer(id)

    // Crée un joueur ; renvoie son identifiant, ou null si le pseudo est déjà pris
    // (sans tenir compte des majuscules ni des accents : "Gaëlle" = "gaelle")
    suspend fun createPlayer(name: String): Long? {
        val key = normalizeName(name)
        if (playerDao.getAllNames().any { normalizeName(it) == key }) return null
        return playerDao.insertPlayer(PlayerEntity(name = name))
    }

    // "Gaëlle" → "gaelle" : on retire les accents et on passe en minuscules
    private fun normalizeName(name: String): String =
        Normalizer.normalize(name.trim(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()

    suspend fun updateBalance(playerId: Long, balance: Int) = playerDao.updateBalance(playerId, balance)
    suspend fun markBroke(playerId: Long) = playerDao.markBroke(playerId)
    suspend fun restartPlayer(playerId: Long) = playerDao.restartPlayer(playerId, STARTING_BALANCE)
    suspend fun deletePlayer(playerId: Long) = playerDao.deletePlayer(playerId)

    // --- Mains jouées ---
    suspend fun recordHand(hand: HandEntity) = handDao.insertHand(hand)
    fun recentHands(playerId: Long): Flow<List<HandEntity>> = handDao.getRecentHands(playerId)
    fun stats(playerId: Long): Flow<PlayerStats> = handDao.getStats(playerId)
}