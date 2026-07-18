package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.BoardSizeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBoardSizeAsFlowUseCase(
    private val repository: BoardSizeRepository,
) {
    operator fun invoke(): Flow<BoardSize> =
        combine(repository.columns, repository.rows) { c, r -> BoardSize.of(columns = c, rows = r) }
}
