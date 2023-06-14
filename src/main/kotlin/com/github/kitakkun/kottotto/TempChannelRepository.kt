package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannelConfigData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TempChannelRepository(
    private val tempChannelTable: TempChannel,
) {
    init {
        Config.apply {
            Database.connect(
                url = get("DB_URL"),
                driver = get("DB_DRIVER"),
                user = get("DB_USER"),
                password = get("DB_PASSWORD")
            )
        }
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.create(tempChannelTable)
        }
    }

    fun fetch(
        voiceChannelId: Long,
        guildId: Long,
    ): TempChannelConfigData? = transaction {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.select {
            tempChannelTable.voiceChannelId eq voiceChannelId
            tempChannelTable.guildId eq guildId
        }.firstOrNull()?.let { TempChannelConfigData.convert(it) }
    }

    fun create(
        voiceChannelId: Long,
        roleId: Long,
        textChannelId: Long,
        guildId: Long,
    ): Unit = transaction {
        addLogger(Slf4jSqlDebugLogger)
        TempChannel.insert {
            it[tempChannelTable.voiceChannelId] = voiceChannelId
            it[tempChannelTable.roleId] = roleId
            it[tempChannelTable.textChannelId] = textChannelId
            it[tempChannelTable.guildId] = guildId
        }
    }

    fun delete(
        voiceChannelId: Long,
        guildId: Long,
    ): Unit = transaction {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.deleteWhere {
            tempChannelTable.voiceChannelId eq voiceChannelId and
                    (tempChannelTable.guildId eq guildId)
        }
    }
}