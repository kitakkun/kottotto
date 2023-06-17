package com.github.kitakkun.kottotto.feature.read

import com.github.kitakkun.kottotto.extensions.join
import com.github.kitakkun.kottotto.extensions.leave
import com.github.kitakkun.kottotto.feature.Feature
import com.github.kitakkun.kottotto.lavaplayer.PlayerManager
import com.github.kitakkun.kottotto.soundengine.SoundEngine
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.Subcommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ReadChannelFeature(
    bundle: ResourceBundle,
    private val readChannelRepository: ReadChannelRepository,
    private val soundEngine: SoundEngine,
    private val playerManager: PlayerManager,
) : Feature, ListenerAdapter(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Job() + Dispatchers.IO

    private val command = Command(name = "read", description = bundle.getString("cmd_read_desc")) {
        addSubcommands(
            listOf(
                Subcommand("s", bundle.getString("cmd_read_s_desc")),
                Subcommand("e", bundle.getString("cmd_read_e_desc")),
                Subcommand("h", bundle.getString("cmd_read_h_desc")),
                Subcommand("status", bundle.getString("cmd_read_status_desc"))
            )
        )
    }

    private val mutableMessageFlows = mutableMapOf<Guild, MutableSharedFlow<String>>()

    override fun register(jda: JDA) {
        jda.guilds.forEach { guild ->
            guild.upsertCommand(command).queue()
        }
        jda.addEventListener(this)
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val messageFlow = mutableMessageFlows.computeIfAbsent(guild) { _ ->
            MutableSharedFlow()
        }
        launch {
            messageFlow.collect { message ->
                speak(guild, message)
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return
        if (event.name != command.name) return
        val subcommand = event.subcommandName ?: return
        when (subcommand) {
            "s" -> {
                handleStartCommand(event)
            }

            "e" -> {
                handleEndCommand(event)
            }

            "h" -> {
                event.interaction.reply("h").queue()
            }

            "status" -> {
                event.interaction.reply("status").queue()
            }
        }
    }

    private fun handleStartCommand(event: SlashCommandInteractionEvent) {
        if (!isReadServiceAvailable(event.member)) {
            event.reply("service unavailable").queue()
            return
        }

        registerReadTextService(
            guildId = event.guild?.idLong ?: return,
            voiceChannelId = event.member?.voiceState?.channel?.idLong ?: return,
            textChannelId = event.channel.idLong,
            ownerId = event.member?.idLong ?: return,
        )

        event.member?.voiceState?.channel?.join()
        return event.reply("service started").queue()
    }

    private fun isReadServiceAvailable(member: Member?): Boolean {
        return member?.voiceState?.inAudioChannel() == true
    }

    private fun registerReadTextService(
        guildId: Long,
        voiceChannelId: Long,
        textChannelId: Long,
        ownerId: Long,
    ) {
        readChannelRepository.create(
            guildId = guildId,
            voiceChannelId = voiceChannelId,
            textChannelId = textChannelId,
            ownerId = ownerId,
        )
    }

    private fun handleEndCommand(event: SlashCommandInteractionEvent) {
        unregisterReadTextService(guildId = event.guild?.idLong ?: return)
        event.guild?.selfMember?.voiceState?.channel?.leave()
        event.reply("service ended").queue()
    }

    private fun unregisterReadTextService(guildId: Long) {
        readChannelRepository.delete(guildId)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return

        val readConfig = readChannelRepository.fetch(event.guild.idLong) ?: return
        val textChannel = event.guild.getTextChannelById(readConfig.textChannelId) ?: return
        if (event.channel.idLong != textChannel.idLong) return

        val voiceChannel = event.guild.getVoiceChannelById(readConfig.voiceChannelId) ?: return
        voiceChannel.join()

        launch {
            mutableMessageFlows[event.guild]?.emit(event.message.contentDisplay)
        }
    }

    private fun speak(guild: Guild, message: String) {
        val file = File("test.txt")
        file.writeText(message, Charsets.UTF_8)
        val soundFile = soundEngine.generateSoundFileFromText(inputTextFilePath = file.absolutePath) ?: return
        playerManager.loadLocalSourceAndPlay(guild = guild, url = soundFile.absolutePath)
    }
}