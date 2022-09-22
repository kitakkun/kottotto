package com.github.kitakkun.kottotto

import io.github.cdimascio.dotenv.Dotenv

object Config {
    private val dotenv = Dotenv.load()
    fun get(key: String): String = dotenv.get(key)
}
