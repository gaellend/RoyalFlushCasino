package com.gaelle.royalflushcasino.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DeckRepository {

    private val baseUrl = "https://deckofcardsapi.com/api/deck"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // ignore les champs du JSON qu'on n'a pas déclarés
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor-Logger", message)
                }
            }
        }
    }

    // Crée un nouveau paquet mélangé (6 jeux de cartes par défaut, comme au Blackjack)
    suspend fun newDeck(deckCount: Int = 6): DeckResponse {
        return client.get("$baseUrl/new/shuffle/") {
            parameter("deck_count", deckCount)
        }.body()
    }

    // Pioche "count" cartes dans le paquet
    suspend fun draw(deckId: String, count: Int = 1): DrawResponse {
        return client.get("$baseUrl/$deckId/draw/") {
            parameter("count", count)
        }.body()
    }

    // Remélange toutes les cartes du paquet
    suspend fun reshuffle(deckId: String): DeckResponse {
        return client.get("$baseUrl/$deckId/shuffle/").body()
    }
}