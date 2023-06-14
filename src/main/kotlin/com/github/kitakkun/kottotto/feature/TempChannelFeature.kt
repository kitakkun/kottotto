package com.github.kitakkun.kottotto.feature

import com.github.kitakkun.kottotto.TempChannelRepository
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannel.roleId
import com.github.kitakkun.kottotto.database.TempChannelConfigData
import com.github.kitakkun.kottotto.extensions.getCategoryByChannelId
import com.github.kitakkun.kottotto.extensions.getString
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.core.Kord
import dev.kord.core.behavior.createRole
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import okhttp3.internal.wait
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

class TempChannelFeature(
    private val tempChannelRepository: TempChannelRepository,
) {
    fun handleEvents(kord: Kord) {
        kord.on<VoiceStateUpdateEvent> {
            when {
                isJoinEvent() -> {
                    handleJoinEvent(event = this)
                }

                isMoveEvent() -> {
                    handleMoveEvent(event = this)
                }

                isLeaveEvent() -> {
                    handleLeaveEvent(event = this)
                }
            }
        }
    }

    private suspend fun handleJoinEvent(event: VoiceStateUpdateEvent) {
        val voiceChannelId = event.state.channelId ?: return

        val tempChannelConfig = tempChannelRepository.fetch(
            voiceChannelId = voiceChannelId.value,
            guildId = event.state.guildId.value,
        )

        val guild = event.state.getGuild()

        if (tempChannelConfig != null) {
            println("Config exists")
            val member = guild.getMember(event.state.userId)
            member.addRole(Snowflake(tempChannelConfig.roleId))
            return
        }

        val tempRole = guild.createRole {
            name = "temp-${event.state.channelId}"
            reason = "created by kottotto's TempChannelFeature"
        }
        val voiceChannel = guild.getChannel(voiceChannelId) as? CategorizableChannel ?: return
        println(voiceChannel)
        val tempChannel = guild.createTextChannel(name = "temp-${event.state.channelId}") {
            parentId = voiceChannel.categoryId
        }

        tempChannelRepository.create(
            voiceChannelId = voiceChannelId.value,
            roleId = tempRole.id.value,
            textChannelId = tempChannel.id.value,
            guildId = event.state.guildId.value,
        )

        val member = guild.getMember(event.state.userId)
        member.addRole(tempRole.id)
    }

    private suspend fun handleLeaveEvent(event: VoiceStateUpdateEvent) {
        val voiceChannelId = event.old?.channelId ?: return

        val tempChannelConfig = tempChannelRepository.fetch(
            voiceChannelId = voiceChannelId.value,
            guildId = event.state.guildId.value,
        ) ?: return

        val guild = event.state.getGuild()
        val voiceChannel = guild.getChannel(voiceChannelId)
        if (voiceChannel.data.memberCount.value == 0) {
            println(voiceChannel)
            // TODO: error handling
            guild.getChannelOrNull(Snowflake(tempChannelConfig.channelId))?.delete()
            guild.getRoleOrNull(Snowflake(tempChannelConfig.roleId))?.delete()
            tempChannelRepository.delete(
                voiceChannelId = voiceChannelId.value,
                guildId = event.state.guildId.value,
            )
        } else {
            guild.getMemberOrNull(event.state.userId)
                ?.removeRole(Snowflake(tempChannelConfig.roleId))
        }
    }

    private suspend fun handleMoveEvent(event: VoiceStateUpdateEvent) {
        handleJoinEvent(event)
        handleLeaveEvent(event)
    }
}

internal fun VoiceStateUpdateEvent.isJoinEvent() = old?.channelId == null && state.channelId != null
internal fun VoiceStateUpdateEvent.isMoveEvent() = old?.channelId != null && state.channelId != null
internal fun VoiceStateUpdateEvent.isLeaveEvent() = old?.channelId != null && state.channelId == null
