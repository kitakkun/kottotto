package com.github.kitakkun.kottotto.feature.read

import com.github.kitakkun.kottotto.database.ReadChannelConfigData
import com.github.kitakkun.kottotto.database.ReadChannelTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ReadChannelRepository(
    private val readChannelTable: ReadChannelTable,
    private val database: Database,
) {
    init {
        transaction(database) {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.create(readChannelTable)
        }
    }

    fun create(
        guildId: Long,
        textChannelId: Long,
        ownerId: Long,
        voiceChannelId: Long,
    ) = transaction(database) {
        readChannelTable.insert {
            it[this.guildId] = guildId
            it[this.textChannelId] = textChannelId
            it[this.ownerId] = ownerId
            it[this.voiceChannelId] = voiceChannelId
        }
    }

    fun fetch(guildId: Long): ReadChannelConfigData? = transaction(database) {
        readChannelTable.select {
            readChannelTable.guildId eq guildId
        }.firstOrNull()?.let { ReadChannelConfigData.convert(it) }
    }

    fun delete(guildId: Long) = transaction(database) {
        readChannelTable.deleteWhere {
            readChannelTable.guildId eq guildId
        }
    }
}