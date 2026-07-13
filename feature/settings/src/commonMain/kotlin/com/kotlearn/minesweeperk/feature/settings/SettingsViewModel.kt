package com.kotlearn.minesweeperk.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import com.kotlearn.minesweeperk.domain.settings.GetDifficultyAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.GetUsernameAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.UpdateDifficultyUseCase
import com.kotlearn.minesweeperk.domain.settings.UpdateUsernameUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val getUsernameAsFlowUseCase: GetUsernameAsFlowUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val getDifficultyAsFlowUseCase: GetDifficultyAsFlowUseCase,
    private val updateDifficultyUseCase: UpdateDifficultyUseCase,
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _difficulty = MutableStateFlow(Difficulty.DEFAULT)
    val difficulty = _difficulty.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            getUsernameAsFlowUseCase().firstOrNull()?.let {
                _username.value = it
            }
        }
        viewModelScope.launch {
            getDifficultyAsFlowUseCase().firstOrNull()?.let {
                _difficulty.value = it
            }
        }
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setDifficulty(difficulty: Difficulty) {
        _difficulty.value = difficulty
        viewModelScope.launch {
            updateDifficultyUseCase(difficulty)
        }
    }

    fun save() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            updateUsernameUseCase(_username.value)
            // Keep the loading state visible briefly so the feedback is perceivable.
            delay(500)
            _isSaving.value = false
        }
    }

}