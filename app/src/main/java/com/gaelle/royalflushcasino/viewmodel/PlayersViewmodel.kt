package com.gaelle.royalflushcasino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaelle.royalflushcasino.data.CasinoRepository
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

const val MAX_NAME_LENGTH = 15

class PlayersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CasinoRepository(application)

    val players = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val error = MutableStateFlow<String?>(null)

    init {
        // La liste se met à jour toute seule à chaque changement dans la base
        viewModelScope.launch {
            repository.players().collect { players.value = it }
        }
    }

    // Crée un joueur puis appelle onCreated avec son identifiant
    fun createPlayer(name: String, onCreated: (Long) -> Unit) {
        val cleanName = name.trim()
        when {
            cleanName.isEmpty() -> error.value = "Choisissez un pseudo"
            cleanName.length > MAX_NAME_LENGTH -> error.value = "$MAX_NAME_LENGTH caractères maximum"
            else -> viewModelScope.launch {
                val id = repository.createPlayer(cleanName)
                if (id == null) {
                    error.value = "Ce pseudo est déjà pris"
                } else {
                    error.value = null
                    onCreated(id)
                }
            }
        }
    }

    // Choisir un joueur : s'il est ruiné, il recommence avec le solde de départ
    fun selectPlayer(player: PlayerEntity, onReady: (Long) -> Unit) {
        viewModelScope.launch {
            if (player.balance <= 0) {
                repository.restartPlayer(player.id)
            }
            onReady(player.id)
        }
    }

    // Supprime un joueur (ses mains sont effacées avec lui grâce au CASCADE)
    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            repository.deletePlayer(playerId)
        }
    }

    fun clearError() {
        error.value = null
    }
}