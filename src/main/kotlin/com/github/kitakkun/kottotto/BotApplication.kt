package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.database.ReadChannel
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.di.DaggerMyComponent
import com.github.kitakkun.kottotto.di.MyModules
import com.github.kitakkun.kottotto.event.EventStore
import com.github.kitakkun.kottotto.eventmanager.ReadChannelEventManager
import com.github.kitakkun.kottotto.eventmanager.TempChannelManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

class BotApplication {

    private val component = DaggerMyComponent.builder()
        .myModules(MyModules())
        .build()

    @Inject lateinit var eventStore: EventStore
    @Inject lateinit var botClient: BotClient
    @Inject lateinit var tempChannelManager: TempChannelManager
    @Inject lateinit var readChannelEventManager: ReadChannelEventManager

    init {
        this.component.inject(this)
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
