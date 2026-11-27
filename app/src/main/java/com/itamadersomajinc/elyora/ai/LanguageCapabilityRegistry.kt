package com.itamadersomajinc.elyora.ai

enum class EngineSupportLevel {
    VERIFIED_OFFLINE,
    DEVICE_DEPENDENT,
    PARTIAL,
    UNVERIFIED
}

data class LanguageCapability(
    val code: String,
    val displayName: String,
    val offlineGlossAvailable: Boolean,
    val grammarAcademyAvailable: Boolean,
    val asrSupport: EngineSupportLevel,
    val ttsSupport: EngineSupportLevel,
    val scriptRenderingVerified: Boolean,
    val notes: String
)

/**
 * Explicit capability registry so unsupported or device-dependent features
 * (such as Android platform SpeechRecognizer/TTS for non-installed locales)
 * are never advertised as universally verified offline.
 */
object LanguageCapabilityRegistry {

    val capabilities: List<LanguageCapability> = listOf(
        LanguageCapability(
            code = "en",
            displayName = "English",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = true,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = true,
            notes = "Full offline dictionary (25,656+ entries), 13 tenses, 210+ grammar lessons; ASR/TTS depend on device Google/Android speech packs."
        ),
        LanguageCapability(
            code = "bn",
            displayName = "বাংলা (Bengali)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = true,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = true,
            notes = "Bundled Noto Sans Bengali font, full offline gloss_bn.tsv & grammar_academy_bn.tsv; ASR/TTS require device bn-BD/bn-IN pack."
        ),
        LanguageCapability(
            code = "ja",
            displayName = "日本語 (Japanese)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = true,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = true,
            notes = "217,686+ offline dictionary entries, Kana, Kanji & JLPT N5-N1; ASR/TTS require device ja-JP pack."
        ),
        LanguageCapability(
            code = "hi",
            displayName = "हिन्दी (Hindi)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = false,
            notes = "Offline gloss_hi.tsv available; grammar academy & device speech packs vary by OEM."
        ),
        LanguageCapability(
            code = "es",
            displayName = "Español (Spanish)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = false,
            notes = "Offline gloss_es.tsv available; ASR/TTS depend on device es-ES/es-US pack."
        ),
        LanguageCapability(
            code = "ar",
            displayName = "العربية (Arabic)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = false,
            notes = "Offline gloss_ar.tsv available; RTL/ASR/TTS depend on device configuration."
        ),
        LanguageCapability(
            code = "fr",
            displayName = "Français (French)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = false,
            notes = "Offline gloss_fr.tsv available; ASR/TTS depend on device fr-FR pack."
        ),
        LanguageCapability(
            code = "de",
            displayName = "Deutsch (German)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            ttsSupport = EngineSupportLevel.DEVICE_DEPENDENT,
            scriptRenderingVerified = false,
            notes = "Offline gloss_de.tsv available; ASR/TTS depend on device de-DE pack."
        ),
        LanguageCapability(
            code = "ta",
            displayName = "தமிழ் (Tamil)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.UNVERIFIED,
            ttsSupport = EngineSupportLevel.UNVERIFIED,
            scriptRenderingVerified = false,
            notes = "Offline gloss_ta.tsv available; voice ASR/TTS not verified."
        ),
        LanguageCapability(
            code = "te",
            displayName = "తెలుగు (Telugu)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.UNVERIFIED,
            ttsSupport = EngineSupportLevel.UNVERIFIED,
            scriptRenderingVerified = false,
            notes = "Offline gloss_te.tsv available; voice ASR/TTS not verified."
        ),
        LanguageCapability(
            code = "ur",
            displayName = "اردو (Urdu)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.UNVERIFIED,
            ttsSupport = EngineSupportLevel.UNVERIFIED,
            scriptRenderingVerified = false,
            notes = "Offline gloss_ur.tsv available; voice ASR/TTS not verified."
        ),
        LanguageCapability(
            code = "kn",
            displayName = "ಕನ್ನಡ (Kannada)",
            offlineGlossAvailable = true,
            grammarAcademyAvailable = false,
            asrSupport = EngineSupportLevel.UNVERIFIED,
            ttsSupport = EngineSupportLevel.UNVERIFIED,
            scriptRenderingVerified = false,
            notes = "Offline gloss_kn.tsv available; voice ASR/TTS not verified."
        )
    )

    fun forCode(code: String): LanguageCapability =
        capabilities.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: capabilities.first()
}
