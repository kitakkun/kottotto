package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TempChannel : Table() {
    private val id = integer("id").autoIncrement()
    val bindChannelId: Column<Long> = long("bindChannelId")
    val tempChannelId: Column<Long> = long("tempChannelId")
    val tempRoleId: Column<Long> = long("tempRoleId")
    val guildId: Column<Long> = long("guildId")

    override val primaryKey = PrimaryKey(id)
}
