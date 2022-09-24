package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.ResultRow

data class TempChannelConfigData(
    val bindingChannelId: Long,
    val roleId: Long,
    val channelId: Long,
    val guildId: Long,
) {
    companion object {
        fun convert(resultRow: ResultRow) : TempChannelConfigData =
            TempChannelConfigData(
                bindingChannelId = resultRow[TempChannel.bindChannelId],
                roleId = resultRow[TempChannel.tempRoleId],
                channelId = resultRow[TempChannel.tempChannelId],
                guildId = resultRow[TempChannel.guildId]
            )
    }
}
