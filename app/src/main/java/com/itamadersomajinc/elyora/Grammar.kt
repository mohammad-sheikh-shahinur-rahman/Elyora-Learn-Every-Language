package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.ui.platform.testTag

private enum class GrammarTab(val label: String) {
    ACADEMY("Academy 210+ · একাডেমি"),
    TENSES("12 Tenses · টেন্স"),
    QUIZ("Quiz · কুইজ"),
    AI("AI Tutor · এআই"),
    PATTERNS("Patterns · প্যাটার্ন")
}

@Composable
fun GrammarScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onOpenLanguageSwitcher: () -> Unit = {}
) {
    var activeTab by remember {
        mutableStateOf(if (store.target == "en") GrammarTab.ACADEMY else GrammarTab.PATTERNS)
    }
    val visibleTabs = remember(store.aiAssistantEnabled) {
        GrammarTab.entries.filter { it != GrammarTab.AI || store.aiAssistantEnabled }
    }
    if (activeTab == GrammarTab.AI && !store.aiAssistantEnabled) {
        activeTab = GrammarTab.ACADEMY
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleTabs.forEach { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag("grammar_tab_${tab.name.lowercase()}")
                )
            }
            LanguagePillButton(targetCode = store.target, onClick = onOpenLanguageSwitcher)
        }

        when (activeTab) {
            GrammarTab.ACADEMY -> GrammarAcademyView(
                store = store,
                speaker = speaker,
                modifier = Modifier.weight(1f),
                onOpenQuiz = { activeTab = GrammarTab.QUIZ }
            )
            GrammarTab.TENSES -> TenseMasteryScreen(
                store = store,
                speaker = speaker,
                modifier = Modifier.weight(1f)
            )
            GrammarTab.QUIZ -> GrammarQuizScreen(
                store = store,
                speaker = speaker,
                modifier = Modifier.weight(1f)
            )
            GrammarTab.AI -> AiAssistantScreen(
                store = store,
                speaker = speaker,
                onOpenLanguageSwitcher = onOpenLanguageSwitcher,
                modifier = Modifier.weight(1f)
            )
            GrammarTab.PATTERNS -> ClassicPatternsScreen(
                store = store,
                speaker = speaker,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClassicPatternsScreen(store: Store, speaker: Speaker, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf<Pattern?>(null) }
    var autoQuiz by remember { mutableStateOf(false) }
    var q by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    val targetLang = store.target
    val learningJa = targetLang == "ja"
    val learningEn = targetLang == "en"
    val learningOther = !learningJa && !learningEn
    val query = q.trim().lowercase()

    fun matches(p: Pattern): Boolean {
        if (query.isEmpty()) return true
        return p.titleEn.lowercase().contains(query) || p.titleJa.lowercase().contains(query) ||
            p.ruleEn.lowercase().contains(query) || p.ruleJa.lowercase().contains(query)
    }

    when {
        open != null -> PatternDetail(open!!, store, speaker, modifier, autoQuiz = autoQuiz, onClose = { open = null; autoQuiz = false })
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text("Grammar Patterns · গ্রামার প্যাটার্ন", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(
                "Learn the patterns with Bengali & English explanations · বাংলা ও ইংরেজিসহ প্যাটার্ন শিখুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search grammar / খুঁজুন — e.g. would, have…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (q.isNotEmpty()) {
                    {
                        IconButton(onClick = { q = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(16.dp)
            )
            if (query.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Searching \"$q\"",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))

            val jaCore = Data.allPatterns.filter { it.lang == "ja" && it.source.isEmpty() && matches(it) }
            val enCore = Data.allPatterns.filter { it.lang == "en" && it.source.isEmpty() && matches(it) }
            val genki = Genki.patterns.filter { matches(it) }
            val jfz = Jfz.patterns.filter { matches(it) }
            val jg = JapaneseGrammar.patterns.filter { matches(it) }
            val eg = EnglishGrammar.patterns.filter { matches(it) }
            val tenses = Tenses.patterns.filter { matches(it) }
            val otherCore = if (learningOther) remember(targetLang) { TargetContent.patterns(targetLang) } else emptyList()
            val otherCoreMatches = otherCore.filter { it.source.isEmpty() && matches(it) }
            val otherEssentials = otherCore.filter { it.source == "english" && matches(it) }
            val total = (if (learningJa) jaCore.size + genki.size + jfz.size + jg.size else 0) +
                (if (learningEn) enCore.size + eg.size + tenses.size else 0) +
                (if (learningOther) otherCoreMatches.size + otherEssentials.size else 0)

            val practicePool = when {
                learningJa -> Data.allPatterns.filter { it.lang == "ja" && it.source.isEmpty() } +
                    Genki.patterns + Jfz.patterns + JapaneseGrammar.patterns
                learningEn -> Data.allPatterns.filter { it.lang == "en" && it.source.isEmpty() } +
                    EnglishGrammar.patterns + Tenses.patterns
                else -> otherCore
            }

            if (practicePool.isNotEmpty()) {
                Button(
                    onClick = {
                        autoQuiz = true
                        open = practicePool.random()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🎯 Practice random grammar · এলোমেলো অনুশীলন")
                }
                Spacer(Modifier.height(14.dp))
            }

            if (query.isNotEmpty() && total == 0) {
                Text(
                    "No matches for \"$q\" · কোনো ফলাফল পাওয়া যায়নি।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            } else if (learningOther) {
                SectionList("${languageLabel(targetLang)} patterns", otherCoreMatches, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                Spacer(Modifier.height(12.dp))
                SectionList("Grammar essentials · মূল ব্যাকরণ", otherEssentials, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                Spacer(Modifier.height(12.dp))
            } else {
                if (learningJa) {
                    SectionList("Japanese patterns · জাপানি প্যাটার্ন", jaCore, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                    SectionList("Genki Textbook 1 · গেনকি বই ১", genki, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                    SectionList("Japanese From Zero", jfz, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                    SectionList("Japanese Essentials · জাপানি মূল নিয়ম", jg, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                }
                if (learningEn) {
                    SectionList("Tenses · টেন্স ও কাল", tenses, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                    SectionList("English patterns · ইংরেজি প্যাটার্ন", enCore, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                    SectionList("English Grammar Essentials · ইংরেজি মূল ব্যাকরণ", eg, query.isNotEmpty(), store.nativeLang, onClick = { open = it })
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionList(title: String, ps: List<Pattern>, isSearch: Boolean, native: String, onClick: (Pattern) -> Unit) {
    if (ps.isEmpty()) return
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (isSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
    if (isSearch) {
        Text(
            "${ps.size} match" + (if (ps.size == 1) "" else "es"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(6.dp))
    ps.forEach { p ->
        PatternRow(p, native, onClick = { onClick(p) })
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PatternRow(p: Pattern, native: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(p.titleEn, fontWeight = FontWeight.Bold)
                val sub = patternSubtitle(p, native)
                if (sub.isNotEmpty()) {
                    Text(
                        sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun patternSubtitle(p: Pattern, native: String): String {
    val bnTitle = banglaGloss(p.titleEn)
    val baseSub = when {
        p.lang == "ja" || native == "ja" -> p.titleJa
        native == "en" || native.isBlank() -> ""
        else -> Gloss.lookup(p.titleEn) ?: ""
    }
    return when {
        baseSub.isNotEmpty() && bnTitle.isNotEmpty() && !hasBengaliScript(baseSub) -> "$baseSub · $bnTitle"
        baseSub.isNotEmpty() -> baseSub
        else -> bnTitle
    }
}

@Composable
private fun PatternDetail(p: Pattern, store: Store, speaker: Speaker, modifier: Modifier = Modifier, autoQuiz: Boolean = false, onClose: () -> Unit) {
    var quiz by remember { mutableStateOf(autoQuiz) }
    if (quiz) {
        PatternQuiz(p, store, speaker, modifier, onClose = { quiz = false })
        return
    }
    BackHandler(onBack = onClose)
    val exs = remember(p.id, store.nativeLang, p.lang) { GrammarPacks.examples(p) }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(p.titleEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val sub = patternSubtitle(p, store.nativeLang)
                if (sub.isNotEmpty()) {
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(6.dp))

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            RuleCard(p, store)
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Examples (${exs.size}) · উদাহরণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = { quiz = true }) {
                    Text("🎯 Practice · অনুশীলন")
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap 🔊 to hear, then practice · শুনতে 🔊 ট্যাপ করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            exs.forEach { ex ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ex.emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ex.ja, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            if (ex.romaji.isNotBlank()) {
                                Text(
                                    ex.romaji,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(ex.glossFor(store.nativeLang, p.lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        FilledIconButton(
                            onClick = { speak(store, speaker, ex.ja, p.lang != "en") },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "Hear", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RuleCard(p: Pattern, store: Store) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(18.dp)) {
            RuleParagraphs(p.ruleFor(store.nativeLang))
            if (store.nativeLang != "en") {
                Spacer(Modifier.height(10.dp))
                RuleParagraphs(p.ruleEn)
            }
        }
    }
}

@Composable
private fun RuleParagraphs(text: String) {
    val paragraphs = text.split("\n").filter { it.isNotBlank() }
    paragraphs.forEach { para ->
        val labelMatch = Regex("^([A-Z][A-Z ]{2,20}):").find(para)
        if (labelMatch != null) {
            Text(
                labelMatch.groupValues[1] + ":",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            val rest = para.substring(labelMatch.range.last + 1).trim()
            if (rest.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(rest, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(para, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
    }
}

private data class QuizQ(
    val prompt: String,
    val answer: String,
    val options: List<String>,
    val sentence: String
)

@Composable
private fun PatternQuiz(
    p: Pattern,
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val native = store.nativeLang
    val questions = remember(p.id, native, p.lang) {
        val pool = GrammarPacks.examples(p).mapNotNull { ex ->
            val prompt = ex.glossFor(native, p.lang)
            if (prompt.isBlank()) null else QuizQ(prompt, ex.ja, emptyList(), ex.ja)
        }
        if (pool.size < 4) {
            emptyList()
        } else {
            pool.shuffled().map { q ->
                val distractors = pool.filter { it.answer != q.answer }
                    .shuffled().take(3).map { it.answer }
                q.copy(options = (distractors + q.answer).shuffled())
            }
        }
    }
    var qi by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf<String?>(null) }

    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    "Practice · অনুশীলন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    p.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (questions.isEmpty()) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        "Not enough examples yet · অনুশীলনের জন্য পর্যাপ্ত উদাহরণ নেই।",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to examples · উদাহরণে ফিরুন")
                }
                Spacer(Modifier.height(24.dp))
                return@Column
            }

            if (qi >= questions.size) {
                val perfect = score == questions.size
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (perfect) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Text(
                            if (perfect) "Perfect score! · দারুণ! 🎉" else "Nice work! · ভালো হয়েছে!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Score · স্কোর: $score / ${questions.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { qi = 0; score = 0; picked = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Practise again · আবার অনুশীলন")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to examples · উদাহরণে ফিরুন")
                }
                Spacer(Modifier.height(24.dp))
                return@Column
            }

            val q = questions[qi]
            Text(
                "Question ${qi + 1} / ${questions.size} · প্রশ্ন",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Score · স্কোর: $score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Choose the matching sentence · সঠিক বাক্যটি বেছে নিন:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        q.prompt,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            val revealed = picked != null
            q.options.forEach { opt ->
                val isAnswer = opt == q.answer
                val bg = when {
                    !revealed -> MaterialTheme.colorScheme.surfaceVariant
                    isAnswer -> MaterialTheme.colorScheme.tertiaryContainer
                    opt == picked -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !revealed) {
                            picked = opt
                            if (isAnswer) score += 1
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bg)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            opt,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        if (revealed && isAnswer) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else if (revealed && opt == picked) {
                            Text("✗", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (revealed) {
                FilledIconButton(onClick = { speak(store, speaker, q.answer, p.lang != "en") }) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Hear")
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (qi < questions.size - 1) {
                            qi += 1
                            picked = null
                        } else {
                            qi = questions.size
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (qi < questions.size - 1) "Next question · পরবর্তী প্রশ্ন" else "See results · ফলাফল")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
