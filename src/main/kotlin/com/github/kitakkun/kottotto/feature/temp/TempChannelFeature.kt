package com.github.kitakkun.kottotto.feature.temp

import com.github.kitakkun.kottotto.extensions.getCategoryByChannelId
import com.github.kitakkun.kottotto.feature.Feature
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

class TempChannelFeature(
    private val tempChannelRepository: TempChannelRepository,
) : Feature, ListenerAdapter(), CoroutineScope {
    override val coroutineContext = Job() + Dispatchers.IO

    override fun register(jda: JDA) {
        jda.addEventListener(this)
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        launch {
            event.channelJoined?.let {
                handleJoinEvent(
                    channelJoined = it.asVoiceChannel(),
                    guild = event.guild,
                    member = event.member,
                )
            }
            event.channelLeft?.let {
                handleLeaveEvent(
                    channelLeft = it.asVoiceChannel(),
                    guild = event.guild,
                    member = event.member,
                )
            }
        }
    }

    private suspend fun handleJoinEvent(
        guild: Guild,
        member: Member,
        channelJoined: AudioChannel,
    ) {
        val tempChannelConfig = tempChannelRepository.fetch(
            voiceChannelId = channelJoined.idLong,
            guildId = guild.idLong,
        )

        if (tempChannelConfig != null) {
            val role = guild.getRoleById(tempChannelConfig.roleId) ?: return
            guild.addRoleToMember(member, role).queue()
            return
        }

        val (tempRole, tempChannel) = guild.createPrivateTextChannel(
            parentCategory = guild.getCategoryByChannelId(channelJoined.idLong),
            roleName = "temp-${channelJoined.idLong}",
            channelName = "temp-${channelJoined.idLong}",
        )

        tempChannelRepository.create(
            voiceChannelId = channelJoined.idLong,
            roleId = tempRole.idLong,
            textChannelId = tempChannel.idLong,
            guildId = guild.idLong,
        )

        guild.addRoleToMember(member, tempRole).queue()
    }

    private suspend fun Guild.createPrivateTextChannel(
        parentCategory: Category?,
        roleName: String,
        channelName: String,
    ): Pair<Role, Channel> {
        val tempRole = createRole()
            .setName(roleName)
            .await()
        val tempChannel = createTextChannel(channelName)
            .addRolePermissionOverride(tempRole.idLong, EnumSet.of(Permission.VIEW_CHANNEL), null)
            .addRolePermissionOverride(publicRole.idLong, null, EnumSet.of(Permission.VIEW_CHANNEL))
            .setParent(parentCategory)
            .await()
        return Pair(tempRole, tempChannel)
    }

    private fun handleLeaveEvent(
        guild: Guild,
        member: Member,
        channelLeft: AudioChannel,
    ) {
        val tempChannelConfig = tempChannelRepository.fetch(
            voiceChannelId = channelLeft.idLong,
            guildId = guild.idLong,
        ) ?: return

        if (channelLeft.members.size == 0) {
            // TODO: error handling
            guild.getGuildChannelById(tempChannelConfig.channelId)?.delete()?.queue()
            guild.getRoleById(tempChannelConfig.roleId)?.delete()?.queue()
            tempChannelRepository.delete(
                voiceChannelId = channelLeft.idLong,
                guildId = guild.idLong,
            )
        } else {
            guild.removeRoleFromMember(
                member,
                guild.getRoleById(tempChannelConfig.roleId) ?: return
            ).queue()
        }
    }
}
