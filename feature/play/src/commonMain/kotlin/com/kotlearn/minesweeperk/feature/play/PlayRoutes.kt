package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object Play

fun NavGraphBuilder.playRoutes() {

    composable<Play> {
        val viewModel: PlayViewModel = koinViewModel()
        PlayScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
        )
    }

}
