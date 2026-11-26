package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Random

data class DailyExamItem(
    val category: String,
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val explanationBn: String,
    val speakText: String = ""
)

object DailyChallengeGenerator {
    fun generateForDay(epochDay: Long): List<DailyExamItem> {
        val rng = Random(epochDay * 31337L + 7L)
        val out = mutableListOf<DailyExamItem>()

        // 1. 4 Grammar Quiz items
        val grammarPool = GrammarQuizBank.curated.shuffled(rng)
        grammarPool.take(4).forEach { q ->
            out.add(
                DailyExamItem(
                    category = q.type.label,
                    prompt = q.prompt,
                    options = q.options.shuffled(rng),
                    answer = q.answer,
                    explanation = q.explanation,
                    explanationBn = q.explanationBn,
                    speakText = q.audioText
                )
            )
        }

        // 2. 3 Tense Mastery items
        val tensePool = TenseMasteryData.all.flatMap { it.drillQuestions }.shuffled(rng)
        tensePool.take(3).forEach { tq ->
            out.add(
                DailyExamItem(
                    category = "Tense Mastery · টেন্স",
                    prompt = tq.prompt,
                    options = tq.options.shuffled(rng),
                    answer = tq.answer,
                    explanation = tq.explanation,
                    explanationBn = tq.explanationBn
                )
            )
        }

        // 3. 2 Irregular Verb items
        val irregularVerbs = Verbs.all.filter { it.irregular && it.v2.isNotBlank() && it.v3.isNotBlank() && it.v2 != it.base }
        if (irregularVerbs.size >= 4) {
            val pickedVerbs = irregularVerbs.shuffled(rng).take(2)
            pickedVerbs.forEachIndexed { idx, v ->
                val askV3 = idx == 1
                val correct = if (askV3) v.v3 else v.v2
                val distractors = listOf(v.base + "ed", v.v4, if (askV3) v.v2 else v.v3, v.base + "s")
                    .filter { it != correct && it.isNotBlank() }
                    .distinct()
                    .take(3)
                if (distractors.size == 3) {
                    val meaningBn = Verbs.meaning(v, "bn")
                    out.add(
                        DailyExamItem(
                            category = "Verb Master · ক্রিয়া",
                            prompt = if (askV3) "Past Participle (V3) of \"${v.base}\" ($meaningBn):" else "Past Simple (V2) of \"${v.base}\" ($meaningBn):",
                            options = (distractors + correct).shuffled(rng),
                            answer = correct,
                            explanation = "${v.base} (V1) → ${v.v2} (V2) → ${v.v3} (V3) → ${v.v4} (V4).",
                            explanationBn = "ক্রিয়া '$meaningBn': ${v.base} (V1) → ${v.v2} (V2) → ${v.v3} (V3) → ${v.v4} (V4)।",
                            speakText = "${v.base}, ${v.v2}, ${v.v3}"
                        )
                    )
                }
            }
        }

        // 4. Fill up to 10 items
        val vocabFallback = listOf(
            DailyExamItem("Vocabulary · শব্দভান্ডার", "Choose the best synonym for \"abundant\" (প্রচুর):", listOf("plentiful", "scarce", "fragile", "silent"), "plentiful", "'Abundant' means existing in large quantities (plentiful).", "'Abundant' এবং 'plentiful' উভয়ের অর্থ প্রচুর বা পর্যাপ্ত।", "abundant"),
            DailyExamItem("Vocabulary · শব্দভান্ডার", "Choose the opposite (antonym) of \"reluctant\" (অনিচ্ছুক):", listOf("eager", "hesitant", "cautious", "uncertain"), "eager", "'Reluctant' means unwilling; its opposite is 'eager'.", "'Reluctant' (অনিচ্ছুক)-এর বিপরীত শব্দ হলো 'eager' (আগ্রহী)।", "reluctant"),
            DailyExamItem("Vocabulary · শব্দভান্ডার", "What does \"meticulous\" (অতি সতর্ক/নিখুঁত) mean?", listOf("Very careful and precise", "Quick and careless", "Loud and energetic", "Extremely simple"), "Very careful and precise", "A meticulous person pays great attention to every small detail.", "'Meticulous' মানে প্রতিটি খুঁটিনাটি বিষয়ে অত্যন্ত সতর্ক ও নিখুঁত।", "meticulous")
        ).shuffled(rng)

        var fbIdx = 0
        while (out.size < 10 && fbIdx < vocabFallback.size) {
            out.add(vocabFallback[fbIdx++])
        }
        var extraIdx = 4
        while (out.size < 10 && extraIdx < grammarPool.size) {
            val q = grammarPool[extraIdx++]
            out.add(DailyExamItem(q.type.label, q.prompt, q.options.shuffled(rng), q.answer, q.explanation, q.explanationBn, q.audioText))
        }
        return out.take(10)
    }
}

@Composable
fun DailyChallengeScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val today = remember { LocalDate.now() }
    val dateLabel = remember(today) { today.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")) }
    val epochDay = remember { store.todayEpochDay() }
    val questions = remember(epochDay) { DailyChallengeGenerator.generateForDay(epochDay) }

    var started by remember { mutableStateOf(false) }
    var idx by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(listOf<Triple<DailyExamItem, String, Boolean>>()) }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("daily_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column {
                Text("Daily Challenge · দৈনিক পরীক্ষা", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (!started) {
                val doneToday = store.isDailyChallengeDoneToday()
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (doneToday) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = if (doneToday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (doneToday) "Today's Exam Completed! · আজকের পরীক্ষা সম্পন্ন!" else "Today's Fresh 10-Question Exam · আজকের নতুন ১০ প্রশ্নের পরীক্ষা",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (doneToday) "Last score · সর্বশেষ স্কোর: ${store.lastDailyScore}% · মোট সম্পন্ন: ${store.completedDailyDays.size}"
                            else "Fresh daily mix of Grammar, 12 Tenses, Verbs & Vocabulary.\nপ্রতিদিন গ্রামার, ১২ টেন্স, ক্রিয়া ও শব্দভান্ডারের নতুন পরীক্ষা।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                started = true; idx = 0; score = 0; picked = null; history = emptyList(); submitted = false
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("daily_start_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (doneToday) "Retake Exam · আবার দিন (+15 XP)" else "Start Daily Exam · শুরু করুন (+50 XP)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DailyStatMini("🔥", "${store.streak}", "Streak · স্ট্রিক", Modifier.weight(1f))
                    DailyStatMini("🏆", "${store.completedDailyDays.size}", "Exams · পরীক্ষা", Modifier.weight(1f))
                    DailyStatMini("🎯", "${store.accuracyPercent()}%", "Accuracy · নির্ভুলতা", Modifier.weight(1f))
                }
                return@Column
            }

            if (idx >= questions.size) {
                if (!submitted) {
                    submitted = true
                    store.completeDailyChallenge(score, questions.size)
                }
                val pct = (score * 100) / questions.size.coerceAtLeast(1)
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Daily Challenge Complete! · আজকের পরীক্ষা শেষ!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(6.dp))
                        Text("$score / ${questions.size} ($pct%)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Come back tomorrow · আগামীকাল আবার নতুন প্রশ্ন পাবেন।", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Answer Review · উত্তর পর্যালোচনা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                history.forEachIndexed { i, (q, ans, ok) ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ok) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${i + 1}. ${q.prompt}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (ok) "✓ Your answer · আপনার উত্তর: $ans" else "✗ Yours: $ans  ·  Correct · সঠিক: ${q.answer}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(q.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (q.explanationBn.isNotBlank()) {
                                Text(q.explanationBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { started = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Done · সম্পন্ন") }
                Spacer(Modifier.height(24.dp))
                return@Column
            }

            val q = questions[idx]
            LinearProgressIndicator(
                progress = { (idx + 1f) / questions.size },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${idx + 1} / ${questions.size} · প্রশ্ন", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(q.category, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(q.prompt, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))

            val revealed = picked != null
            q.options.forEach { opt ->
                val isRight = opt == q.answer
                val bg = when {
                    !revealed -> MaterialTheme.colorScheme.surfaceVariant
                    isRight -> MaterialTheme.colorScheme.tertiaryContainer
                    opt == picked -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !revealed) {
                            picked = opt
                            if (isRight) score += 1
                            history = history + Triple(q, opt, isRight)
                            store.recordAnswer(isRight, xpOnCorrect = 6)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bg)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(opt, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        if (revealed && isRight) Text("✓", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                        else if (revealed && opt == picked) Text("✗", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (revealed) {
                Spacer(Modifier.height(10.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                            if (q.explanationBn.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(q.explanationBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (q.speakText.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = { speakIn(store, speaker, q.speakText, "en") },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { idx += 1; picked = null },
                    modifier = Modifier.fillMaxWidth().testTag("daily_next_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (idx < questions.lastIndex) "Next Question · পরবর্তী প্রশ্ন" else "Finish Exam · পরীক্ষা শেষ করুন")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DailyStatMini(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji)
            Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
