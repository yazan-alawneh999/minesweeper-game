package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultBoardSizeRepository(
    private val storage: Storage,
) : BoardSizeRepository {

    override val columns: Flow<Int> = storage.getAsFlow(BoardSizeRepository.ColumnsKey)
        .map { it ?: BoardSizeRepository.ColumnsKey.defaultValue ?: 10 }

    override val rows: Flow<Int> = storage.getAsFlow(BoardSizeRepository.RowsKey)
        .map { it ?: BoardSizeRepository.RowsKey.defaultValue ?: 16 }

    override suspend fun updateSize(columns: Int, rows: Int) {
        storage.writeValue(BoardSizeRepository.ColumnsKey, columns)
        storage.writeValue(BoardSizeRepository.RowsKey, rows)
    }
}
