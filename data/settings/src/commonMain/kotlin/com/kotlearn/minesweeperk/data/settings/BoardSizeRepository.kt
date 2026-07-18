package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface BoardSizeRepository {

    val columns: Flow<Int>
    val rows: Flow<Int>

    suspend fun updateSize(columns: Int, rows: Int)

    data object ColumnsKey : Storage.Key.IntKey("board_columns", 10)
    data object RowsKey : Storage.Key.IntKey("board_rows", 16)
}
