package com.kotlearn.minesweeperk

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kotlearn.minesweeperk.feature.highscores.Highscores
import com.kotlearn.minesweeperk.feature.highscores.highscoresRoutes
import com.kotlearn.minesweeperk.feature.menu.Menu
import com.kotlearn.minesweeperk.feature.menu.menuRoutes
import com.kotlearn.minesweeperk.feature.play.Play
import com.kotlearn.minesweeperk.feature.play.playRoutes
import com.kotlearn.minesweeperk.feature.settings.Settings
import com.kotlearn.minesweeperk.feature.settings.settingsRoutes
import com.kotlearn.minesweeperk.ui.core.LocalSystemPaddingValue
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

@Composable
@Preview
fun App(
    platformModule: Module = Module(),
) {
    KoinApplication(
        application = {
            modules(appModule, platformModule)
        }
    ) {
        MaterialTheme {
            val navController = rememberNavController()
            Scaffold {
                CompositionLocalProvider(LocalSystemPaddingValue provides it) {
                    NavHost(
                        navController = navController,
                        startDestination = Menu,
                        modifier = Modifier.fillMaxSize(),

                        ) {
                        menuRoutes(
                            goToPlay = {
                                navController.navigate(Play)
                            },
                            goToHighscores = {
                                navController.navigate(Highscores)
                            },
                            goToSettings = {
                                navController.navigate(Settings)
                            },
                        )
                        playRoutes(
                            onNavigateBack = {
                                navController.navigateUp()
                            },
                        )
                        highscoresRoutes()
                        settingsRoutes(
                            onNavigateBack = {
                                navController.navigateUp()
                            },
                        )
                    }
                }
            }
        }
    }
}



