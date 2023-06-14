package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.ResultRow

data class TempChannelConfigData(
    val voiceChannelId: ULong,
    val roleId: ULong,
    val channelId: ULong,
    val guildId: ULong,
) {
    companion object {
        fun convert(resultRow: ResultRow) : TempChannelConfigData =
            TempChannelConfigData(
                voiceChannelId = resultRow[TempChannel.voiceChannelId],
                roleId = resultRow[TempChannel.roleId],
                channelId = resultRow[TempChannel.textChannelId],
                guildId = resultRow[TempChannel.guildId]
            )
    }
}
