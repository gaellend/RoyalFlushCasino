package com.gaelle.royalflushcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun CasinoApp() {
    val backStack = remember { mutableStateListOf<Any>(Hall()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavDisplay(
            backStack = backStack,
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