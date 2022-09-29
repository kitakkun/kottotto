package com.github.kitakkun.kottotto.database

data class ReadChannelConfigData(
    val guildId: Long,
    val textChannelId: Long,
    val ownerId: Long,
    val voiceChannelId: Long
)
