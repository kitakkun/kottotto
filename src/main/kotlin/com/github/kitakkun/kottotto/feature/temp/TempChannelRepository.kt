package com.github.kitakkun.kottotto.feature.temp

import com.github.kitakkun.kottotto.Config
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannelConfigData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TempChannelRepository(
    private val tempChannelTable: TempChannel,
) {
    init {
        val dbUrl = Config.get("DB_URL") ?: throw Exception("DB_URL is not set")
        val driver = Config.get("DB_DRIVER") ?: throw Exception("DB_DRIVER is not set")
        val user = Config.get("DB_USER") ?: throw Exception("DB_USER is not set")
        val password = Config.get("DB_PASSWORD") ?: throw Exception("DB_PASSWORD is not set")
        Database.connect(
            url = dbUrl,
            driver = driver,
            user = user,
            password = password,
        )
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