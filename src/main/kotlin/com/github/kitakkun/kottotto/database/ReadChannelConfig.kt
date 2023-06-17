package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.ResultRow

data class ReadChannelConfig(
    val textChannelId: Long,
    val ownerId: Long,
    val voiceChannelId: Long,
) {
    companion object {
        fun convert(resultRow: ResultRow): ReadChannelConfig =
            ReadChannelConfig(
                textChannelId = resultRow[ReadChannelTable.textChannelId],
                ownerId = resultRow[ReadChannelTable.ownerId],
                voiceChannelId = resultRow[ReadChannelTable.voiceChannelId]
            )
    }
}
