package com.gaelle.royalflushcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gaelle.royalflushcasino.data.Card
import com.gaelle.royalflushcasino.data.DeckRepository
import com.gaelle.royalflushcasino.ui.theme.RoyalFlushCasinoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoyalFlushCasinoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestApi(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Test temporaire : vérifie que Ktor (API) et Coil (image) fonctionnent.
// Sera supprimé quand on passera au ViewModel.
@Composable
fun TestApi(modifier: Modifier = Modifier) {
    val repo = remember { DeckRepository() }
    var card by remember { mutableStateOf<Card?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val deck = repo.newDeck()
            card = repo.draw(deck.deckId, 1).cards.first()
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        card?.let {
            AsyncImage(
                model = it.image,
                contentDescription = "${it.value} of ${it.suit}",
                modifier = Modifier.width(150.dp)
            )
            Text("${it.value} de ${it.suit}")
        }
        error?.let { Text("Erreur : $it") }
    }
}