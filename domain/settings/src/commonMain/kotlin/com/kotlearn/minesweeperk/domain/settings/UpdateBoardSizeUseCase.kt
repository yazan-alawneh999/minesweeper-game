package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.BoardSizeRepository

class UpdateBoardSizeUseCase(
    private val repository: BoardSizeRepository,
) {
    suspend operator fun invoke(size: BoardSize) {
        repository.updateSize(columns = size.columns, rows = size.rows)
    }
}
