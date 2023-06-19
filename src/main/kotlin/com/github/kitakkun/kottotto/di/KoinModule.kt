package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.Config
import com.github.kitakkun.kottotto.database.ReadChannelTable
import com.github.kitakkun.kottotto.database.TempChannelTable
import com.github.kitakkun.kottotto.feature.gpt.GPTFeature
import com.github.kitakkun.kottotto.feature.ping.PingFeature
import com.github.kitakkun.kottotto.feature.read.ReadChannelFeature
import com.github.kitakkun.kottotto.feature.read.ReadChannelRepository
import com.github.kitakkun.kottotto.feature.team.TeamFeature
import com.github.kitakkun.kottotto.feature.temp.TempChannelFeature
import com.github.kitakkun.kottotto.feature.temp.TempChannelRepository
import com.github.kitakkun.kottotto.lavaplayer.PlayerManager
import com.github.kitakkun.kottotto.soundengine.SoundEngine
import com.github.kitakkun.kottotto.soundengine.voicevox.VoiceVoxEngine
import com.github.kitakkun.ktvox.api.KtVoxApi
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    single { TempChannelTable }
    singleOf(::TempChannelRepository)
    singleOf(::TempChannelFeature)

    single { ReadChannelTable }
    singleOf(::ReadChannelFeature)
    singleOf(::ReadChannelRepository)

    factory<SoundEngine> { VoiceVoxEngine(get()) }

    single<Retrofit>(named("voicevox")) {
        Retrofit.Builder()
            .baseUrl(Config.get("VOICEVOX_SERVER_URL") ?: throw Exception("VOICEVOX_SERVER_URL is not set"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    factory<KtVoxApi> {
        val retrofit: Retrofit = get(named("voicevox"))
        retrofit.create(KtVoxApi::class.java)
    }

    single { PlayerManager() }

    singleOf(::GPTFeature)

    singleOf(::TeamFeature)
}
