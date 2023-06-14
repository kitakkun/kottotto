package com.github.kitakkun.kottotto.feature

import net.dv8tion.jda.api.JDA

interface Feature {
    fun register(jda: JDA)
}