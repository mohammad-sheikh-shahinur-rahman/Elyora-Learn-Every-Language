package com.itamadersomajinc.elyora

import android.annotation.SuppressLint
import android.content.Context

/**
 * Multilingual gloss table with always-on Bengali (বাংলা) support.
 *
 * Three tables are kept in memory:
 *  - [nativeTable]: the learner's selected native language (`lookup`)
 *  - [targetTable]: the language being learned (`lookupTarget`)
 *  - [bnTable]: ALWAYS loaded from `assets/gloss_bn.tsv` so Bengali meanings
 *    appear alongside English and whichever target language the user is studying.
 */
object Gloss {

    @SuppressLint("StaticFieldLeak")
    private var ctx: Context? = null

    @Volatile
    private var nativeLang: String = ""

    @Volatile
    private var nativeTable: Map<String, String> = emptyMap()

    @Volatile
    private var targetLang: String = ""

    @Volatile
    private var targetTable: Map<String, String> = emptyMap()

    @Volatile
    private var bnLoaded: Boolean = false

    @Volatile
    private var bnTable: Map<String, String> = emptyMap()

    fun attach(context: Context) {
        ctx = context.applicationContext
    }

    private fun load(lang: String): Map<String, String> {
        if (lang == "en" || lang.isBlank()) return emptyMap()
        val c = ctx ?: return emptyMap()
        return try {
            c.assets.open("gloss_$lang.tsv").bufferedReader().use { r ->
                val map = HashMap<String, String>(24000)
                r.lineSequence().forEach { line ->
                    if (line.isNotEmpty()) {
                        val i = line.indexOf('\t')
                        if (i > 0) map[line.substring(0, i)] = line.substring(i + 1)
                    }
                }
                map
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /** Always load Bengali glosses so every screen can display বাংলা alongside English & target language. */
    fun ensureBn() {
        if (bnLoaded) return
        synchronized(this) {
            if (bnLoaded) return
            bnTable = load("bn")
            bnLoaded = true
        }
    }

    /** Load the table for the learner's native language. */
    fun ensure(lang: String) {
        ensureBn()
        if (lang == nativeLang) return
        nativeTable = if (lang == "bn") bnTable else load(lang)
        nativeLang = lang
    }

    /** Load the table used to translate English source content into the target language being learned. */
    fun ensureTarget(lang: String) {
        if (lang == targetLang) return
        targetTable = when (lang) {
            "ja", "en" -> emptyMap()
            "bn" -> {
                ensureBn()
                bnTable
            }
            else -> load(lang)
        }
        targetLang = lang
    }

    /** Meaning of an English string in the learner's native language. */
    fun lookup(en: String): String? {
        if (en.isEmpty()) return null
        return nativeTable[en] ?: nativeTable[en.lowercase()]
    }

    /** Always-available Bengali (বাংলা) meaning for an English word or sentence. */
    fun lookupBn(en: String): String? {
        if (en.isBlank()) return null
        if (!bnLoaded) ensureBn()
        val exact = bnTable[en] ?: bnTable[en.trim()]
        if (exact != null) return exact
        val lower = en.trim().lowercase()
        bnTable[lower]?.let { return it }
        val stripped = lower.trimEnd('.', '!', '?', ',')
        bnTable[stripped]?.let { return it }
        return null
    }

    /** Translation of an English string into the language being learned. */
    fun lookupTarget(en: String): String? {
        if (en.isEmpty()) return null
        return targetTable[en]
    }

    fun targetLoaded(): Boolean = targetTable.isNotEmpty()
}

/** Returns true if the text already contains Bengali script characters. */
fun hasBengaliScript(text: String): Boolean = text.any { it.code in 0x0980..0x09FF }

/**
 * Clean Bengali gloss for an English word or sentence (empty string if not found).
 */
fun banglaGloss(en: String): String {
    if (en.isBlank()) return ""
    val raw = Gloss.lookupBn(en) ?: return ""
    return cleanGloss(raw) ?: raw
}

/**
 * Ensures Bengali is displayed alongside the primary English/native gloss whenever available.
 */
fun appendBangla(primary: String, englishSource: String): String {
    if (hasBengaliScript(primary)) return primary
    val bn = banglaGloss(englishSource)
    if (bn.isEmpty()) return primary
    return if (primary.isBlank()) bn else "$primary · $bn"
}

/**
 * Meaning of an English string in the learner's native language, with Bengali
 * paired alongside English so every language track stays bilingual.
 */
fun nativeMeaning(en: String, fallback: String, native: String): String {
    val base = when (native) {
        "en", "" -> en
        "bn" -> {
            val bn = banglaGloss(en)
            if (bn.isNotEmpty() && bn != en) "$en · $bn" else fallback
        }
        else -> {
            val nat = cleanGloss(Gloss.lookup(en)) ?: Gloss.lookup(en) ?: fallback
            appendBangla(nat, en)
        }
    }
    return if (native == "en" || native == "") appendBangla(base, en) else base
}
