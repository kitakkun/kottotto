package com.github.kitakkun.kottotto.event.manager

import com.github.kitakkun.kottotto.event.EventStore
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannel.tempRoleId
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
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
                    val role = createTempChannel(
                        name = event.channelJoined.name,
                        guild = event.guild,
                        bindingChannelId = event.channelJoined.idLong
                    )
                    event.guild.addRoleToMember(event.member, role).queue()
                } else {
                    event.guild.getRoleById(entry[tempRoleId])?.let {
                        event.guild.addRoleToMember(event.member, it).queue()
                    }
                }
            }
        }
        launch {
            eventStore.guildVoiceLeaveEvent.collect { event ->
                val entry = getRegisteredData(event.channelLeft.idLong, event.guild.idLong)
                entry?.let {
                    if (event.channelLeft.members.size == 0) {
                        event.guild.channels.find { channel -> channel.idLong == it[TempChannel.tempChannelId] }
                            ?.delete()
                            ?.queue()
                        event.guild.roles.find { role -> role.idLong == it[TempChannel.tempRoleId] }?.delete()?.queue()
                        transaction {
                            addLogger(StdOutSqlLogger)
                            TempChannel.deleteWhere {
                                TempChannel.bindChannelId eq event.channelLeft.idLong
                            }
                        }
                    } else {
                        event.guild.getRoleById(entry[tempRoleId])?.let {
                            event.guild.removeRoleFromMember(event.member, it).queue()
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

    private suspend fun createTempChannel(guild: Guild, name: String, bindingChannelId: Long): Role {
        val role = guild.createRole().setName(name).await()
        val channel = guild.createTextChannel(name)
            .addRolePermissionOverride(role.idLong, EnumSet.of(Permission.VIEW_CHANNEL), null)
            .addRolePermissionOverride(guild.publicRole.idLong, null, EnumSet.of(Permission.VIEW_CHANNEL))
            .await()
        transaction {
            addLogger(StdOutSqlLogger)
            TempChannel.insert {
                it[bindChannelId] = bindingChannelId
                it[tempRoleId] = role.idLong
                it[tempChannelId] = channel.idLong
                it[guildId] = guild.idLong
            }
        }
        return role
    }

    private fun getRegisteredData(bindChannelId: Long, guildId: Long): ResultRow? = transaction {
        addLogger(StdOutSqlLogger)
        return@transaction TempChannel.select { TempChannel.bindChannelId eq bindChannelId }.firstOrNull()
    }
}
