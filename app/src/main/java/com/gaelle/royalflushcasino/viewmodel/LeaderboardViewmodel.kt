package com.gaelle.royalflushcasino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaelle.royalflushcasino.data.CasinoRepository
import com.gaelle.royalflushcasino.data.local.HandEntity
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import com.gaelle.royalflushcasino.data.local.PlayerStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Tout ce qu'affiche le détail d'un joueur
data class PlayerDetail(
    val player: PlayerEntity,
    val stats: PlayerStats,
    val hands: List<HandEntity>
)

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CasinoRepository(application)

    val players = MutableStateFlow<List<PlayerEntity>>(emptyList()) // triés par record
    val detail = MutableStateFlow<PlayerDetail?>(null)              // joueur sélectionné

    private var detailJob: Job? = null

    init {
        // Le classement se met à jour tout seul après chaque main jouée
        viewModelScope.launch {
            repository.leaderboard().collect { players.value = it }
        }
    }

    // Sélection d'un joueur : on combine trois Flow de la base en un seul objet
    fun select(playerId: Long) {
        detailJob?.cancel() // on arrête d'écouter le joueur précédent
        detail.value = null
        detailJob = viewModelScope.launch {
            combine(
                repository.player(playerId),
                repository.stats(playerId),
                repository.recentHands(playerId)
            ) { player, stats, hands ->
                player?.let { PlayerDetail(it, stats, hands) } // null si le joueur a été supprimé
            }.collect { detail.value = it }
        }
    }
}