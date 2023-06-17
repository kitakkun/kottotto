package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.ResultRow

data class ReadChannelConfigData(
    val guildId: Long,
    val textChannelId: Long,
    val ownerId: Long,
    val voiceChannelId: Long,
) {
    companion object {
        fun convert(resultRow: ResultRow): ReadChannelConfigData =
            ReadChannelConfigData(
                guildId = resultRow[ReadChannel.guildId],
                textChannelId = resultRow[ReadChannel.textChannelId],
                ownerId = resultRow[ReadChannel.ownerId],
                voiceChannelId = resultRow[ReadChannel.voiceChannelId]
            )
    }
}
