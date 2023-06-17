package com.github.kitakkun.kottotto.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

class ReadChannelPreferences : Table() {
    val guildId: Column<Long> = long("guildId")
    val readBotMessage: Column<Boolean> = bool("readBotMessage")
    val ignoreSelfMessage: Column<Boolean> = bool("ignoreSelfMessage")


    override val primaryKey = PrimaryKey(guildId)
}