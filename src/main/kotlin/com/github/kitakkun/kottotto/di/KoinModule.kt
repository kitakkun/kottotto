package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.TempChannelRepository
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.feature.PingFeature
import com.github.kitakkun.kottotto.feature.TempChannelFeature
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val koinModule = module {
    single { TempChannel }
    single { TempChannelFeature(get()) }
    singleOf(::TempChannelRepository)
    singleOf(::PingFeature)
}