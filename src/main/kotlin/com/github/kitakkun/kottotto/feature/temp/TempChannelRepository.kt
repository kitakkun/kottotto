package com.github.kitakkun.kottotto.feature.temp

import com.github.kitakkun.kottotto.database.TempChannelConfig
import com.github.kitakkun.kottotto.database.TempChannelTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TempChannelRepository(
    private val database: Database,
    private val tempChannelTable: TempChannelTable,
) {
    init {
        transaction(database) {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.create(tempChannelTable)
        }
    }

    fun fetch(
        voiceChannelId: Long,
        guildId: Long,
    ): TempChannelConfig? = transaction(database) {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.select {
            tempChannelTable.voiceChannelId eq voiceChannelId
            tempChannelTable.guildId eq guildId
        }.firstOrNull()?.let { TempChannelConfig.convert(it) }
    }

    fun create(
        voiceChannelId: Long,
        roleId: Long,
        textChannelId: Long,
        guildId: Long,
    ): Unit = transaction(database) {
        addLogger(Slf4jSqlDebugLogger)
        TempChannelTable.insert {
            it[tempChannelTable.voiceChannelId] = voiceChannelId
            it[tempChannelTable.roleId] = roleId
            it[tempChannelTable.textChannelId] = textChannelId
            it[tempChannelTable.guildId] = guildId
        }
    }

    fun delete(
        voiceChannelId: Long,
        guildId: Long,
    ): Unit = transaction(database) {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.deleteWhere {
            tempChannelTable.voiceChannelId eq voiceChannelId and
                    (tempChannelTable.guildId eq guildId)
        }
    }
}