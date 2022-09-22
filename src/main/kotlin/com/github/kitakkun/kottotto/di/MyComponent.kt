package com.github.kitakkun.kottotto.di

import com.github.kitakkun.kottotto.BotApplication
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MyModules::class])
interface MyComponent {
    fun inject(application: BotApplication)
}
