package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.event.EventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class BotClient @Inject constructor(
    private val eventStore: EventStore
) : CoroutineScope, EventListener {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    init {
        JDABuilder.createDefault(Config.get("TOKEN"))
            .setEnabledIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            )
            .addEventListeners(this)
            .build()
    }

    override fun onEvent(event: GenericEvent) {
        launch {
            if (event is ReadyEvent) eventStore.produceReadyEvent(event)
            if (event is MessageReceivedEvent) eventStore.produceMessageReceivedEvent(event)
            if (event is GuildVoiceJoinEvent) eventStore.produceGuildVoiceJoinEvent(event)
            if (event is GuildVoiceLeaveEvent) eventStore.produceGuildVoiceLeaveEvent(event)
            if (event is GuildReadyEvent) eventStore.produceGuildReadyEvent(event)
            if (event is SlashCommandInteractionEvent) eventStore.produceSlashCommandInteractionEvent(event)
            if (event is GuildVoiceMoveEvent) eventStore.produceGuildVoiceMoveEvent(event)
        }
    }
}
