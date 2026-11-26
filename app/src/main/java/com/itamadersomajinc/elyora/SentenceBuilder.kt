package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BuilderItem(
    val id: String,
    val level: String,
    val category: String,
    val formula: String,
    val sentence: String,
    val hintBn: String
)

object SentenceBuilderData {
    val levels = listOf("All", "A1", "A2", "B1", "B2")

    val items: List<BuilderItem> = listOf(
        BuilderItem("sb1", "A1", "Daily Routine · দৈনন্দিন অভ্যাস", "Subject + V1(-s) + Object + Time", "She drinks green tea every morning.", "সে প্রতিদিন সকালে গ্রিন টি পান করে।"),
        BuilderItem("sb2", "A1", "Daily Routine · দৈনন্দিন অভ্যাস", "Subject + V1 + Place + Adverb", "We walk to school together every day.", "আমরা প্রতিদিন একসাথে স্কুলে হেঁটে যাই।"),
        BuilderItem("sb3", "A1", "Questions · প্রশ্ন", "Do/Does + Subject + V1 + Object?", "Do you speak English at work?", "আপনি কি কাজে ইংরেজি বলেন?"),
        BuilderItem("sb4", "A1", "Negative · না-বোধক", "Subject + does not + V1 + Object", "He does not eat fast food.", "সে ফাস্ট ফুড খায় না।"),
        BuilderItem("sb5", "A1", "Present Continuous · ঘটমান বর্তমান", "Subject + is + V-ing + Object + now", "My sister is reading a novel now.", "আমার বোন এখন একটি উপন্যাস পড়ছে।"),
        BuilderItem("sb6", "A1", "Past Simple · সাধারণ অতীত", "Subject + V2 + Object + Time", "They visited their grandparents last weekend.", "তারা গত সপ্তাহান্তে তাদের দাদা-দাদির সাথে দেখা করেছিল।"),
        BuilderItem("sb7", "A1", "Future Simple · সাধারণ ভবিষ্যৎ", "Subject + will + V1 + Object + Time", "I will call you tomorrow evening.", "আমি আগামীকাল সন্ধ্যায় তোমাকে ফোন করব।"),
        BuilderItem("sb8", "A1", "There is/are · অস্তিত্ব", "There are + Plural Noun + Place", "There are many books on my desk.", "আমার ডেস্কে অনেক বই আছে।"),
        BuilderItem("sb9", "A2", "Present Perfect · পুরাঘটিত বর্তমান", "Subject + have + V3 + Object", "I have already finished my homework.", "আমি ইতিমধ্যে আমার বাড়ির কাজ শেষ করেছি।"),
        BuilderItem("sb10", "A2", "Present Perfect · পুরাঘটিত বর্তমান", "Have + Subject + ever + V3 + Place?", "Have you ever traveled to Japan?", "তুমি কি কখনো জাপানে ভ্রমণ করেছ?"),
        BuilderItem("sb11", "A2", "Past Continuous · ঘটমান অতীত", "Subject + was + V-ing + when + Past Simple", "I was cooking dinner when you called.", "তুমি যখন ফোন করেছিলে আমি তখন রাতের খাবার রান্না করছিলাম।"),
        BuilderItem("sb12", "A2", "Comparatives · তুলনা", "Subject + is + Comparative + than + Object", "Learning grammar is easier than I thought.", "ব্যাকরণ শেখা আমার ধারণার চেয়ে সহজ।"),
        BuilderItem("sb13", "A2", "Modals · মডাল", "Subject + should + V1 + Object", "You should practice speaking English every day.", "তোমার প্রতিদিন ইংরেজি বলার অনুশীলন করা উচিত।"),
        BuilderItem("sb14", "A2", "Infinitives · ইনফিনিটিভ", "Subject + want + to + V1 + Object", "She wants to become a software engineer.", "সে একজন সফটওয়্যার ইঞ্জিনিয়ার হতে চায়।"),
        BuilderItem("sb15", "A2", "Prepositions · অব্যয়", "Subject + V1 + Object + at + Time", "Our English class begins at nine o'clock.", "আমাদের ইংরেজি ক্লাস নয়টায় শুরু হয়।"),
        BuilderItem("sb16", "B1", "First Conditional · ১ম শর্তবাক্য", "If + Present Simple, Subject + will + V1", "If it rains tomorrow, we will stay inside.", "আগামীকাল বৃষ্টি হলে আমরা ভেতরে থাকব।"),
        BuilderItem("sb17", "B1", "Second Conditional · ২য় শর্তবাক্য", "If + Past Simple, Subject + would + V1", "If I had more time, I would learn Japanese.", "আমার আরও সময় থাকলে আমি জাপানি শিখতাম।"),
        BuilderItem("sb18", "B1", "Passive Voice · কর্মবাচ্য", "Subject + was/were + V3 + by + Agent", "This famous poem was written by Tagore.", "এই বিখ্যাত কবিতাটি রবীন্দ্রনাথ লিখেছিলেন।"),
        BuilderItem("sb19", "B1", "Present Perfect Cont. · পুরাঘটিত ঘটমান", "Subject + has + been + V-ing + since", "She has been studying medicine since 2022.", "সে ২০২২ সাল থেকে মেডিসিন পড়ছে।"),
        BuilderItem("sb20", "B1", "Relative Clauses · রিলেটিভ ক্লজ", "Noun + who + Verb + Object", "The teacher who helped me is very kind.", "যে শিক্ষক আমাকে সাহায্য করেছিলেন তিনি খুব দয়ালু।"),
        BuilderItem("sb21", "B1", "Past Perfect · পুরাঘটিত অতীত", "When + Past Simple, Subject + had + V3", "When we arrived, the train had already left.", "আমরা পৌঁছানোর আগেই ট্রেনটি ছেড়ে চলে গিয়েছিল।"),
        BuilderItem("sb22", "B1", "Gerunds · জেরান্ড", "Subject + enjoy + V-ing + Object", "They enjoy listening to podcasts while walking.", "তারা হাঁটার সময় পডকাস্ট শুনতে উপভোগ করে।"),
        BuilderItem("sb23", "B2", "Third Conditional · ৩য় শর্তবাক্য", "If + had + V3, Subject + would have + V3", "If he had worked harder, he would have passed.", "সে আরও কঠোর পরিশ্রম করলে পাস করত।"),
        BuilderItem("sb24", "B2", "Concession · বৈপরীত্য", "Although + Clause 1, Clause 2", "Although she was tired, she finished the project.", "যদিও সে ক্লান্ত ছিল, তবুও সে প্রকল্পটি শেষ করেছে।"),
        BuilderItem("sb25", "B2", "Future Perfect · পুরাঘটিত ভবিষ্যৎ", "By + Future Time, Subject + will have + V3", "By next year, I will have mastered twelve tenses.", "আগামী বছরের মধ্যে আমি বারোটি টেন্স আয়ত্ত করে ফেলব।"),
        BuilderItem("sb26", "B2", "Reported Speech · পরোক্ষ উক্তি", "Subject + said + that + Past Clause", "He said that he was preparing for the interview.", "সে বলেছিল যে সে ইন্টারভিউয়ের জন্য প্রস্তুতি নিচ্ছে।"),
        BuilderItem("sb27", "B2", "Passive Modal · প্যাসিভ মডাল", "Subject + must + be + V3 + Time", "This application must be submitted before Friday.", "এই আবেদনটি শুক্রবারের আগে জমা দিতে হবে।"),
        BuilderItem("sb28", "B2", "Inversion · উল্টানো গঠন", "Not only + Aux + Subject + V1, but also...", "Not only does she speak English, but also French.", "সে শুধু ইংরেজিই নয়, ফরাসিও বলে।")
    )

    fun allWithTenses(): List<BuilderItem> {
        val dynamic = Tenses.patterns.take(12).flatMap { p ->
            p.examples.take(2).mapIndexed { i, ex ->
                BuilderItem(
                    id = "t_${p.id}_$i",
                    level = p.level.ifBlank { "A2" },
                    category = p.titleEn.substringBefore("—").trim(),
                    formula = p.titleEn.substringAfter("—", p.titleEn).trim(),
                    sentence = ex.ja,
                    hintBn = ex.glossFor("bn", "en").ifBlank { appendBangla(ex.en, ex.ja) }
                )
            }
        }
        return items + dynamic
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    var selectedLevel by remember { mutableStateOf("All") }
    var showLevelChips by remember { mutableStateOf(false) }
    val allItems = remember { SentenceBuilderData.allWithTenses() }
    val pool = remember(selectedLevel, allItems) {
        if (selectedLevel == "All") allItems else allItems.filter { it.level == selectedLevel }
    }
    var idx by remember(selectedLevel) { mutableStateOf(0) }
    val item = pool.getOrNull(idx % pool.size.coerceAtLeast(1)) ?: SentenceBuilderData.items.first()

    val targetTokens = remember(item.id) { item.sentence.split(" ").filter { it.isNotBlank() } }
    val scrambled = remember(item.id) {
        targetTokens.mapIndexed { i, w -> i to w }.shuffled()
    }
    var selectedIndices by remember(item.id) { mutableStateOf(listOf<Int>()) }
    var checked by remember(item.id) { mutableStateOf<Boolean?>(null) }

    val builtSentence = selectedIndices.map { i -> targetTokens[i] }.joinToString(" ")
    val remainingBank = scrambled.filter { (i, _) -> i !in selectedIndices }
    val difficultyProgress = when (item.level) {
        "A1" -> 0.28f
        "A2" -> 0.52f
        "B1" -> 0.76f
        else -> 0.94f
    }

    Column(
        modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Minimal Reference Header: Close X + Centered Title + Level Filter button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClose != null) {
                Surface(
                    shape = CircleShape,
                    color = cs.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("builder_back_button")) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = cs.onSurface)
                    }
                }
            } else {
                Spacer(Modifier.size(44.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Reading & Sentence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "বাক্য গঠন · Built: ${store.sentencesBuilt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                shape = CircleShape,
                color = if (showLevelChips) cs.primaryContainer else cs.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = { showLevelChips = !showLevelChips }) {
                    Icon(Icons.Filled.Tune, contentDescription = "Filter Level", tint = cs.onSurface)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // "Difficulty level" Slim Purple Progress Bar (Reference Left Screen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Difficulty level · ${item.level}",
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${(idx % pool.size) + 1} / ${pool.size}",
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { difficultyProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            strokeCap = StrokeCap.Round,
            color = cs.primary,
            trackColor = cs.surfaceVariant
        )

        AnimatedVisibility(visible = showLevelChips) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SentenceBuilderData.levels.forEach { lv ->
                    FilterChip(
                        selected = selectedLevel == lv,
                        onClick = { selectedLevel = lv },
                        label = { Text(if (lv == "All") "All · সব" else lv) }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Tutor Chat Bubble with Avatar & Teal/Purple Highlighted Structure Prompt (Reference Left Screen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(R.drawable.img_ai_orb),
                contentDescription = "Elyora Tutor",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                border = BorderStroke(1.dp, accents.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = accents.tealHighlight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.formula,
                        style = MaterialTheme.typography.titleSmall,
                        color = cs.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (item.hintBn.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            item.hintBn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // User Purple Response Bubble (Aligned Right, Reference Left Screen)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .heightIn(min = 68.dp),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 6.dp),
                color = when (checked) {
                    true -> accents.tealHighlight
                    false -> cs.error
                    null -> cs.primary
                },
                shadowElevation = 3.dp
            ) {
                if (selectedIndices.isEmpty()) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "Tap words below to answer · নিচের শব্দগুলোতে ট্যাপ করুন…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedIndices.forEachIndexed { pos, origIdx ->
                            val word = targetTokens[origIdx]
                            val chipBg = when {
                                checked == null -> Color.White.copy(alpha = 0.2f)
                                pos < targetTokens.size && word == targetTokens[pos] -> Color.White.copy(alpha = 0.28f)
                                else -> Color.Black.copy(alpha = 0.24f)
                            }
                            Surface(
                                modifier = Modifier.clickable(enabled = checked != true) {
                                    selectedIndices = selectedIndices - origIdx
                                    checked = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = chipBg
                            ) {
                                Text(
                                    word,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Elevated White Word-Bank Tray (Reference Left Screen)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surface),
            border = BorderStroke(1.dp, accents.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Word Bank · শব্দ ভান্ডার",
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    remainingBank.forEach { (origIdx, word) ->
                        Surface(
                            modifier = Modifier
                                .clickable(enabled = checked != true) {
                                    selectedIndices = selectedIndices + origIdx
                                    checked = null
                                }
                                .testTag("builder_word_$origIdx"),
                            shape = RoundedCornerShape(14.dp),
                            color = cs.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, accents.cardBorder)
                        ) {
                            Text(
                                word,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                fontWeight = FontWeight.SemiBold,
                                color = cs.onSurface
                            )
                        }
                    }
                    if (remainingBank.isEmpty()) {
                        Text(
                            "All words placed · সব শব্দ বসানো হয়েছে!",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    val nextPos = selectedIndices.size
                    if (nextPos < targetTokens.size) {
                        val neededWord = targetTokens[nextPos]
                        val match = remainingBank.firstOrNull { it.second == neededWord }
                        if (match != null) {
                            selectedIndices = selectedIndices + match.first
                            checked = null
                        }
                    }
                },
                enabled = checked != true && selectedIndices.size < targetTokens.size,
                modifier = Modifier
                    .weight(1f)
                    .testTag("builder_hint_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hint · ইঙ্গিত")
            }
            OutlinedButton(
                onClick = { selectedIndices = emptyList(); checked = null },
                enabled = selectedIndices.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear · মুছুন")
            }
        }

        Spacer(Modifier.height(12.dp))
        val canCheck = selectedIndices.size == targetTokens.size && checked != true
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (canCheck) Brush.horizontalGradient(listOf(accents.ctaStart, accents.ctaEnd))
                    else Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surfaceVariant))
                )
                .clickable(enabled = canCheck) {
                    val ok = builtSentence.trim().equals(item.sentence.trim(), ignoreCase = true)
                    checked = ok
                    store.recordSentenceBuilt(ok)
                    if (ok) speakIn(store, speaker, item.sentence, "en")
                }
                .testTag("builder_check_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (canCheck) Color.White else cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Check Sentence · বাক্য যাচাই করুন",
                    color = if (canCheck) Color.White else cs.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (checked != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (checked == true) cs.tertiaryContainer else cs.errorContainer
                )
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (checked == true) "Perfect Sentence! · দারুণ! (+8 XP)" else "Correct Order · সঠিক বাক্য:",
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(item.sentence, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        if (item.hintBn.isNotEmpty()) {
                            Text(item.hintBn, style = MaterialTheme.typography.bodySmall, color = cs.primary)
                        }
                    }
                    FilledIconButton(
                        onClick = { speakIn(store, speaker, item.sentence, "en") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.horizontalGradient(listOf(accents.ctaStart, accents.ctaEnd)))
                    .clickable { idx += 1 }
                    .testTag("builder_next_button"),
                contentAlignment = Alignment.Center
            ) {
                Text("Next Sentence · পরবর্তী বাক্য", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
