package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TempChannelTable : Table() {
    private val id = integer("id").autoIncrement()
    val voiceChannelId: Column<Long> = long("voiceChannelId")
    val textChannelId: Column<Long> = long("textChannelId")
    val roleId: Column<Long> = long("roleId")
    val guildId: Column<Long> = long("guildId")

    override val primaryKey = PrimaryKey(id)
}
