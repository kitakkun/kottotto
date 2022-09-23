package com.github.kitakkun.kottotto.event.manager

import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannelDataModel
import com.github.kitakkun.kottotto.event.EventStore
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.Category
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

    init {
        launch {
            eventStore.guildVoiceJoinEvent.collect { event ->
                val entry = getRegisteredData(event.channelJoined.idLong, event.guild.idLong)
                if (entry == null) {
                    // create private temporal channel and role.
                    val data = createTempChannel(
                        name = event.channelJoined.name,
                        parentCategory = event.guild.categories.find { it.voiceChannels.contains(event.channelJoined) },
                        guild = event.guild,
                        bindingChannelId = event.channelJoined.idLong
                    )
                    // register ids to database.
                    registerTempChannel(
                        bindingChannelId = data.bindingChannelId,
                        roleId = data.roleId,
                        channelId = data.channelId,
                        guildId = data.guildId
                    )
                    // update member's role.
                    event.guild.getRoleById(data.roleId)?.let {
                        event.guild.addRoleToMember(event.member, it).queue()
                    }
                } else {
                    // update member's role.
                    event.guild.getRoleById(entry.roleId)?.let {
                        event.guild.addRoleToMember(event.member, it).queue()
                    }
                }
            }
        }
        launch {
            eventStore.guildVoiceLeaveEvent.collect { event ->
                getRegisteredData(event.channelLeft.idLong, event.guild.idLong)?.let {
                    // if no one exists in the voice channel...
                    if (event.channelLeft.members.size == 0) {
                        deleteTempChannel(event.guild, it.channelId, it.roleId)
                        deregisterTempChannel(it.bindingChannelId, it.guildId)
                    } else {
                        // remove role from the member.
                        event.guild.getRoleById(it.roleId)?.let { role ->
                            event.guild.removeRoleFromMember(event.member, role).queue()
                        }
                    }
                }
            }
        }
        launch {
            eventStore.readyEvent.collect {
                println("READY")
            }
        }
    }

    private suspend fun createTempChannel(guild: Guild, name: String, parentCategory: Category?, bindingChannelId: Long): TempChannelDataModel {
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
