package com.itamadersomajinc.elyora

/**
 * Speak text in the learner's current target language (the "learning" language),
 * or in English when [targetAudio] is false.
 */
fun speak(store: Store, speaker: Speaker, text: String, targetAudio: Boolean) {
    speaker.setRate(store.speechRate)
    speaker.speak(text, if (targetAudio) store.target else "en")
}

/** Speak text in one explicit language, ignoring the current target. */
fun speakIn(store: Store, speaker: Speaker, text: String, lang: String) {
    speaker.setRate(store.speechRate)
    speaker.speak(text, lang)
}

fun localeFor(code: String): String = when (code) {
    "ja" -> "ja-JP"
    "hi" -> "hi-IN"
    "es" -> "es-ES"
    "ar" -> "ar-SA"
    "fr" -> "fr-FR"
    "de" -> "de-DE"
    "bn" -> "bn-IN"
    "ta" -> "ta-IN"
    "te" -> "te-IN"
    "ur" -> "ur-PK"
    "kn" -> "kn-IN"
    else -> "en-US"
}

/**
 * Trim a dictionary gloss down to its first, most common sense, dropping leading
 * sense numbers and everything after the first `;`.
 */
fun cleanGloss(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    var s = raw.substringBefore(';').trim()
    s = s.replace(Regex("^\\(?\\d+[.)]\\s*"), "").trim()
    return s.ifEmpty { null }
}

/**
 * Every word displays English + Bengali (বাংলা), plus the learner's native language
 * if they picked a third language.
 */
fun Word.glossFor(native: String): String {
    if (lang != "ja" && lang != "en") {
        val m = nativeMeaning(en, en, native)
        return appendBangla(cleanGloss(m) ?: m, en)
    }
    val base = when (native) {
        "ja" -> if (kana.isNotEmpty()) kana + (if (romaji.isNotEmpty()) " ($romaji)" else "") else nativeMeaning(en, en, native)
        "hi" -> if (hi.isNotEmpty()) "$en · $hi" else nativeMeaning(en, en, native)
        "bn" -> {
            val bn = banglaGloss(en)
            if (bn.isNotEmpty()) "$en · $bn" else en
        }
        else -> nativeMeaning(en, en, native)
    }
    return appendBangla(base, en)
}

fun Phrase.glossFor(native: String): String {
    val base = when (native) {
        "ja" -> "$ja · $en"
        "hi" -> if (hi.isNotEmpty()) "$en · $hi" else nativeMeaning(en, en, native)
        else -> nativeMeaning(en, en, native)
    }
    return appendBangla(base, en)
}

fun Pattern.ruleFor(native: String): String {
    if (lang != "ja" && lang != "en") {
        val key = ruleEn.replace("\n", "\\n")
        return nativeMeaning(key, key, native).replace("\\n", "\n")
    }
    return when (native) {
        "ja" -> if (ruleJa.isNotEmpty()) ruleJa else ruleEn
        "hi" -> if (ruleHi.isNotEmpty()) ruleHi else ruleEn
        else -> {
            val bn = Gloss.lookupBn(ruleEn)
            if (!bn.isNullOrBlank() && bn != ruleEn) "$ruleEn\n\nবাংলা ব্যাখ্যা:\n$bn"
            else ruleEn
        }
    }
}

fun PatternExample.glossFor(native: String, lang: String): String {
    val english = if (lang == "en") ja else en
    val target = if (lang == "en") en else ja
    if (lang != "ja" && lang != "en") {
        val base = when (native) {
            "en" -> english
            "hi" -> if (hi.isNotEmpty()) "$english · $hi" else nativeMeaning(english, english, native)
            else -> nativeMeaning(english, english, native)
        }
        return appendBangla(base, english)
    }
    val base = when (native) {
        "en" -> if (lang == "en") banglaGloss(english) else appendBangla(english, english)
        "ja" -> if (lang == "ja") banglaGloss(english) else appendBangla(target.ifEmpty { english }, english)
        "hi" -> if (hi.isNotEmpty()) appendBangla(hi, english) else appendBangla(english, english)
        else -> {
            val bn = banglaGloss(english)
            if (lang == "en") bn else if (bn.isNotEmpty()) "$english · $bn" else english
        }
    }
    return base
}

fun Sentence.glossFor(native: String): String {
    val base = when (native) {
        "hi" -> if (hi.isNotEmpty()) "$en · $hi" else nativeMeaning(en, en, native)
        else -> nativeMeaning(en, en, native)
    }
    return appendBangla(base, en)
}

fun AlphabetChar.glossFor(native: String): String {
    val base = when (native) {
        "ja" -> if (kana.isNotEmpty()) "$kana — ${word.lowercase()}" else word
        "hi" -> if (hi.isNotEmpty()) "$hi — ${word.lowercase()}" else nativeMeaning(word, word, native)
        else -> nativeMeaning(word, word, native)
    }
    return appendBangla(base, word)
}

val NATIVE_LANGUAGES = listOf(
    "bn" to "🇧🇩 বাংলা (Bengali)",
    "en" to "🇺🇸 English",
    "hi" to "🇮🇳 Hindi",
    "ja" to "🇯🇵 Japanese",
    "es" to "🇪🇸 Spanish",
    "ar" to "🇸🇦 Arabic",
    "fr" to "🇫🇷 French",
    "de" to "🇩🇪 German",
    "ta" to "🇮🇳 Tamil",
    "te" to "🇮🇳 Telugu",
    "ur" to "🇵🇰 Urdu",
    "kn" to "🇮🇳 Kannada"
)

fun nativeName(code: String): String =
    NATIVE_LANGUAGES.firstOrNull { it.first == code }?.second ?: "English"

fun languageLabel(code: String): String = when (code) {
    "ja" -> "Japanese (জাপানি)"
    "en" -> "English (ইংরেজি)"
    "bn" -> "Bengali (বাংলা)"
    "hi" -> "Hindi (হিন্দি)"
    "es" -> "Spanish (স্প্যানিশ)"
    "ar" -> "Arabic (আরবি)"
    "fr" -> "French (ফরাসি)"
    "de" -> "German (জার্মান)"
    else -> nativeName(code).substringAfter(' ', nativeName(code))
}
