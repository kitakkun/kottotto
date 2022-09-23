package com.github.kitakkun.kottotto.event.manager

import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannelDataModel
import com.github.kitakkun.kottotto.event.EventStore
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TempChannelManager @Inject constructor(
    private val eventStore: EventStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

    private val logger = KotlinLogging.logger { }

    init {
        logger.debug { "Initializing..." }

        launch {
            eventStore.guildVoiceJoinEvent.collect { onJoin(it.channelJoined, it.guild, it.member) }
        }

        launch {
            eventStore.guildVoiceLeaveEvent.collect { onLeave(it.channelLeft, it.guild, it.member) }
        }

        launch {
            eventStore.guildVoiceMoveEvent.collect {
                onLeave(it.channelLeft, it.guild, it.member)
                onJoin(it.channelJoined, it.guild, it.member)
            }
        }
    }

    private suspend fun onJoin(channelJoined: AudioChannel, guild: Guild, member: Member) {
        val entry = getRegisteredData(channelJoined.idLong, guild.idLong)
        if (entry == null) {
            // create private temporal channel and role.
            val data = createTempChannel(
                name = channelJoined.name,
                parentCategory = guild.categories.find { it.voiceChannels.contains(channelJoined) },
                guild = guild,
                bindingChannelId = channelJoined.idLong
            )
            // register ids to database.
            registerTempChannel(
                bindingChannelId = data.bindingChannelId,
                roleId = data.roleId,
                channelId = data.channelId,
                guildId = data.guildId
            )
            // update member's role.
            guild.getRoleById(data.roleId)?.let {
                guild.addRoleToMember(member, it).queue()
            }
        } else {
            // update member's role.
            guild.getRoleById(entry.roleId)?.let {
                guild.addRoleToMember(member, it).queue()
            }
        }
    }

    private suspend fun onLeave(channelLeft: AudioChannel, guild: Guild, member: Member) {
        getRegisteredData(channelLeft.idLong, guild.idLong)?.let {
            // if no one exists in the voice channel...
            if (channelLeft.members.size == 0) {
                deleteTempChannel(guild, it.channelId, it.roleId)
                deregisterTempChannel(it.bindingChannelId, it.guildId)
            } else {
                // remove role from the member.
                guild.getRoleById(it.roleId)?.let { role ->
                    guild.removeRoleFromMember(member, role).queue()
                }
            }
        }
    }

    private suspend fun createTempChannel(guild: Guild, name: String, parentCategory: Category?, bindingChannelId: Long): TempChannelDataModel {
        logger.debug { "Creating a temporal private text channel at guild ${guild.id}" }
        val role = guild.createRole().setName(name).await()
        val channel = guild.createTextChannel(name)
            .addRolePermissionOverride(role.idLong, EnumSet.of(Permission.VIEW_CHANNEL), null)
            .addRolePermissionOverride(guild.publicRole.idLong, null, EnumSet.of(Permission.VIEW_CHANNEL))
            .setParent(parentCategory)
            .await()
        return TempChannelDataModel(
            bindingChannelId = bindingChannelId,
            channelId = channel.idLong,
            roleId = role.idLong,
            guildId = guild.idLong
        )
    }

    private suspend fun deleteTempChannel(guild: Guild, tempChannelId: Long, tempRoleId: Long) {
        logger.debug { "Deleting a temporal private text channel at guild ${guild.id}" }
        guild.channels.find { channel -> channel.idLong == tempChannelId }?.delete()?.queue()
        guild.roles.find { role -> role.idLong == tempRoleId }?.delete()?.queue()
    }

    private fun registerTempChannel(bindingChannelId: Long, roleId: Long, channelId: Long, guildId: Long) =
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            TempChannel.insert {
                it[bindChannelId] = bindingChannelId
                it[tempRoleId] = roleId
                it[tempChannelId] = channelId
                it[TempChannel.guildId] = guildId
            }
        }

    private fun deregisterTempChannel(bindingChannelId: Long, guildId: Long) =
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            TempChannel.deleteWhere {
                TempChannel.bindChannelId eq bindingChannelId
                TempChannel.guildId eq guildId
            }
        }

    private fun getRegisteredData(bindChannelId: Long, guildId: Long): TempChannelDataModel? = transaction {
        addLogger(Slf4jSqlDebugLogger)
        return@transaction TempChannel.select {
            TempChannel.bindChannelId eq bindChannelId
            TempChannel.guildId eq guildId
        }.firstOrNull()?.let { TempChannelDataModel.convert(it) }
    }
}
