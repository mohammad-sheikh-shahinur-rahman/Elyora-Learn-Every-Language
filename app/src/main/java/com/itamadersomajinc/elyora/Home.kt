package com.itamadersomajinc.elyora

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private data class LessonRingCardData(
    val id: String,
    val title: String,
    val titleBn: String,
    val percent: Int,
    val subtitle: String,
    val icon: ImageVector,
    val arcColor: Color,
    val iconBgStart: Color,
    val iconBgEnd: Color,
    val useOrbArt: Boolean = false,
    val onClick: () -> Unit
)

private data class HubFeature(
    val id: String,
    val title: String,
    val titleBn: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: AccentPair,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    store: Store,
    speaker: Speaker,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onOpenLanguageSwitcher: () -> Unit,
    modifier: Modifier = Modifier,
    onNav: (Screen) -> Unit,
    onOpenTool: (FeatureTool) -> Unit,
    onOpenVerbs: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    val doneToday = store.isDailyChallengeDoneToday()
    val weekXp = store.last7DaysXp()
    var gridView by remember { mutableStateOf(true) }

    val grammarTotal = GrammarAcademyData.all.size.coerceAtLeast(210)
    val readingPct = ((store.completedGrammar.size * 100) / grammarTotal).coerceIn(0, 100)
    val listeningPct = ((store.listeningCompleted * 100) / ListeningData.exercises.size.coerceAtLeast(12)).coerceIn(0, 100)
    val speakingPct = ((store.speakingCompleted * 100) / 12).coerceIn(0, 100)
    val conversationPct = ((store.completedLessons.size * 10 + store.sentencesBuilt * 8) / 2).coerceIn(0, 100)

    val coreLessonCards = listOf(
        LessonRingCardData(
            id = "grammar_academy",
            title = "Reading",
            titleBn = "পড়া ও ব্যাকরণ",
            percent = readingPct,
            subtitle = "${store.completedGrammar.size}/$grammarTotal লেসন",
            icon = Icons.Filled.MenuBook,
            arcColor = cs.primary,
            iconBgStart = cs.primary,
            iconBgEnd = accents.ctaStart,
            onClick = { onNav(Screen.GRAMMAR) }
        ),
        LessonRingCardData(
            id = "listening",
            title = "Listening",
            titleBn = "শোনার অনুশীলন",
            percent = listeningPct,
            subtitle = "${store.listeningCompleted} অডিও সম্পন্ন",
            icon = Icons.Filled.Headphones,
            arcColor = accents.pinkRing,
            iconBgStart = Color(0xFFEC70C4),
            iconBgEnd = Color(0xFFC849A3),
            onClick = { onOpenTool(FeatureTool.LISTENING) }
        ),
        LessonRingCardData(
            id = "speaking",
            title = "Speaking",
            titleBn = "বলার অনুশীলন",
            percent = speakingPct,
            subtitle = "${store.speakingCompleted} বার অনুশীলন",
            icon = Icons.Filled.Mic,
            arcColor = accents.pinkRing,
            iconBgStart = Color(0xFFF38CC8),
            iconBgEnd = Color(0xFFDF5CB2),
            onClick = { onOpenTool(FeatureTool.SPEAKING) }
        ),
        LessonRingCardData(
            id = "ai_tutor",
            title = "Voice & AI Chat",
            titleBn = "ভয়েস চ্যাট · Qwen GGUF",
            percent = conversationPct,
            subtitle = "অফলাইন ভয়েস ও .gguf",
            icon = Icons.Filled.Forum,
            arcColor = accents.tealHighlight,
            iconBgStart = accents.ctaStart,
            iconBgEnd = accents.pinkRing,
            useOrbArt = true,
            onClick = { onOpenTool(FeatureTool.AI_ASSISTANT) }
        )
    )

    val studioModules = listOf(
        HubFeature(
            "tense_mastery", "12 Tenses", "টেন্স মাস্টারি",
            "${store.masteredTenses.size}/12 টি টেন্স",
            Icons.Filled.Schedule, accents.mint,
            onClick = { onOpenTool(FeatureTool.TENSE_MASTERY) }
        ),
        HubFeature(
            "grammar_quiz", "Grammar Quiz", "গ্রামার কুইজ",
            "MCQ · শূন্যস্থান · সংশোধন",
            Icons.Filled.Quiz, accents.coral,
            onClick = { onOpenTool(FeatureTool.GRAMMAR_QUIZ) }
        ),
        HubFeature(
            "sentence_builder", "Sentence Builder", "বাক্য গঠন",
            "${store.sentencesBuilt} বাক্য তৈরি",
            Icons.Filled.ViewModule, accents.amber,
            onClick = { onOpenTool(FeatureTool.SENTENCE_BUILDER) }
        ),
        HubFeature(
            "writing", "Writing Studio", "লেখার অনুশীলন",
            "অনুচ্ছেদ · ইমেইল · রচনা",
            Icons.Filled.Create, accents.lavender,
            onClick = { onOpenTool(FeatureTool.WRITING) }
        ),
        HubFeature(
            "vocabulary", "Vocabulary 25k+", "শব্দভান্ডার",
            "SQLite FTS দ্রুত খোঁজ",
            Icons.Filled.School, accents.sky,
            onClick = { onNav(Screen.DICTIONARY) }
        ),
        HubFeature(
            "verb_master", "Verb Master", "ক্রিয়া মাস্টার",
            "Regular ও Irregular",
            Icons.Filled.Spellcheck, accents.amber,
            onClick = onOpenVerbs
        )
    )

    val targetShortName = languageLabel(store.target).substringBefore(" ").ifBlank { "English" }
    val targetCode = store.target.uppercase().take(2)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // Soft lilac-lavender top gradient wash inspired by reference UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accents.heroStart, accents.heroMid, cs.background)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top Brand Bar: Elyora Emblem + Language Pill + Theme Toggle + Pro Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = if (accents.isDark) 0.14f else 0.72f),
                    modifier = Modifier.clickable { onNav(Screen.LEARN) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.elyora_logo),
                            contentDescription = "Elyora Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Elyora",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (accents.isDark) Color.White else Color(0xFF28175A)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                LanguagePillButton(
                    targetCode = store.target,
                    lightOnGradient = true,
                    onClick = onOpenLanguageSwitcher
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = if (accents.isDark) 0.16f else 0.82f),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("home_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (accents.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Light or Dark theme (${themeMode.labelEn})",
                            tint = if (accents.isDark) Color.White else Color(0xFF28175A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Hero Greeting matching "Hello Alan, Continue French!" in Reference UI
            Text(
                if (store.target == "ja") "こんにちは Learner · হ্যালো শিক্ষার্থী," else "Hello Learner · হ্যালো শিক্ষার্থী,",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.94f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Continue\n$targetShortName!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 41.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Elyora — Learn Every Language · ভাষা শিখুন, বিশ্বকে জানুন",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(20.dp))

            // "Your Lessons" Header + Grid / List View Switcher (matching Reference UI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Your Lessons · আপনার পাঠসমূহ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (gridView) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { gridView = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.GridView,
                                contentDescription = "Grid view",
                                tint = if (gridView) cs.primary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!gridView) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { gridView = false }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.ViewAgenda,
                                contentDescription = "List view",
                                tint = if (!gridView) cs.primary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 4 Featured White Rounded Cards with Circular Progress Arcs around Icons
            if (gridView) {
                coreLessonCards.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { card ->
                            LessonRingGridCard(card, Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                coreLessonCards.forEach { card ->
                    LessonRingListCard(card)
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            // Daily Challenge Featured Card with custom Streak Mascot art
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTool(FeatureTool.DAILY_CHALLENGE) }
                    .testTag("home_daily_challenge_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (doneToday) accents.mint.container else accents.amber.container
                )
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.img_mascot_streak),
                        contentDescription = "Daily Challenge Reward",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (doneToday) "Daily Challenge Done · আজকের পরীক্ষা সম্পন্ন ✓" else "Daily Challenge · আজকের দৈনিক পরীক্ষা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (doneToday) accents.mint.content else accents.amber.content
                        )
                        Text(
                            if (doneToday) "Score · স্কোর: ${store.lastDailyScore}% · আবার অনুশীলন করুন"
                            else "Fresh 10-question exam · প্রতিদিন নতুন পরীক্ষা (+50 XP)",
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (doneToday) accents.mint.content else accents.amber.content).copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        if (doneToday) Icons.Filled.CheckCircle else Icons.Filled.ChevronRight,
                        contentDescription = "Open Daily Challenge",
                        tint = if (doneToday) accents.mint.content else accents.amber.content
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Practice Studio · দক্ষতা অনুশীলন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(10.dp))

            studioModules.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { feat ->
                        CompactSkillTile(feat, Modifier.weight(1f))
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))

            // Streak & 7-day Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNav(Screen.PROFILE) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                border = BorderStroke(1.dp, accents.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accents.lavender.container
                        ) {
                            Text(
                                "LV ${store.level()}",
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = accents.lavender.content
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "🔥 ${store.streak} day streak · ${store.xp} XP",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Accuracy · নির্ভুলতা: ${store.accuracyPercent()}% · Best: ${store.bestStreak}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Analytics",
                            tint = cs.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekXp.forEach { (dayLabel, dayXp) ->
                            val active = dayXp > 0
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (active) cs.primary else cs.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (active) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = dayLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    dayLabel,
                                    fontSize = 10.sp,
                                    color = if (active) cs.primary else cs.onSurfaceVariant,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            val due = store.dueCount()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                border = BorderStroke(1.dp, accents.cardBorder)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Continue Flashcards · রিভিশন চালিয়ে যান", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                        Text(
                            if (due > 0) "$due flashcards ready · রিভিশনের জন্য প্রস্তুত"
                            else "${store.wordsLearnedCount()} words saved · ধাপে ধাপে কোর্স করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { onNav(if (due > 0) Screen.REVIEW else Screen.LEARN) },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (due > 0) "Review" else "Learn")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant.copy(alpha = 0.55f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Study Tip · আজকের পড়াশোনার টিপস", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(6.dp))
                    val tip = Tips.tipOfTheDay()
                    Text(tip.first, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(tip.second, style = MaterialTheme.typography.bodySmall, color = cs.primary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LessonRingGridCard(card: LessonRingCardData, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    Card(
        modifier = modifier
            .clickable(onClick = card.onClick)
            .testTag("hub_tile_${card.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, accents.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            LessonProgressRingBadge(card = card, sizeDp = 76)
            Spacer(Modifier.height(14.dp))
            Text(
                card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                card.titleBn,
                style = MaterialTheme.typography.labelSmall,
                color = cs.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${card.percent}% completed · ${card.subtitle}",
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LessonRingListCard(card: LessonRingCardData) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = card.onClick)
            .testTag("hub_tile_${card.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, accents.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonProgressRingBadge(card = card, sizeDp = 62)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(card.titleBn, style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.SemiBold)
                Text("${card.percent}% completed · ${card.subtitle}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = card.title, tint = cs.primary)
        }
    }
}

@Composable
private fun LessonProgressRingBadge(card: LessonRingCardData, sizeDp: Int) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val displayFraction = (card.percent.coerceAtLeast(18) / 100f).coerceIn(0.18f, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(sizeDp.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 3.5.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
                color = trackColor.copy(alpha = 0.65f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = card.arcColor,
                startAngle = -75f,
                sweepAngle = 360f * displayFraction,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        if (card.useOrbArt) {
            Image(
                painter = painterResource(R.drawable.img_ai_orb),
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size((sizeDp - 20).dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size((sizeDp - 20).dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(card.iconBgStart, card.iconBgEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = Color.White,
                    modifier = Modifier.size((sizeDp / 2.8f).dp)
                )
            }
        }
    }
}

@Composable
private fun CompactSkillTile(f: HubFeature, modifier: Modifier = Modifier) {
    val accents = LocalElyoraAccents.current
    Card(
        modifier = modifier
            .clickable(onClick = f.onClick)
            .testTag("hub_tile_${f.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accents.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = f.accent.container) {
                Icon(
                    imageVector = f.icon,
                    contentDescription = f.title,
                    tint = f.accent.content,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(f.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(f.titleBn, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(f.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

object Tips {
    private val tips = listOf(
        "Spaced repetition beats cramming: short daily reviews make memories stick." to "একসাথে অনেক পড়ার চেয়ে প্রতিদিন অল্প রিভিশন দিলে শব্দ দীর্ঘদিন মনে থাকে।",
        "Say words ALOUD — hearing yourself speak doubles the memory." to "শব্দগুলো জোরে উচ্চারণ করে পড়ুন — নিজের কণ্ঠ শুনলে মনে রাখার ক্ষমতা দ্বিগুণ হয়।",
        "Master the 12 tenses by noticing signal words like 'since', 'yet', and 'by tomorrow'." to "'since', 'yet', 'by tomorrow'-এর মতো ইঙ্গিত শব্দ চিনলেই ১২টি টেন্স ধরা সহজ হয়।",
        "Learn words in chunks ('I look forward to…') — you'll speak in sentences faster." to "একা শব্দ না শিখে ছোট বাক্যাংশ হিসেবে শিখুন — দ্রুত কথা বলতে পারবেন।",
        "A 5-minute Daily Challenge today keeps your streak and accuracy sharp." to "প্রতিদিন ৫ মিনিটের দৈনিক পরীক্ষা আপনার ধারা ও নির্ভুলতা অটুট রাখে।",
        "The first 500 high-frequency words cover over 70% of everyday English." to "সবচেয়ে বেশি ব্যবহৃত প্রথম ৫০০টি শব্দ দৈনন্দিন ইংরেজির ৭০%-এর বেশি কাভার করে।",
        "Write one short paragraph a day to turn passive grammar rules into active fluency." to "প্রতিদিন একটি ছোট অনুচ্ছেদ লিখলে শেখা ব্যাকরণ বাস্তব দক্ষতায় পরিণত হয়।",
        "Mistakes are data, not failure. Every wrong answer teaches a rule that sticks." to "ভুল করা মানেই ব্যর্থতা নয় — প্রতিটি ভুল উত্তর একটি নতুন নিয়ম স্থায়ীভাবে শেখায়।",
        "Shadow the audio in Speaking Practice: repeat right after the voice." to "বলার অনুশীলনে অডিও শোনার পরপরই হুবহু একইভাবে বলার চেষ্টা করুন।",
        "Consistency beats intensity. 10 focused minutes every day wins." to "একদিন ২ ঘণ্টা পড়ার চেয়ে প্রতিদিন মনোযোগ দিয়ে ১০ মিনিট পড়া অনেক বেশি কার্যকর।"
    )

    fun tipOfTheDay(): Pair<String, String> {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return tips[day % tips.size]
    }
}
