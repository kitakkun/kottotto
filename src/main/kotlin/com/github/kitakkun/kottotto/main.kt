package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.di.koinModule
import com.github.kitakkun.kottotto.feature.gpt.GPTFeature
import com.github.kitakkun.kottotto.feature.ping.PingFeature
import com.github.kitakkun.kottotto.feature.read.ReadChannelFeature
import com.github.kitakkun.kottotto.feature.team.TeamFeature
import com.github.kitakkun.kottotto.feature.temp.TempChannelFeature
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

fun main() {
    startKoin {
        modules(koinModule)
    }

    val tempChannelFeature: TempChannelFeature by inject(TempChannelFeature::class.java)
    val pingFeature: PingFeature by inject(PingFeature::class.java)
    val readChannelFeature: ReadChannelFeature by inject(ReadChannelFeature::class.java)
    val gptFeature: GPTFeature by inject(GPTFeature::class.java)
    val teamFeature: TeamFeature by inject(TeamFeature::class.java)

    JDABuilder.createDefault(Config.get("TOKEN"))
        .setEnabledIntents(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.SCHEDULED_EVENTS,
        )
        .build().apply {
            tempChannelFeature.register(this)
            pingFeature.register(this)
            readChannelFeature.register(this)
            gptFeature.register(this)
            teamFeature.register(this)
        }
}
