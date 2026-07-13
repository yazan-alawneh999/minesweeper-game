package com.kotlearn.minesweeperk.feature.highscores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.game.GetHighscoresAsFlowUseCase
import com.kotlearn.minesweeperk.domain.game.Highscore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class HighscoresViewModel(
    getHighscoresAsFlowUseCase: GetHighscoresAsFlowUseCase,
) : ViewModel() {

    private val _highscores = MutableStateFlow<List<Highscore>>(emptyList())
    val highscores = _highscores.asStateFlow()

    init {
        viewModelScope.launch {
            getHighscoresAsFlowUseCase().collect {
                _highscores.value = it
            }
        }
    }

}
