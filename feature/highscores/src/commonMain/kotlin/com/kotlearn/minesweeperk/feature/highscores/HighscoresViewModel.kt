package com.kotlearn.minesweeperk.feature.highscores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.game.GetHighscoresAsFlowUseCase
import com.kotlearn.minesweeperk.domain.game.Highscore
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HighscoresViewModel(
    getHighscoresAsFlowUseCase: GetHighscoresAsFlowUseCase,
) : ViewModel() {

    private val allHighscores = MutableStateFlow<List<Highscore>>(emptyList())

    private val _selectedDifficulty = MutableStateFlow(Difficulty.DEFAULT)
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    // Only the selected level's scores are shown, so each difficulty has its own
    // leaderboard instead of one global list dominated by the fastest levels.
    val highscores: StateFlow<List<Highscore>> =
        combine(allHighscores, _selectedDifficulty) { scores, difficulty ->
            scores.filter { it.difficulty == difficulty }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            getHighscoresAsFlowUseCase().collect { allHighscores.value = it }
        }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
    }

}