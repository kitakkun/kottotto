package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.BotClient
import com.github.kitakkun.kottotto.event.EventStore
import com.github.kitakkun.kottotto.event.EventStoreImpl
import com.github.kitakkun.kottotto.event.manager.TempChannelManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MyModules {
    @Provides
    @Singleton
    fun provideEventStore(): EventStore = EventStoreImpl()

    @Provides
    @Singleton
    fun provideBotClient(eventStore: EventStore): BotClient = BotClient(eventStore)

    @Provides
    @Singleton
    fun provideTempChannelManager(eventStore: EventStore): TempChannelManager = TempChannelManager(eventStore)
}
