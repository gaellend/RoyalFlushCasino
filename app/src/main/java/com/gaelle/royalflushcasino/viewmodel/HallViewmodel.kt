package com.gaelle.royalflushcasino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gaelle.royalflushcasino.data.CasinoRepository
import com.gaelle.royalflushcasino.data.local.PlayerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HallViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CasinoRepository(application)

    // Les trois meilleurs records, pour l'aperçu du classement
    val topPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())

    init {
        viewModelScope.launch {
            repository.leaderboard()
                .map { it.take(3) } // opérateur map des Flow : on ne garde que le podium
                .collect { topPlayers.value = it }
        }
    }
}