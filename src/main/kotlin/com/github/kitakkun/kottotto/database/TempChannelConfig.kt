package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.ResultRow

data class TempChannelConfig(
    val voiceChannelId: Long,
    val roleId: Long,
    val channelId: Long,
    val guildId: Long,
) {
    companion object {
        fun convert(resultRow: ResultRow): TempChannelConfig =
            TempChannelConfig(
                voiceChannelId = resultRow[TempChannelTable.voiceChannelId],
                roleId = resultRow[TempChannelTable.roleId],
                channelId = resultRow[TempChannelTable.textChannelId],
                guildId = resultRow[TempChannelTable.guildId]
            )
    }
}
