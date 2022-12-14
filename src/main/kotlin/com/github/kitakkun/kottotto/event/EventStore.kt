package com.github.kitakkun.kottotto.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface EventStore {
    val guildVoiceJoinEvent: SharedFlow<GuildVoiceJoinEvent>
    val guildVoiceLeaveEvent: SharedFlow<GuildVoiceLeaveEvent>
    val readyEvent: SharedFlow<ReadyEvent>
    val slashCommandInteractionEvent: SharedFlow<SlashCommandInteractionEvent>
    val messageReceivedEvent: SharedFlow<MessageReceivedEvent>
    val guildReadyEvent: SharedFlow<GuildReadyEvent>
    val guildVoiceMoveEvent: SharedFlow<GuildVoiceMoveEvent>

    suspend fun produceReadyEvent(event: ReadyEvent)
    suspend fun produceGuildVoiceJoinEvent(event: GuildVoiceJoinEvent)
    suspend fun produceGuildVoiceLeaveEvent(event: GuildVoiceLeaveEvent)
    suspend fun produceMessageReceivedEvent(event: MessageReceivedEvent)
    suspend fun produceGuildReadyEvent(event: GuildReadyEvent)
    suspend fun produceSlashCommandInteractionEvent(event: SlashCommandInteractionEvent)
    suspend fun produceGuildVoiceMoveEvent(event: GuildVoiceMoveEvent)
}

class EventStoreImpl : EventStore {

    private val mutableReadyEvent = MutableSharedFlow<ReadyEvent>()
    override val readyEvent = mutableReadyEvent.asSharedFlow()

    private val mutableGuildVoiceJoinEvent = MutableSharedFlow<GuildVoiceJoinEvent>()
    override val guildVoiceJoinEvent = mutableGuildVoiceJoinEvent.asSharedFlow()

    private val mutableGuildVoiceLeaveEvent = MutableSharedFlow<GuildVoiceLeaveEvent>()
    override val guildVoiceLeaveEvent = mutableGuildVoiceLeaveEvent.asSharedFlow()

    private val mutableSlashCommandInteractionEvent = MutableSharedFlow<SlashCommandInteractionEvent>()
    override val slashCommandInteractionEvent = mutableSlashCommandInteractionEvent.asSharedFlow()

    private val mutableMessageReceivedEvent = MutableSharedFlow<MessageReceivedEvent>()
    override val messageReceivedEvent = mutableMessageReceivedEvent.asSharedFlow()

    private val mutableGuildReadyEvent = MutableSharedFlow<GuildReadyEvent>()
    override val guildReadyEvent: SharedFlow<GuildReadyEvent> = mutableGuildReadyEvent.asSharedFlow()

    private val mutableGuildVoiceMoveEvent = MutableSharedFlow<GuildVoiceMoveEvent>()
    override val guildVoiceMoveEvent: SharedFlow<GuildVoiceMoveEvent> = mutableGuildVoiceMoveEvent.asSharedFlow()

    override suspend fun produceReadyEvent(event: ReadyEvent) = mutableReadyEvent.emit(event)
    override suspend fun produceGuildVoiceJoinEvent(event: GuildVoiceJoinEvent) = mutableGuildVoiceJoinEvent.emit(event)
    override suspend fun produceGuildVoiceLeaveEvent(event: GuildVoiceLeaveEvent) = mutableGuildVoiceLeaveEvent.emit(event)
    override suspend fun produceMessageReceivedEvent(event: MessageReceivedEvent) = mutableMessageReceivedEvent.emit(event)
    override suspend fun produceGuildReadyEvent(event: GuildReadyEvent) = mutableGuildReadyEvent.emit(event)
    override suspend fun produceSlashCommandInteractionEvent(event: SlashCommandInteractionEvent) = mutableSlashCommandInteractionEvent.emit(event)
    override suspend fun produceGuildVoiceMoveEvent(event: GuildVoiceMoveEvent) = mutableGuildVoiceMoveEvent.emit(event)
}
