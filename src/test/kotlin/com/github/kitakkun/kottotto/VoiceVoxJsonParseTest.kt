package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.soundengine.voicevox.VoiceVoxSpeaker
import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VoiceVoxJsonParseTest {
    val jsonInput = """ 
    {
        "supported_features": {
            "permitted_synthesis_morphing": "SELF_ONLY"
        },
        "name": "四国めたん",
        "speaker_uuid": "7ffcb7ce-00ec-4bdc-82cd-45a8889e43ff",
        "styles": [
            {
                "name": "ノーマル",
                "id": 2
            },
            {
                "name": "あまあま",
                "id": 0
            },
            {
                "name": "ツンツン",
                "id": 6
            },
            {
                "name": "セクシー",
                "id": 4
            },
            {
                "name": "ささやき",
                "id": 36
            },
            {
                "name": "ヒソヒソ",
                "id": 37
            }
        ],
        "version": "0.14.4"
    }
    """

    @Test
    fun testIfJsonCanBeParsed() {
        val gson = Gson()
        val speaker = gson.fromJson(jsonInput, VoiceVoxSpeaker::class.java)
        assertEquals("四国めたん", speaker.name)
        assertEquals("7ffcb7ce-00ec-4bdc-82cd-45a8889e43ff", speaker.speakerUuid)
        assertEquals(6, speaker.styles.size)
        assertEquals("ノーマル", speaker.styles[0].name)
        assertEquals(2, speaker.styles[0].id)
        assertEquals("あまあま", speaker.styles[1].name)
        assertEquals(0, speaker.styles[1].id)
        assertEquals("ツンツン", speaker.styles[2].name)
        assertEquals(6, speaker.styles[2].id)
        assertEquals("SELF_ONLY", speaker.supportedFeatures.permittedSynthesisMorphing)
        assertEquals("0.14.4", speaker.version)
    }
}