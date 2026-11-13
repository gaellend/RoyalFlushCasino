package com.gaelle.royalflushcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.gaelle.royalflushcasino.navigation.BlackjackTable
import com.gaelle.royalflushcasino.navigation.Hall
import com.gaelle.royalflushcasino.navigation.History
import com.gaelle.royalflushcasino.navigation.Players
import com.gaelle.royalflushcasino.ui.screens.BlackjackScreen
import com.gaelle.royalflushcasino.ui.screens.HallScreen
import com.gaelle.royalflushcasino.ui.screens.HistoryScreen
import com.gaelle.royalflushcasino.ui.screens.PlayersScreen
import com.gaelle.royalflushcasino.ui.theme.RoyalFlushCasinoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            // Comme dans le cours : on récupère les classes de taille d'écran dans le setContent
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            RoyalFlushCasinoTheme {
                CasinoApp(windowSizeClass)
            }
        }
    }
}

@Composable
fun CasinoApp(windowSizeClass: WindowSizeClass) {
    // Pile de navigation sauvegardée : on reste sur le bon écran quand on tourne le téléphone
    val backStack = rememberNavBackStack(Hall)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Hall> {
                    HallScreen(
                        onBlackjack = { backStack.add(Players) }, // on choisit d'abord le joueur
                        onHistory = { backStack.add(History) }
                    )
                }
                entry<Players> {
                    PlayersScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onPlayerSelected = { playerId -> backStack.add(BlackjackTable(playerId)) }
                    )
                }
                entry<BlackjackTable> { key ->
                    // key.playerId : le joueur choisi (utilisé à l'étape 3)
                    BlackjackScreen(
                        windowSizeClass = windowSizeClass,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<History> {
                    HistoryScreen(onBack = { backStack.removeLastOrNull() })
                }
            }
        )
    }
}