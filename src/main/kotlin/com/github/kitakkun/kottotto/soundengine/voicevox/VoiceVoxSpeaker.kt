package com.github.kitakkun.kottotto.soundengine.voicevox

import com.google.gson.annotations.SerializedName

data class VoiceVoxSpeaker(
    @SerializedName("supported_features")
    val supportedFeatures: SupportedFeatures,
    val name: String,
    @SerializedName("speaker_uuid")
    val speakerUuid: String,
    val styles: List<VoiceVoxVoiceStyle>,
    val version: String,
)


data class VoiceVoxVoiceStyle(
    val name: String,
    val id: Int,
)

data class SupportedFeatures(
    @SerializedName("permitted_synthesis_morphing")
    val permittedSynthesisMorphing: String,
)
