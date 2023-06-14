package com.github.kitakkun.kottotto.eventmanager

import com.github.kitakkun.kottotto.Config
import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.ReadChannelConfigData
import com.github.kitakkun.kottotto.event.EventStore
import com.github.kitakkun.kottotto.extensions.getString
import com.github.kitakkun.kottotto.lavaplayer.PlayerManager
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import runCommand
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ReadChannelEventManager @Inject constructor(
    private val eventStore: EventStore, private val bundle: ResourceBundle
) : CoroutineScope {

    private val logger = KotlinLogging.logger {}
    private val command = Command(name = "read", description = bundle.getString("cmd_read_desc"))
    private val subcommands = listOf(
        Subcommand("s", bundle.getString("cmd_read_s_desc")),
        Subcommand("e", bundle.getString("cmd_read_e_desc")),
        Subcommand("h", bundle.getString("cmd_read_h_desc")),
        Subcommand("status", bundle.getString("cmd_read_status_desc"))
    )

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    private fun getCurrentConfigurationByGuildId(guildId: Long): ReadChannelConfigData? = transaction {
        return@transaction ReadChannel.select { ReadChannel.guildId eq guildId }.map {
            addLogger(Slf4jSqlDebugLogger)
            ReadChannelConfigData(
                guildId = it[ReadChannel.guildId],
                textChannelId = it[ReadChannel.textChannelId],
                voiceChannelId = it[ReadChannel.voiceChannelId],
                ownerId = it[ReadChannel.ownerId]
            )
        }.firstOrNull()
    }

    init {

        logger.debug { "Initializing functions..." }

        launch {
            eventStore.readyEvent.collect {
                logger.debug { "Queued command registration." }
                it.jda.updateCommands {
                    addCommands(command.addSubcommands(subcommands))
                }.queue()
            }
        }

        launch {
            // start subcommand
            eventStore.slashCommandInteractionEvent.filter { it.name == command.name && it.subcommandName == "s" }.collect { event ->
                logger.debug { "slashCommand: \"${command.name} s\" called" }
                val guild = event.guild ?: return@collect
                val member = event.member ?: return@collect
                val voiceChannelId = event.member?.voiceState?.channel?.idLong ?: return@collect
                val responseMessage = onStart(guild = guild, textChannelId = event.channel.idLong, voiceChannelId = voiceChannelId, member = member)
                event.reply(responseMessage).queue()
            }
        }

        launch {
            // end subcommand
            eventStore.slashCommandInteractionEvent.filter { it.name == command.name && it.subcommandName == "e" }.collect { event ->
                logger.debug { "slashCommand: \"${command.name} e\" called" }
                val guild = event.guild ?: return@collect
                if (getCurrentConfigurationByGuildId(guild.idLong) != null) {
                    deregisterFromDatabase(guild.idLong)
                    event.reply(bundle.getString("unsubscribe_read")).queue()
                    event.guild?.audioManager?.closeAudioConnection()
                } else {
                    event.reply(bundle.getString("no_configuration")).queue()
                }
            }
        }

        launch {
            // help subcommand
            eventStore.slashCommandInteractionEvent.filter { it.name == command.name && it.subcommandName == "h" }.collect { event ->
                logger.debug { "slashCommand: \"${command.name} h\" called" }
                event.replyEmbeds(Embed {
                    title = bundle.getString("cmd_read_h_msg_title")
                    description = bundle.getString("cmd_read_h_msg_desc")
                    field(bundle.getString("cmd_read_h_msg_field1_title"), bundle.getString("cmd_read_h_msg_field1_value"), inline = false)
                    field {
                        field(bundle.getString("cmd_read_h_msg_field2_title"), bundle.getString("cmd_read_h_msg_field2_value"), inline = true)
                        field(bundle.getString("cmd_read_h_msg_field3_title"), bundle.getString("cmd_read_h_msg_field3_value"), inline = true)
                    }
                    field {
                        field(bundle.getString("cmd_read_h_msg_field4_title"), bundle.getString("cmd_read_h_msg_field4_value"), inline = true)
                        field(bundle.getString("cmd_read_h_msg_field5_title"), bundle.getString("cmd_read_h_msg_field5_value"), inline = true)
                    }
                }).setEphemeral(true).queue()
            }
        }

        launch {
            eventStore.slashCommandInteractionEvent.filter { it.name == command.name && it.subcommandName == "status" }.collect { event ->
                logger.debug { "slashCommand: \"${command.name} status\" called" }
                val guild = event.guild ?: return@collect
                val config = getCurrentConfigurationByGuildId(guild.idLong)
                if (config == null) {
                    event.replyEmbeds(Embed {
                        title = bundle.getString("cmd_read_status_msg_title")
                        description = bundle.getString("cmd_read_status_msg_not_used_desc")
                    }).setEphemeral(true).queue()
                    return@collect
                }
                event.replyEmbeds(Embed {
                    title = bundle.getString("cmd_read_status_msg_title")
                    description = bundle.getString("cmd_read_status_msg_used_desc")
                    field {
                        name = bundle.getString("cmd_read_status_msg_used_field1_title")
                        value = bundle.getString("discord_text_channel_expression", config.textChannelId)
                    }
                    field {
                        name = bundle.getString("cmd_read_status_msg_used_field2_title")
                        value = bundle.getString(
                            "discord_voice_channel_expression",
                            event.guild?.getVoiceChannelById(config.voiceChannelId)?.name ?: ""
                        )
                    }
                    field {
                        name = bundle.getString("cmd_read_status_msg_used_field3_title")
                        value = bundle.getString("discord_member_expression", config.ownerId)
                    }
                }).setEphemeral(true).queue()
            }
        }

        launch {
            eventStore.messageReceivedEvent.collect { event ->
                val config = getCurrentConfigurationByGuildId(event.guild.idLong) ?: return@collect
                if (config.textChannelId != event.guildChannel.idLong) return@collect
                val voiceChannel = event.guild.getVoiceChannelById(config.voiceChannelId) ?: return@collect

                event.guild.audioManager.openAudioConnection(voiceChannel)

                Config.apply {
                    val openJTalkExecutable = get("OPEN_JTALK")
                    val voice = get("OPEN_JTALK_HTS_VOICE")
                    val dictionary = get("OPEN_JTALK_DICTIONARY")
                    val file = File("test.txt")
                    file.writeText(event.message.contentDisplay, Charsets.UTF_8)
                    val process = "$openJTalkExecutable -m $voice -x $dictionary -ow test.wav ${file.absolutePath}".runCommand()
                    process.waitFor()
                    process.destroy()
                    PlayerManager.loadLocalSourceAndPlay(event.guild, "test.wav")
                }
            }
        }

        launch {
            eventStore.guildVoiceLeaveEvent.collect { event ->
                getCurrentConfigurationByGuildId(event.guild.idLong) ?: return@collect
                if (event.channelLeft.members.size != 1) return@collect
                deregisterFromDatabase(event.guild.idLong)
                event.guild.audioManager.closeAudioConnection()
            }
        }
    }

    private fun onStart(guild: Guild, member: Member, textChannelId: Long, voiceChannelId: Long): MessageCreateData {
        val entry = getCurrentConfigurationByGuildId(guildId = guild.idLong)
        return if (entry == null) {
            registerToDatabase(guildId = guild.idLong, ownerId = member.idLong, textChannelId = textChannelId, voiceChannelId = voiceChannelId)
            generateRegisteredMessageEmbed(
                textChannelId = textChannelId,
                voiceChannelName = guild.getVoiceChannelById(voiceChannelId)?.name ?: "",
                memberId = member.idLong
            )
        } else {
            MessageCreateData.fromContent(bundle.getString("read_channel_configuration_exist"))
        }
    }

    private fun generateRegisteredMessageEmbed(textChannelId: Long, voiceChannelName: String, memberId: Long) =
        MessageCreateData.fromEmbeds(Embed {
            title = bundle.getString("cmd_read_s_msg_registered_title")
            field(
                name = bundle.getString("cmd_read_s_msg_registered_field1_title"),
                value = bundle.getString("discord_text_channel_expression", textChannelId),
                inline = false
            )
            field(
                name = bundle.getString("cmd_read_s_msg_registered_field2_title"), value = bundle.getString(
                    "discord_voice_channel_expression", voiceChannelName
                ), inline = false
            )
            field(
                name = bundle.getString("cmd_read_s_msg_registered_field3_title"),
                value = bundle.getString("discord_member_expression", memberId),
                inline = false
            )
        })

    private fun generateStatusMessageCreateData() = MessageCreateData.fromEmbeds(
        Embed {

        }
    )

    private fun registerToDatabase(guildId: Long, ownerId: Long, textChannelId: Long, voiceChannelId: Long) = transaction {
        addLogger(Slf4jSqlDebugLogger)
        ReadChannel.insert {
            it[ReadChannel.guildId] = guildId
            it[ReadChannel.ownerId] = ownerId
            it[ReadChannel.textChannelId] = textChannelId
            it[ReadChannel.voiceChannelId] = voiceChannelId
        }
    }

    private fun deregisterFromDatabase(guildId: Long) = transaction {
//        ReadChannel.deleteWhere { ReadChannel.guildId eq guildId }
    }

}
