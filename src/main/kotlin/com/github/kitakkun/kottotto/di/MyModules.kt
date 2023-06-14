package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.BotClient
import com.github.kitakkun.kottotto.TempChannelRepository
import com.github.kitakkun.kottotto.database.TempChannel
import com.github.kitakkun.kottotto.event.EventStore
import com.github.kitakkun.kottotto.event.EventStoreImpl
import com.github.kitakkun.kottotto.eventmanager.ReadChannelEventManager
import com.github.kitakkun.kottotto.eventmanager.TempChannelManager
import com.github.kitakkun.kottotto.feature.TempChannelFeature
import dagger.Module
import dagger.Provides
import java.util.*
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
    fun provideTempChannelManager(eventStore: EventStore, resourceBundle: ResourceBundle): TempChannelManager =
        TempChannelManager(eventStore, resourceBundle)

    @Provides
    @Singleton
    fun provideReadChannelEventManager(eventStore: EventStore, resourceBundle: ResourceBundle): ReadChannelEventManager =
        ReadChannelEventManager(eventStore, resourceBundle)

    @Provides
    @Singleton
    fun provideResourceBundle(): ResourceBundle = ResourceBundle.getBundle("strings")

}
