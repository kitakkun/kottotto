package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.feature.TempChannelFeature
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BotApplication: KoinComponent {

    private val tempChannelFeature: TempChannelFeature by inject()

    init {
        initDatabase()
    }

    private fun initDatabase() {
        // establish database connection
        Config.apply {
            Database.connect(url = get("DB_URL"), driver = get("DB_DRIVER"), user = get("DB_USER"), password = get("DB_PASSWORD"))
        }
        // initialize tables
        transaction {
            SchemaUtils.create(TempChannel, ReadChannel)
        }
    }

}
