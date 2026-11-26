package com.itamadersomajinc.elyora

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp

data class AcademyLesson(
    val id: String,
    val category: String,
    val level: String,
    val title: String,
    val formula: String,
    val rule: String,
    val tip: String,
    val examples: List<String>,
    val titleBn: String = "",
    val ruleBn: String = "",
    val tipBn: String = "",
    val examplesBn: List<String> = emptyList()
)

private data class AcademyLessonBn(
    val titleBn: String,
    val ruleBn: String,
    val tipBn: String,
    val examplesBn: List<String>
)

object GrammarAcademyData {
    @Volatile
    private var cache: List<AcademyLesson>? = null

    val all: List<AcademyLesson> get() = cache ?: emptyList()

    val categories: List<String>
        get() = listOf("All") + all.map { it.category }.distinct()

    val levels = listOf("All", "A1", "A2", "B1", "B2", "C1")

    fun categoryBn(cat: String): String = when (cat) {
        "All" -> "সব"
        "Parts of Speech" -> "পদ প্রকরণ"
        "Articles & Determiners" -> "আর্টিকেল ও নির্ধারক"
        "Tenses & Aspect" -> "টেন্স ও ক্রিয়ার কাল"
        "Modals & Auxiliaries" -> "মডাল ও সাহায্যকারী ক্রিয়া"
        "Voice & Speech" -> "বাচ্য ও উক্তি"
        "Conditionals & Wishes" -> "শর্তবাক্য ও ইচ্ছা"
        "Prepositions" -> "পদান্বয়ী অব্যয়"
        "Adjectives & Adverbs" -> "বিশেষণ ও ক্রিয়াবিশেষণ"
        "Clauses & Sentences" -> "বাক্যাংশ ও বাক্য গঠন"
        "Gerunds & Infinitives" -> "জেরান্ড ও ইনফিনিটিভ"
        "Questions & Emphasis" -> "প্রশ্ন ও জোর দেওয়া"
        "Conjunctions & Connectors" -> "সংযোজক অব্যয়"
        "Subject-Verb Agreement" -> "কর্তা-ক্রিয়ার সামঞ্জস্য"
        "Phrasal Verbs" -> "ফ্রেজাল ভার্ব"
        "Punctuation & Mechanics" -> "বিরামচিহ্ন ও বানানরীতি"
        "Common Mistakes" -> "সাধারণ ভুল"
        "Academic & Style" -> "একাডেমিক ও লেখার শৈলী"
        "Special Structures" -> "বিশেষ বাক্যগঠন"
        else -> cat
    }

    fun init(context: Context) {
        if (cache != null) return
        synchronized(this) {
            if (cache != null) return
            val bnMap = try {
                context.assets.open("grammar_academy_bn.tsv").bufferedReader().use { r ->
                    r.lineSequence()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val c = line.split("\t")
                            if (c.size < 5) return@mapNotNull null
                            c[0].trim() to AcademyLessonBn(
                                titleBn = c[1].trim(),
                                ruleBn = c[2].trim(),
                                tipBn = c[3].trim(),
                                examplesBn = c[4].split("||").map { it.trim() }.filter { it.isNotEmpty() }
                            )
                        }.toMap()
                }
            } catch (_: Exception) {
                emptyMap()
            }

            cache = try {
                context.assets.open("grammar_academy.tsv").bufferedReader().use { r ->
                    r.lineSequence()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val c = line.split("\t")
                            if (c.size < 8) return@mapNotNull null
                            val id = c[0].trim()
                            val bn = bnMap[id]
                            AcademyLesson(
                                id = id,
                                category = c[1].trim(),
                                level = c[2].trim(),
                                title = c[3].trim(),
                                formula = c[4].trim(),
                                rule = c[5].trim(),
                                tip = c[6].trim(),
                                examples = c[7].split("||").map { it.trim() }.filter { it.isNotEmpty() },
                                titleBn = bn?.titleBn ?: "",
                                ruleBn = bn?.ruleBn ?: "",
                                tipBn = bn?.tipBn ?: "",
                                examplesBn = bn?.examplesBn ?: emptyList()
                            )
                        }.toList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

@Composable
fun GrammarAcademyView(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onOpenQuiz: (() -> Unit)? = null
) {
    val allLessons = remember { GrammarAcademyData.all }
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLevel by remember { mutableStateOf("All") }
    var openLesson by remember { mutableStateOf<AcademyLesson?>(null) }

    if (openLesson != null) {
        AcademyLessonDetail(
            lesson = openLesson!!,
            allLessons = allLessons,
            store = store,
            speaker = speaker,
            modifier = modifier,
            onNavigateLesson = { openLesson = it },
            onClose = { openLesson = null }
        )
        return
    }

    val query = search.trim().lowercase()
    val filtered = remember(allLessons, query, selectedCategory, selectedLevel) {
        allLessons.filter { l ->
            (selectedCategory == "All" || l.category == selectedCategory) &&
                (selectedLevel == "All" || l.level == selectedLevel) &&
                (query.isEmpty() || l.title.lowercase().contains(query) ||
                    l.titleBn.lowercase().contains(query) ||
                    l.category.lowercase().contains(query) ||
                    GrammarAcademyData.categoryBn(l.category).lowercase().contains(query) ||
                    l.formula.lowercase().contains(query) ||
                    l.rule.lowercase().contains(query) ||
                    l.ruleBn.lowercase().contains(query))
        }
    }

    val doneCount = remember(store.completedGrammar, allLessons) {
        allLessons.count { it.id in store.completedGrammar }
    }
    val progress = if (allLessons.isEmpty()) 0f else doneCount.toFloat() / allLessons.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Grammar Academy · গ্রামার একাডেমি",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "${allLessons.size} structured lessons (A1–C1) · বাংলা ও ইংরেজি ব্যাখ্যাসহ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "$doneCount / ${allLessons.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    if (onOpenQuiz != null) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onOpenQuiz,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("academy_open_quiz_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Grammar Quiz · গ্রামার কুইজ দিন")
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academy_search_input"),
                singleLine = true,
                placeholder = { Text("Search lessons / বাংলা বা ইংরেজিতে খুঁজুন…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = if (search.isNotEmpty()) {
                    {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrammarAcademyData.levels.forEach { lv ->
                    FilterChip(
                        selected = selectedLevel == lv,
                        onClick = { selectedLevel = lv },
                        label = { Text(if (lv == "All") "All · সব" else lv) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrammarAcademyData.categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            val bn = GrammarAcademyData.categoryBn(cat)
                            Text(if (cat == bn) cat else "$cat · $bn")
                        }
                    )
                }
            }
        }

        item {
            Text(
                "Showing ${filtered.size} lessons · মোট ${filtered.size}টি লেসন",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filtered, key = { it.id }) { lesson ->
            val done = lesson.id in store.completedGrammar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openLesson = lesson }
                    .testTag("academy_lesson_${lesson.id}"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (done) "Completed" else "Not completed",
                        tint = if (done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    lesson.level,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${lesson.category} · ${GrammarAcademyData.categoryBn(lesson.category)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(lesson.title, fontWeight = FontWeight.Bold)
                        if (lesson.titleBn.isNotBlank()) {
                            Text(
                                lesson.titleBn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            lesson.formula,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Open lesson",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademyLessonDetail(
    lesson: AcademyLesson,
    allLessons: List<AcademyLesson>,
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onNavigateLesson: (AcademyLesson) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val done = lesson.id in store.completedGrammar
    val idx = remember(lesson.id, allLessons) { allLessons.indexOfFirst { it.id == lesson.id } }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("academy_detail_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "${lesson.level} · ${lesson.category} (${GrammarAcademyData.categoryBn(lesson.category)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                if (lesson.titleBn.isNotBlank()) {
                    Text(
                        lesson.titleBn,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(10.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "FORMULA / STRUCTURE · সূত্র ও গঠন",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        lesson.formula,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "DETAILED EXPLANATION · বিস্তারিত ব্যাখ্যা",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(lesson.rule, style = MaterialTheme.typography.bodyLarge)
                    if (lesson.ruleBn.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "বাংলা ব্যাখ্যা:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            lesson.ruleBn,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "COMMON MISTAKE & PRO TIP · সাধারণ ভুল ও টিপস",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            lesson.tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (lesson.tipBn.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                lesson.tipBn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Examples (${lesson.examples.size}) · উদাহরণ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            lesson.examples.forEachIndexed { exIdx, ex ->
                val exBn = lesson.examplesBn.getOrNull(exIdx).orEmpty()
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            if (exBn.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    exBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        FilledIconButton(
                            onClick = { speakIn(store, speaker, ex, "en") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { store.completeGrammarLesson(lesson.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academy_complete_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (done) "Completed · সম্পন্ন ✓" else "Mark Complete · সম্পন্ন করুন (+15 XP)")
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { if (idx > 0) onNavigateLesson(allLessons[idx - 1]) },
                    enabled = idx > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Previous · পূর্ববর্তী")
                }
                OutlinedButton(
                    onClick = { if (idx in 0 until allLessons.lastIndex) onNavigateLesson(allLessons[idx + 1]) },
                    enabled = idx in 0 until allLessons.lastIndex,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Next · পরবর্তী")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
