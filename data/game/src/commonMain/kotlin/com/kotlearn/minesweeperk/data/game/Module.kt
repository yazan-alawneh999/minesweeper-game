package com.kotlearn.minesweeperk.data.game

import com.kotlearn.minesweeperk.data.core.dataCoreModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataGameModule = module {

    includes(dataCoreModule)

    singleOf(::DefaultHighscoreRepository) {
        bind<HighscoreRepository>()
    }

}