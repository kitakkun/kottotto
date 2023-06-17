package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.Config
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
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.*

val koinModule = module {
    single { ResourceBundle.getBundle("strings") }

    single {
        val dbUrl = Config.get("DB_URL") ?: throw Exception("DB_URL is not set")
        val driver = Config.get("DB_DRIVER") ?: throw Exception("DB_DRIVER is not set")
        val user = Config.get("DB_USER") ?: throw Exception("DB_USER is not set")
        val password = Config.get("DB_PASSWORD") ?: throw Exception("DB_PASSWORD is not set")
        Database.connect(
            url = dbUrl,
            driver = driver,
            user = user,
            password = password,
        )
    }

    singleOf(::PingFeature)

    single { TempChannel }
    singleOf(::TempChannelRepository)
    singleOf(::TempChannelFeature)

    single { ReadChannel }
    singleOf(::ReadChannelFeature)
    singleOf(::ReadChannelRepository)

    factory<SoundEngine> { OpenJTalkEngine() }
    single { PlayerManager() }
}