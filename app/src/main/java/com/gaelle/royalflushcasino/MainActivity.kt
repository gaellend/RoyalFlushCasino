package com.gaelle.royalflushcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.gaelle.royalflushcasino.navigation.BlackjackTable
import com.gaelle.royalflushcasino.navigation.Hall
import com.gaelle.royalflushcasino.navigation.History
import com.gaelle.royalflushcasino.ui.screens.BlackjackScreen
import com.gaelle.royalflushcasino.ui.screens.HallScreen
import com.gaelle.royalflushcasino.ui.screens.HistoryScreen
import com.gaelle.royalflushcasino.ui.theme.CasinoBlack
import com.gaelle.royalflushcasino.ui.theme.Gold
import com.gaelle.royalflushcasino.ui.theme.RoyalFlushCasinoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            RoyalFlushCasinoTheme {
                CasinoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasinoApp() {
    // La pile de navigation, avec le hall comme destination initiale
    val backStack = remember { mutableStateListOf<Any>(Hall()) }

    // Le titre dépend de la destination en haut de la pile
    val title = when (backStack.lastOrNull()) {
        is Hall -> "Royal Flush Casino"
        is BlackjackTable -> "Blackjack"
        is History -> "Historique"
        else -> ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CasinoBlack,
                    titleContentColor = Gold
                )
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<Hall> {
                    HallScreen(
                        onBlackjack = { backStack.add(BlackjackTable()) },
                        onHistory = { backStack.add(History()) }
                    )
                }
                entry<BlackjackTable> {
                    BlackjackScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<History> {
                    HistoryScreen(onBack = { backStack.removeLastOrNull() })
                }
            }
        )
    }
}