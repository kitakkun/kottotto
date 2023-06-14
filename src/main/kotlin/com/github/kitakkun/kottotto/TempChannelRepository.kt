package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.database.TempChannelConfigData
import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import javax.inject.Inject

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
        voiceChannelId: ULong,
        guildId: ULong,
    ): TempChannelConfigData? = transaction {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.select {
            tempChannelTable.voiceChannelId eq voiceChannelId
            tempChannelTable.guildId eq guildId
        }.firstOrNull()?.let { TempChannelConfigData.convert(it) }
    }

    fun create(
        voiceChannelId: ULong,
        roleId: ULong,
        textChannelId: ULong,
        guildId: ULong,
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
        voiceChannelId: ULong,
        guildId: ULong,
    ): Unit = transaction {
        addLogger(Slf4jSqlDebugLogger)
        tempChannelTable.deleteWhere {
            tempChannelTable.voiceChannelId eq voiceChannelId
            tempChannelTable.guildId eq guildId
        }
    }
}