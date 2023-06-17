package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ReadChannelTable : Table() {
    val textChannelId: Column<Long> = long("bindChannelId")
    val ownerId: Column<Long> = long("tempChannelId")
    val voiceChannelId: Column<Long> = long("tempRoleId")
    val guildId: Column<Long> = long("guildId")

    override val primaryKey = PrimaryKey(guildId)
}
