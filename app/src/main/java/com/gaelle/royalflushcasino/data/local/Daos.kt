package com.gaelle.royalflushcasino.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    // Tous les joueurs, par ordre alphabétique (écran "Qui joue ?")
    @Query("SELECT * FROM players ORDER BY name COLLATE NOCASE")
    fun getAllByName(): Flow<List<PlayerEntity>>

    // Le classement : du meilleur record au moins bon
    @Query("SELECT * FROM players ORDER BY bestBalance DESC, name COLLATE NOCASE")
    fun getLeaderboard(): Flow<List<PlayerEntity>>

    // Un joueur précis (null s'il n'existe pas)
    @Query("SELECT * FROM players WHERE id = :id")
    fun getPlayer(id: Long): Flow<PlayerEntity?>

    // Combien de joueurs ont déjà ce pseudo (sans tenir compte des majuscules)
    @Query("SELECT COUNT(*) FROM players WHERE name = :name COLLATE NOCASE")
    suspend fun countByName(name: String): Int

    // Tous les pseudos (pour vérifier les doublons sans tenir compte des accents)
    @Query("SELECT name FROM players")
    suspend fun getAllNames(): List<String>

    // Ajoute un joueur et renvoie son identifiant
    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    // Nouveau solde ; le record est mis à jour s'il est battu
    @Query("UPDATE players SET balance = :balance, bestBalance = MAX(bestBalance, :balance) WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Int)

    // Une ruine de plus
    @Query("UPDATE players SET timesBroke = timesBroke + 1 WHERE id = :id")
    suspend fun markBroke(id: Long)

    // Le joueur recommence : solde remis au départ (le record est conservé)
    @Query("UPDATE players SET balance = :startBalance WHERE id = :id")
    suspend fun restartPlayer(id: Long, startBalance: Int)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deletePlayer(id: Long)
}

// Statistiques d'un joueur, calculées par la base
data class PlayerStats(
    val handsPlayed: Int,
    val wins: Int,
    val blackjacks: Int,
    val biggestWin: Int?
)

@Dao
interface HandDao {
    @Insert
    suspend fun insertHand(hand: HandEntity)

    // Les dernières mains d'un joueur, de la plus récente à la plus ancienne
    @Query("SELECT * FROM hands WHERE playerId = :playerId ORDER BY playedAt DESC LIMIT 20")
    fun getRecentHands(playerId: Long): Flow<List<HandEntity>>

    // Statistiques calculées directement en SQL
    @Query(
        """
        SELECT COUNT(*) AS handsPlayed,
               COALESCE(SUM(CASE WHEN net > 0 THEN 1 ELSE 0 END), 0) AS wins,
               COALESCE(SUM(CASE WHEN result = 'BLACKJACK' THEN 1 ELSE 0 END), 0) AS blackjacks,
               MAX(net) AS biggestWin
        FROM hands WHERE playerId = :playerId
        """
    )
    fun getStats(playerId: Long): Flow<PlayerStats>
}