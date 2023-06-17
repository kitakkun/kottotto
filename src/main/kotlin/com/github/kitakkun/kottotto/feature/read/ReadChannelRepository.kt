package com.github.kitakkun.kottotto.feature.read

import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.ReadChannelConfigData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ReadChannelRepository(
    private val readChannelTable: ReadChannel,
) {
    init {
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.create(readChannelTable)
        }
    }

    fun create(
        guildId: Long,
        textChannelId: Long,
        ownerId: Long,
        voiceChannelId: Long,
    ) = transaction {
        readChannelTable.insert {
            it[this.guildId] = guildId
            it[this.textChannelId] = textChannelId
            it[this.ownerId] = ownerId
            it[this.voiceChannelId] = voiceChannelId
        }
    }

    fun fetch(guildId: Long): ReadChannelConfigData? = transaction {
        readChannelTable.select {
            readChannelTable.guildId eq guildId
        }.firstOrNull()?.let { ReadChannelConfigData.convert(it) }
    }

    fun delete(guildId: Long) = transaction {
        readChannelTable.deleteWhere {
            readChannelTable.guildId eq guildId
        }
    }
}