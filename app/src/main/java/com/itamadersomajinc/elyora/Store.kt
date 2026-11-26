package com.itamadersomajinc.elyora

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class Card(
    var interval: Int = 0,
    var ease: Double = 2.5,
    var reps: Int = 0,
    var due: Long = 0L,
    var lapses: Int = 0
)

class Store(private val ctx: Context) {

    var xp by mutableStateOf(0)
        private set
    var streak by mutableStateOf(0)
        private set
    var bestStreak by mutableStateOf(0)
        private set
    var lastDay by mutableStateOf(0L)
        private set
    var learnedKana by mutableStateOf(setOf<String>())
        private set
    var completedLessons by mutableStateOf(setOf<String>())
        private set
    var srs by mutableStateOf(mapOf<String, Card>())
        private set

    // Expanded progress & learning statistics
    var completedGrammar by mutableStateOf(setOf<String>())
        private set
    var masteredTenses by mutableStateOf(setOf<String>())
        private set
    var totalAnswered by mutableStateOf(0)
        private set
    var totalCorrect by mutableStateOf(0)
        private set
    var quizzesCompleted by mutableStateOf(0)
        private set
    var sentencesBuilt by mutableStateOf(0)
        private set
    var speakingCompleted by mutableStateOf(0)
        private set
    var listeningCompleted by mutableStateOf(0)
        private set
    var completedWritings by mutableStateOf(setOf<String>())
        private set
    var savedWritings by mutableStateOf(mapOf<String, String>())
        private set
    var completedDailyDays by mutableStateOf(setOf<Long>())
        private set
    var lastDailyScore by mutableStateOf(0)
        private set
    var dailyXpHistory by mutableStateOf(mapOf<Long, Int>())
        private set

    var nativeLang by mutableStateOf("bn")
    var target by mutableStateOf("en")
    var onboarded by mutableStateOf(false)
    var showRomaji by mutableStateOf(true)
    var showTranslations by mutableStateOf(true)
    var speechRate by mutableStateOf(0.85f)
    var dailyNewLimit by mutableStateOf(20)
    var dismissedUpdate by mutableStateOf("")
    var aiAssistantEnabled by mutableStateOf(true)

    private val file: File = File(ctx.filesDir, "elyora.json")

    fun load() {
        val source = if (file.exists()) file else File(ctx.filesDir, "kotoba.json")
        if (!source.exists()) return
        try {
            val o = JSONObject(source.readText())
            xp = o.optInt("xp", 0)
            streak = o.optInt("streak", 0)
            bestStreak = o.optInt("best", 0)
            lastDay = o.optLong("lastDay", 0)
            val legacy = o.optString("direction", "ENGLISH")
            target = o.optString("target", "").ifEmpty { if (legacy == "JAPANESE") "ja" else "en" }
            nativeLang = o.optString("native", "bn")
            onboarded = o.optBoolean("onboarded", false)
            showRomaji = o.optBoolean("showRomaji", true)
            showTranslations = o.optBoolean("showTranslations", true)
            speechRate = o.optDouble("rate", 0.85).toFloat()
            dismissedUpdate = o.optString("dismissedUpdate", "")
            aiAssistantEnabled = o.optBoolean("aiAssistantEnabled", true)
            learnedKana = strSet(o, "kana")
            completedLessons = strSet(o, "lessons")
            completedGrammar = strSet(o, "grammar")
            masteredTenses = strSet(o, "tenses")
            completedWritings = strSet(o, "writingsDone")
            totalAnswered = o.optInt("totalAnswered", 0)
            totalCorrect = o.optInt("totalCorrect", 0)
            quizzesCompleted = o.optInt("quizzesCompleted", 0)
            sentencesBuilt = o.optInt("sentencesBuilt", 0)
            speakingCompleted = o.optInt("speakingCompleted", 0)
            listeningCompleted = o.optInt("listeningCompleted", 0)
            lastDailyScore = o.optInt("lastDailyScore", 0)

            val daysArr = o.optJSONArray("dailyDays") ?: JSONArray()
            val dSet = mutableSetOf<Long>()
            for (i in 0 until daysArr.length()) dSet.add(daysArr.getLong(i))
            completedDailyDays = dSet

            val xpObj = o.optJSONObject("dailyXp")
            if (xpObj != null) {
                val map = mutableMapOf<Long, Int>()
                val keys = xpObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    k.toLongOrNull()?.let { day -> map[day] = xpObj.optInt(k, 0) }
                }
                dailyXpHistory = map
            }

            val wObj = o.optJSONObject("savedWritings")
            if (wObj != null) {
                val map = mutableMapOf<String, String>()
                val keys = wObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = wObj.optString(k, "")
                }
                savedWritings = map
            }

            val arr = o.optJSONArray("srs") ?: JSONArray()
            val m = mutableMapOf<String, Card>()
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                m[c.getString("id")] = Card(
                    interval = c.optInt("i", 0),
                    ease = c.optDouble("e", 2.5),
                    reps = c.optInt("r", 0),
                    due = c.optLong("d", 0),
                    lapses = c.optInt("l", 0)
                )
            }
            srs = m
        } catch (_: Exception) {
        }
    }

    private fun strSet(o: JSONObject, k: String): Set<String> {
        val out = mutableSetOf<String>()
        val a = o.optJSONArray(k) ?: JSONArray()
        for (i in 0 until a.length()) out.add(a.getString(i))
        return out
    }

    fun save() {
        try {
            val o = JSONObject()
            o.put("xp", xp)
            o.put("streak", streak)
            o.put("best", bestStreak)
            o.put("lastDay", lastDay)
            o.put("target", target)
            o.put("native", nativeLang)
            o.put("onboarded", onboarded)
            o.put("showRomaji", showRomaji)
            o.put("showTranslations", showTranslations)
            o.put("rate", speechRate.toDouble())
            o.put("dismissedUpdate", dismissedUpdate)
            o.put("aiAssistantEnabled", aiAssistantEnabled)
            o.put("kana", JSONArray(learnedKana.toList()))
            o.put("lessons", JSONArray(completedLessons.toList()))
            o.put("grammar", JSONArray(completedGrammar.toList()))
            o.put("tenses", JSONArray(masteredTenses.toList()))
            o.put("writingsDone", JSONArray(completedWritings.toList()))
            o.put("totalAnswered", totalAnswered)
            o.put("totalCorrect", totalCorrect)
            o.put("quizzesCompleted", quizzesCompleted)
            o.put("sentencesBuilt", sentencesBuilt)
            o.put("speakingCompleted", speakingCompleted)
            o.put("listeningCompleted", listeningCompleted)
            o.put("lastDailyScore", lastDailyScore)
            o.put("dailyDays", JSONArray(completedDailyDays.toList()))

            val xpObj = JSONObject()
            dailyXpHistory.entries.sortedByDescending { it.key }.take(14).forEach { (k, v) ->
                xpObj.put(k.toString(), v)
            }
            o.put("dailyXp", xpObj)

            val wObj = JSONObject()
            for ((k, v) in savedWritings) wObj.put(k, v)
            o.put("savedWritings", wObj)

            val arr = JSONArray()
            for ((id, c) in srs) {
                arr.put(
                    JSONObject()
                        .put("id", id).put("i", c.interval).put("e", c.ease)
                        .put("r", c.reps).put("d", c.due).put("l", c.lapses)
                )
            }
            o.put("srs", arr)
            file.writeText(o.toString())
        } catch (_: Exception) {
        }
    }

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun isDailyChallengeDoneToday(): Boolean = todayEpochDay() in completedDailyDays

    fun recordPractice() {
        val t = todayEpochDay()
        if (lastDay == t) return
        streak = if (lastDay == t - 1) streak + 1 else 1
        bestStreak = max(bestStreak, streak)
        lastDay = t
        save()
    }

    fun addXp(n: Int) {
        if (n <= 0) return
        xp += n
        val t = todayEpochDay()
        val updated = dailyXpHistory.toMutableMap()
        updated[t] = (updated[t] ?: 0) + n
        dailyXpHistory = updated
        recordPractice()
        save()
    }

    fun recordAnswer(correct: Boolean, xpOnCorrect: Int = 5) {
        totalAnswered += 1
        if (correct) {
            totalCorrect += 1
            addXp(xpOnCorrect)
        } else {
            recordPractice()
            save()
        }
    }

    fun completeGrammarLesson(id: String) {
        if (id !in completedGrammar) {
            completedGrammar = completedGrammar + id
            addXp(15)
        } else {
            recordPractice()
            save()
        }
    }

    fun markTenseMastered(id: String) {
        if (id !in masteredTenses) {
            masteredTenses = masteredTenses + id
            addXp(25)
        } else {
            addXp(10)
        }
    }

    fun recordQuizFinished(score: Int, total: Int) {
        quizzesCompleted += 1
        val bonus = if (total > 0 && score == total) 20 else 10
        addXp(bonus)
    }

    fun recordSentenceBuilt(correct: Boolean) {
        if (correct) sentencesBuilt += 1
        recordAnswer(correct, xpOnCorrect = 8)
    }

    fun recordSpeakingCompleted(accuracy: Int) {
        speakingCompleted += 1
        recordAnswer(accuracy >= 65, xpOnCorrect = 12)
    }

    fun recordListeningCompleted(correct: Boolean) {
        listeningCompleted += 1
        recordAnswer(correct, xpOnCorrect = 10)
    }

    fun saveWriting(promptId: String, text: String, markDone: Boolean) {
        val updated = savedWritings.toMutableMap()
        updated[promptId] = text
        savedWritings = updated
        if (markDone && promptId !in completedWritings) {
            completedWritings = completedWritings + promptId
            addXp(30)
        } else {
            recordPractice()
            save()
        }
    }

    fun completeDailyChallenge(score: Int, total: Int) {
        val today = todayEpochDay()
        val firstTimeToday = today !in completedDailyDays
        completedDailyDays = completedDailyDays + today
        lastDailyScore = if (total > 0) (score * 100) / total else 0
        val bonus = if (firstTimeToday) 50 + score * 5 else 15
        addXp(bonus)
    }

    fun accuracyPercent(): Int =
        if (totalAnswered == 0) 0 else ((totalCorrect * 100.0) / totalAnswered).toInt().coerceIn(0, 100)

    fun levelTitle(): String = when (level()) {
        1 -> "Starter"
        2 -> "Explorer"
        3 -> "Builder"
        4 -> "Communicator"
        5 -> "Scholar"
        in 6..9 -> "Fluency Seeker"
        else -> "Grammar Master"
    }

    fun last7DaysXp(): List<Pair<String, Int>> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val label = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(2)
            label to (dailyXpHistory[d.toEpochDay()] ?: 0)
        }
    }

    fun toggleKana(id: String) {
        learnedKana = if (id in learnedKana) learnedKana - id else learnedKana + id
        save()
    }

    fun completeLesson(id: String, wordIds: List<String>) {
        completedLessons = completedLessons + id
        val now = System.currentTimeMillis()
        val m = srs.toMutableMap()
        for (w in wordIds) {
            if (m[w] == null) m[w] = Card(due = now)
        }
        srs = m
        addXp(wordIds.size * 5)
        recordPractice()
        save()
    }

    fun addNewCards(wordIds: List<String>) {
        val now = System.currentTimeMillis()
        val m = srs.toMutableMap()
        for (w in wordIds) if (m[w] == null) m[w] = Card(due = now)
        srs = m
        save()
    }

    fun toggleCard(id: String) {
        val m = srs.toMutableMap()
        if (m[id] == null) m[id] = Card(due = System.currentTimeMillis()) else m.remove(id)
        srs = m
        save()
    }

    fun dueCount(): Int = srs.values.count { it.due <= System.currentTimeMillis() }

    fun dueCards(limit: Int = 25): List<Pair<String, Card>> =
        srs.filter { it.value.due <= System.currentTimeMillis() }
            .toList()
            .sortedBy { it.second.due }
            .take(limit)

    fun grade(id: String, q: Int) {
        val c = srs[id] ?: return
        val now = System.currentTimeMillis()
        when (q) {
            0 -> {
                c.reps = 0
                c.interval = 0
                c.lapses += 1
                c.ease = max(1.3, c.ease - 0.2)
                c.due = now + 10 * 60 * 1000L
            }
            1 -> {
                c.reps += 1
                c.interval = if (c.reps <= 1) 1 else max(1, (c.interval * 1.2).toInt())
                c.ease = max(1.3, c.ease - 0.15)
                c.due = now + c.interval * 86_400_000L
            }
            2 -> {
                c.reps += 1
                c.interval = if (c.reps == 1) 1 else if (c.reps == 2) 6 else (c.interval * c.ease).toInt()
                c.due = now + max(1, c.interval) * 86_400_000L
            }
            else -> {
                c.reps += 1
                c.interval = if (c.reps == 1) 2 else if (c.reps == 2) 10 else (c.interval * c.ease * 1.3).toInt()
                c.ease = min(3.5, c.ease + 0.15)
                c.due = now + max(1, c.interval) * 86_400_000L
            }
        }
        recordAnswer(correct = q >= 2, xpOnCorrect = if (q == 0) 1 else 5)
    }

    fun wordsLearnedCount(): Int = srs.size

    fun level(): Int = xp / 250 + 1

    fun levelProgress(): Float = (xp % 250) / 250f

    fun kanaProgress(): Int =
        if (KanaData.all.isEmpty()) 0 else learnedKana.count { it in KanaData.allIds } * 100 / KanaData.all.size

    fun kanjiProgress(): Int =
        if (KanjiData.all.isEmpty()) 0 else learnedKana.count { it in KanjiData.allIds } * 100 / KanjiData.all.size

    fun setNative(lang: String) {
        nativeLang = lang
        save()
    }

    fun setTargetLang(lang: String) {
        target = lang
        save()
    }

    fun setOnboarded() {
        onboarded = true
        save()
    }

    fun dismissUpdate(version: String) {
        dismissedUpdate = version
        save()
    }

    fun setAiEnabled(enabled: Boolean) {
        aiAssistantEnabled = enabled
        save()
    }

    fun finishOnboarding(native: String, targetLang: String) {
        nativeLang = native
        target = targetLang
        onboarded = true
        save()
    }

    fun resetAll() {
        xp = 0
        streak = 0
        bestStreak = 0
        lastDay = 0
        learnedKana = emptySet()
        completedLessons = emptySet()
        completedGrammar = emptySet()
        masteredTenses = emptySet()
        totalAnswered = 0
        totalCorrect = 0
        quizzesCompleted = 0
        sentencesBuilt = 0
        speakingCompleted = 0
        listeningCompleted = 0
        completedWritings = emptySet()
        savedWritings = emptyMap()
        completedDailyDays = emptySet()
        lastDailyScore = 0
        dailyXpHistory = emptyMap()
        srs = emptyMap()
        onboarded = false
        file.delete()
    }
}
