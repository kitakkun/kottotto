package com.github.kitakkun.kottotto

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TestConfiguration {
    private val envFilePath = ".env"

    @Test
    fun testIfConfigurationFileExist() {
        val file = File(envFilePath)
        assertTrue(file.exists())
    }

    @Test
    fun testIfOpenJTalkConfigurationIsValid() {
        assertNotNull(Config.get("OPEN_JTALK"))
        assertNotNull(Config.get("OPEN_JTALK_HTS_VOICE"))
        assertNotNull(Config.get("OPEN_JTALK_DICTIONARY"))
    }

    @Test
    fun testIfGPTConfigurationIsValid() {
        assertNotNull(Config.get("GPT4ALL_MODEL_PATH"))
    }

    @Test
    fun testIfVoiceVoxConfigurationIsValid() {
        assertNotNull(Config.get("VOICEVOX_SERVER_URL"))
    }
}