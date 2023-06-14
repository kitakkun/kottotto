package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.di.koinModule
import com.github.kitakkun.kottotto.feature.TempChannelFeature
import com.github.kitakkun.kottotto.feature.setupPingCommand
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val kord = Kord(Config.get("TOKEN"))

    startKoin {
        modules(koinModule)
    }

    val tempChannelFeature: TempChannelFeature by inject(TempChannelFeature::class.java)
    tempChannelFeature.handleEvents(kord)

    setupPingCommand(kord)

    kord.login {
        intents = Intents(
            Intent.GuildVoiceStates,
            Intent.GuildMembers,
            Intent.GuildIntegrations,
        )
    }
}
