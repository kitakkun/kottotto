package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.feature.ping.PingFeature
import com.github.kitakkun.kottotto.feature.read.ReadChannelFeature
import com.github.kitakkun.kottotto.feature.read.ReadChannelRepository
import com.github.kitakkun.kottotto.feature.temp.TempChannelFeature
import com.github.kitakkun.kottotto.feature.temp.TempChannelRepository
import com.github.kitakkun.kottotto.lavaplayer.PlayerManager
import com.github.kitakkun.kottotto.soundengine.OpenJTalkEngine
import com.github.kitakkun.kottotto.soundengine.SoundEngine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.*

val koinModule = module {
    single { ResourceBundle.getBundle("strings") }

    singleOf(::PingFeature)

    single { TempChannel }
    single { TempChannelFeature(get()) }
    singleOf(::TempChannelRepository)


    single { ReadChannel }
    single { ReadChannelRepository(get()) }
    single { ReadChannelFeature(get(), get(), get(), get()) }
    factory<SoundEngine> { OpenJTalkEngine() }
    single { PlayerManager() }
}