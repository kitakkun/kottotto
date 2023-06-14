package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

@OptIn(ExperimentalUnsignedTypes::class)
object TempChannel : Table() {
    private val id = integer("id").autoIncrement()
    val voiceChannelId: Column<ULong> = ulong("voiceChannelId")
    val textChannelId: Column<ULong> = ulong("textChannelId")
    val roleId: Column<ULong> = ulong("roleId")
    val guildId: Column<ULong> = ulong("guildId")

    override val primaryKey = PrimaryKey(id)
}
