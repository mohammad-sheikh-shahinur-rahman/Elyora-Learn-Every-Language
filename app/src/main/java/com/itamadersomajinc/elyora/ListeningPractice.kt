package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ListeningMode(val label: String) {
    COMPREHENSION("Audio Story · গল্প"),
    CHOOSE("Listen & Choose · বাছাই"),
    DICTATION("Dictation · শ্রুতিলিখন")
}

data class ListeningExercise(
    val id: String,
    val mode: ListeningMode,
    val title: String,
    val titleBn: String,
    val level: String,
    val audioScript: String,
    val scriptBn: String,
    val question: String,
    val questionBn: String,
    val options: List<String>,
    val answer: String
)

object ListeningData {
    val exercises: List<ListeningExercise> = listOf(
        // Audio Stories (Comprehension)
        ListeningExercise(
            "ls1", ListeningMode.COMPREHENSION, "Morning Commute", "সকালের যাতায়াত", "A2",
            "Every weekday, Nadia leaves her apartment at seven thirty and walks ten minutes to the metro station. She prefers taking the train because the main road is always crowded with buses and cars.",
            "প্রতি কর্মদিবসে নাদিয়া সকাল সাড়ে সাতটায় তার ফ্ল্যাট থেকে বের হয় এবং দশ মিনিট হেঁটে মেট্রো স্টেশনে যায়। সে ট্রেনে যেতেই পছন্দ করে কারণ প্রধান সড়কে সবসময় বাস ও গাড়ির ভিড় থাকে।",
            "Why does Nadia prefer taking the train?",
            "নাদিয়া কেন ট্রেনে যাওয়া পছন্দ করে?",
            listOf("The train ticket is free", "The main road is always crowded", "Her apartment is inside the station", "She works on weekends only"),
            "The main road is always crowded"
        ),
        ListeningExercise(
            "ls2", ListeningMode.COMPREHENSION, "Library Announcement", "লাইব্রেরির ঘোষণা", "B1",
            "Attention all visitors. The university library will close thirty minutes earlier today at eight thirty PM due to electrical maintenance on the third floor. Please return any borrowed laptops to the main desk before eight fifteen.",
            "সকল পাঠকের দৃষ্টি আকর্ষণ করছি। তৃতীয় তলায় বৈদ্যুতিক রক্ষণাবেক্ষণের কারণে বিশ্ববিদ্যালয় লাইব্রেরি আজ ৩০ মিনিট আগে রাত ৮:৩০টায় বন্ধ হবে। দয়া করে ধার নেওয়া ল্যাপটপ ৮:১৫-এর আগেই মূল ডেস্কে জমা দিন।",
            "What time should visitors return borrowed laptops?",
            "দর্শনার্থীদের কখন ধার নেওয়া ল্যাপটপ ফেরত দিতে হবে?",
            listOf("Before 7:30 PM", "Before 8:15 PM", "At 8:30 PM", "Tomorrow morning"),
            "Before 8:15 PM"
        ),
        ListeningExercise(
            "ls3", ListeningMode.COMPREHENSION, "Coffee Shop Order", "কফি শপের অর্ডার", "A1",
            "Good afternoon! I would like one large iced coffee with less sugar and two warm croissants, please. I will take them to go because my bus arrives in five minutes.",
            "শুভ অপরাহ্ণ! দয়া করে কম চিনি দিয়ে একটি বড় আইসড কফি এবং দুটি গরম ক্রসোঁ দিন। আমি এগুলো সাথে নিয়ে যাব কারণ পাঁচ মিনিটের মধ্যে আমার বাস চলে আসবে।",
            "Why does the customer want the order to go?",
            "ক্রেতা কেন খাবারগুলো পার্সেল করে নিতে চাইছেন?",
            listOf("There are no empty tables", "The coffee shop is closing", "The bus arrives in five minutes", "They forgot their wallet"),
            "The bus arrives in five minutes"
        ),
        ListeningExercise(
            "ls4", ListeningMode.COMPREHENSION, "Weekend Weather", "সপ্তাহান্তের আবহাওয়া", "B1",
            "Although Saturday morning will start with bright sunshine and warm breezes, heavy clouds are expected to move in by late afternoon, bringing steady rainfall throughout Sunday.",
            "শনিবার সকাল উজ্জ্বল রোদ ও উষ্ণ বাতাসে শুরু হলেও বিকেলের শেষ দিকে ঘন মেঘ জমে আসতে পারে এবং রবিবার সারাদিন একটানা বৃষ্টি হতে পারে।",
            "When is the rain expected to arrive?",
            "বৃষ্টি কখন আসতে পারে বলে ধারণা করা হচ্ছে?",
            listOf("Friday night", "Saturday morning", "Late Saturday afternoon", "Monday morning"),
            "Late Saturday afternoon"
        ),

        // Listen & Choose
        ListeningExercise(
            "lc1", ListeningMode.CHOOSE, "Polite Request", "ভদ্র অনুরোধ", "A1",
            "Could you turn down the volume, please? I am preparing for an exam.",
            "দয়া করে ভলিউমটা একটু কমাবেন কি? আমি পরীক্ষার প্রস্তুতি নিচ্ছি।",
            "Listen carefully and pick the exact sentence you heard:",
            "মনোযোগ দিয়ে শুনুন এবং ঠিক যে বাক্যটি শুনেছেন তা বেছে নিন:",
            listOf(
                "Could you turn up the volume, please? I am preparing for an exam.",
                "Could you turn down the volume, please? I am preparing for an exam.",
                "Could you turn off the light, please? I am preparing for an exam.",
                "Would you turn down the music, please? I finished my exam."
            ),
            "Could you turn down the volume, please? I am preparing for an exam."
        ),
        ListeningExercise(
            "lc2", ListeningMode.CHOOSE, "Tense Contrast", "টেন্সের পার্থক্য", "A2",
            "She has lived in this neighborhood since she graduated from college.",
            "কলেজ থেকে স্নাতক শেষ করার পর থেকে সে এই এলাকায় বাস করছে।",
            "Listen carefully and pick the exact sentence you heard:",
            "মনোযোগ দিয়ে শুনুন এবং ঠিক যে বাক্যটি শুনেছেন তা বেছে নিন:",
            listOf(
                "She lived in this neighborhood when she graduated from college.",
                "She is living in this neighborhood before college.",
                "She has lived in this neighborhood since she graduated from college.",
                "She had lived in this neighborhood after college."
            ),
            "She has lived in this neighborhood since she graduated from college."
        ),
        ListeningExercise(
            "lc3", ListeningMode.CHOOSE, "Numbers & Time", "সংখ্যা ও সময়", "A2",
            "Flight four zero seven to Singapore is now boarding at gate fourteen.",
            "সিঙ্গাপুরগামী ফ্লাইট ৪০৭-এর বোর্ডিং এখন ১৪ নম্বর গেটে চলছে।",
            "Listen carefully and pick the exact sentence you heard:",
            "মনোযোগ দিয়ে শুনুন এবং ঠিক যে বাক্যটি শুনেছেন তা বেছে নিন:",
            listOf(
                "Flight four zero seven to Singapore is now boarding at gate forty.",
                "Flight four zero seven to Singapore is now boarding at gate fourteen.",
                "Flight four seven zero to Singapore is now boarding at gate fourteen.",
                "Flight four zero seven to Tokyo is now boarding at gate fourteen."
            ),
            "Flight four zero seven to Singapore is now boarding at gate fourteen."
        ),
        ListeningExercise(
            "lc4", ListeningMode.CHOOSE, "Conditional Meaning", "শর্তবাক্যের অর্থ", "B1",
            "If we leave before six o'clock, we will avoid the heavy evening traffic.",
            "আমরা যদি ছয়টার আগে বের হই, তবে সন্ধ্যার তীব্র যানজট এড়াতে পারব।",
            "Listen carefully and pick the exact sentence you heard:",
            "মনোযোগ দিয়ে শুনুন এবং ঠিক যে বাক্যটি শুনেছেন তা বেছে নিন:",
            listOf(
                "If we left before six o'clock, we would avoid the traffic.",
                "If we leave before six o'clock, we will avoid the heavy evening traffic.",
                "Unless we leave at six o'clock, we will enjoy the evening traffic.",
                "If we leave after six o'clock, we will avoid the heavy evening traffic."
            ),
            "If we leave before six o'clock, we will avoid the heavy evening traffic."
        ),

        // Dictation
        ListeningExercise(
            "ld1", ListeningMode.DICTATION, "Daily Habit", "দৈনন্দিন অভ্যাস", "A1",
            "I drink a glass of warm water every morning.",
            "আমি প্রতিদিন সকালে এক গ্লাস কুসুম গরম পানি পান করি।",
            "Type the exact sentence you hear:",
            "আপনি যে বাক্যটি শুনছেন সেটি হুবহু লিখুন:",
            emptyList(),
            "I drink a glass of warm water every morning."
        ),
        ListeningExercise(
            "ld2", ListeningMode.DICTATION, "Past Event", "অতীত ঘটনা", "A2",
            "They visited the science museum last Saturday.",
            "গত শনিবার তারা বিজ্ঞান জাদুঘর পরিদর্শন করেছিল।",
            "Type the exact sentence you hear:",
            "আপনি যে বাক্যটি শুনছেন সেটি হুবহু লিখুন:",
            emptyList(),
            "They visited the science museum last Saturday."
        ),
        ListeningExercise(
            "ld3", ListeningMode.DICTATION, "Present Perfect", "পুরাঘটিত বর্তমান", "B1",
            "She has already completed three chapters of the book.",
            "সে ইতিমধ্যেই বইটির তিনটি অধ্যায় শেষ করেছে।",
            "Type the exact sentence you hear:",
            "আপনি যে বাক্যটি শুনছেন সেটি হুবহু লিখুন:",
            emptyList(),
            "She has already completed three chapters of the book."
        ),
        ListeningExercise(
            "ld4", ListeningMode.DICTATION, "Future Plan", "ভবিষ্যৎ পরিকল্পনা", "B1",
            "We will practice English listening skills every day.",
            "আমরা প্রতিদিন ইংরেজি শোনার দক্ষতা অনুশীলন করব।",
            "Type the exact sentence you hear:",
            "আপনি যে বাক্যটি শুনছেন সেটি হুবহু লিখুন:",
            emptyList(),
            "We will practice English listening skills every day."
        )
    )
}

private data class InspectedWord(
    val token: String,
    val cleanWord: String,
    val posTags: List<String>,
    val meaning: String
)

private fun inspectWord(rawToken: String): InspectedWord {
    val clean = rawToken.trim().trimEnd('.', ',', '!', '?', ';', ':').lowercase()
    val matches = if (clean.length >= 2) DictionaryData.searchRanked(clean, limit = 4) else emptyList()
    val enEntry = matches.filterIsInstance<DictEntry.En>().firstOrNull { it.entry.head.equals(clean, ignoreCase = true) }
        ?: matches.filterIsInstance<DictEntry.En>().firstOrNull()
    val pos = buildList {
        val rawPos = enEntry?.entry?.pos?.trim().orEmpty()
        if (rawPos.isNotEmpty()) add(rawPos)
        add("vocabulary")
    }.distinct()
    val bn = banglaGloss(clean).ifBlank { enEntry?.entry?.head?.let { banglaGloss(it) }.orEmpty() }
    val enGloss = cleanGloss(Gloss.lookup(clean)) ?: enEntry?.entry?.hi?.takeIf { it.isNotBlank() } ?: ""
    val combined = listOf(bn, enGloss).filter { it.isNotBlank() }.joinToString(" · ")
        .ifBlank { "Tap + to save '$clean' for spaced repetition · সংরক্ষণ করতে + চাপুন" }
    return InspectedWord(
        token = rawToken,
        cleanWord = clean,
        posTags = pos,
        meaning = combined
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListeningPracticeScreen(
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
    var selectedMode by remember { mutableStateOf(ListeningMode.COMPREHENSION) }
    var showModeBar by remember { mutableStateOf(false) }
    var slowMode by remember { mutableStateOf(false) }
    val pool = remember(selectedMode) { ListeningData.exercises.filter { it.mode == selectedMode } }
    var idx by remember(selectedMode) { mutableStateOf(0) }
    val item = pool[idx % pool.size]

    var showTranscript by remember(item.id) { mutableStateOf(selectedMode == ListeningMode.COMPREHENSION) }
    var onlyKnownWords by remember(item.id) { mutableStateOf(false) }
    var selectedToken by remember(item.id) { mutableStateOf<String?>(null) }
    var picked by remember(item.id) { mutableStateOf<String?>(null) }
    var dictationText by remember(item.id) { mutableStateOf("") }
    var dictationScore by remember(item.id) { mutableStateOf<Int?>(null) }

    val activeRate = if (slowMode) 0.65f else 0.90f
    val progressPct = ((idx % pool.size) + 1).toFloat() / pool.size.coerceAtLeast(1)
    val tokens = remember(item.id) { item.audioScript.split(" ").filter { it.isNotBlank() } }

    Column(
        modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Minimal Reference Header: Close X + Centered Title + Filter button
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
                    IconButton(onClick = onClose, modifier = Modifier.testTag("listening_back_button")) {
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
                    "Listening & Conversation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "শোনার ও সংলাপ অনুশীলন",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                shape = CircleShape,
                color = if (showModeBar) cs.primaryContainer else cs.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = { showModeBar = !showModeBar }) {
                    Icon(Icons.Filled.Tune, contentDescription = "Modes", tint = cs.onSurface)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Reference Progress Bar ("Progress: X / Y")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Progress: ${(idx % pool.size) + 1} / ${pool.size} (${item.level})",
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "সম্পন্ন: ${store.listeningCompleted}",
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progressPct },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            strokeCap = StrokeCap.Round,
            color = cs.primary,
            trackColor = cs.surfaceVariant
        )

        AnimatedVisibility(visible = showModeBar) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ListeningMode.entries.forEach { m ->
                    FilterChip(
                        selected = selectedMode == m,
                        onClick = { selectedMode = m },
                        label = { Text(m.label) }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Glowing Circular Speaker Halo + Overlapping Slow Speed Circle (Reference Right Screen)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                cs.primary.copy(alpha = 0.14f),
                                cs.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = cs.surface,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .size(104.dp)
                        .clickable {
                            speaker.setRate(activeRate)
                            speaker.speak(item.audioScript, "en")
                        }
                        .testTag("listening_play_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Audio (${String.format(Locale.US, "%.2f", activeRate)}x)",
                            tint = cs.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Overlapping slow-speed purple circular badge (snail/slow pace toggle)
                Surface(
                    shape = CircleShape,
                    color = if (slowMode) accents.tealHighlight else cs.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-18).dp, y = (-18).dp)
                        .size(46.dp)
                        .clickable {
                            slowMode = !slowMode
                            speaker.setRate(if (slowMode) 0.65f else 0.90f)
                            speaker.speak(item.audioScript, "en")
                        }
                        .testTag("listening_slow_toggle")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Speed,
                            contentDescription = "Slow Speed Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (slowMode) "0.65×" else "Slow",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Interactive Tappable Words Sentence Display (Reference Right Screen)
        if (showTranscript) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tokens.forEachIndexed { idxToken, raw ->
                    val clean = raw.trimEnd('.', ',', '!', '?', ';', ':').lowercase()
                    val isKnownSaved = "listen:$clean" in store.srs
                    val isSelected = selectedToken == raw
                    val isTealHighlight = idxToken == 1 || (clean.length >= 6 && idxToken % 5 == 1)
                    val faded = onlyKnownWords && !isKnownSaved && !isSelected

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelected -> cs.surface
                            isKnownSaved -> cs.primaryContainer.copy(alpha = 0.7f)
                            else -> Color.Transparent
                        },
                        border = if (isSelected) BorderStroke(1.2.dp, cs.primary) else null,
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier.clickable {
                            selectedToken = if (isSelected) null else raw
                            speaker.setRate(activeRate)
                            speaker.speak(clean.ifBlank { raw }, "en")
                        }
                    ) {
                        Text(
                            text = raw,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize = 18.sp,
                            fontWeight = if (isSelected || isTealHighlight) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                faded -> cs.onSurface.copy(alpha = 0.25f)
                                isTealHighlight && !isSelected -> accents.tealHighlight
                                isSelected -> cs.primary
                                else -> cs.onSurface
                            }
                        )
                    }
                }
            }

            // Floating Word Definition Inspector Card when a word is tapped
            val currentInspected = selectedToken?.let { remember(it) { inspectWord(it) } }
            AnimatedVisibility(visible = currentInspected != null) {
                currentInspected?.let { info ->
                    val cardId = "listen:${info.cleanWord}"
                    val saved = cardId in store.srs
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cs.surface),
                        border = BorderStroke(1.dp, accents.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        speaker.setRate(activeRate)
                                        speaker.speak(info.cleanWord, "en")
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak ${info.cleanWord}",
                                        tint = cs.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    info.cleanWord.ifBlank { info.token },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { selectedToken = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = "Dismiss card",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = if (saved) "Saved" else "Inspected",
                                    tint = if (saved) accents.tealHighlight else cs.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = cs.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(15.dp)
                                )
                                info.posTags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = cs.surfaceVariant
                                    ) {
                                        Text(
                                            tag,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cs.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    info.meaning,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (saved) accents.mint.container else Color.Transparent,
                                    border = BorderStroke(1.5.dp, if (saved) accents.tealHighlight else cs.onSurface),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { store.toggleCard(cardId) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (saved) Icons.Filled.Check else Icons.Filled.Add,
                                            contentDescription = "Save word to flashcards",
                                            tint = if (saved) accents.tealHighlight else cs.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = cs.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Text(
                                    "${item.title} · ${item.scriptBn}",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // "Only Words I Know" & Show/Hide Script Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (onlyKnownWords) "Showing All Words · সব শব্দ দেখান" else "Only Words I Know · জানা শব্দ",
                style = MaterialTheme.typography.labelLarge,
                color = cs.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        onlyKnownWords = !onlyKnownWords
                        showTranscript = true
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Text("·", color = cs.onSurfaceVariant)
            Text(
                if (showTranscript) "Hide Text · লেখা লুকান" else "Show Text · লেখা দেখুন",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { showTranscript = !showTranscript }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("listening_transcript_toggle")
            )
        }

        Spacer(Modifier.height(12.dp))

        // Question & Answer Section (Preserving Comprehension, Choose & Dictation)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surface),
            border = BorderStroke(1.dp, accents.cardBorder)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(item.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(item.questionBn, style = MaterialTheme.typography.bodyMedium, color = cs.primary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))

                if (item.mode == ListeningMode.DICTATION) {
                    OutlinedTextField(
                        value = dictationText,
                        onValueChange = { if (dictationScore == null) dictationText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listening_dictation_input"),
                        minLines = 2,
                        placeholder = { Text("Type every word you heard / যা শুনেছেন লিখুন…") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    if (dictationScore == null) {
                        Button(
                            onClick = {
                                val (pct, _) = SpeakingData.scorePronunciation(item.answer, dictationText)
                                dictationScore = pct
                                showTranscript = true
                                store.recordListeningCompleted(pct >= 75)
                            },
                            enabled = dictationText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("listening_check_dictation"),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Check Dictation · যাচাই করুন") }
                    } else {
                        val pct = dictationScore!!
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (pct >= 75) cs.tertiaryContainer else cs.errorContainer
                            )
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("Dictation Accuracy · নির্ভুলতা: $pct%", fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Original · মূল বাক্য: ${item.answer}", style = MaterialTheme.typography.bodyMedium)
                                Text(item.scriptBn, style = MaterialTheme.typography.bodySmall, color = cs.primary)
                            }
                        }
                    }
                } else {
                    val revealed = picked != null
                    item.options.forEach { opt ->
                        val isRight = opt == item.answer
                        val bg = when {
                            !revealed -> cs.surfaceVariant.copy(alpha = 0.55f)
                            isRight -> cs.tertiaryContainer
                            opt == picked -> cs.errorContainer
                            else -> cs.surfaceVariant.copy(alpha = 0.4f)
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !revealed) {
                                    picked = opt
                                    showTranscript = true
                                    store.recordListeningCompleted(isRight)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bg)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(opt, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                if (revealed && isRight) Text("✓", fontWeight = FontWeight.ExtraBold, color = cs.tertiary)
                                else if (revealed && opt == picked) Text("✗", fontWeight = FontWeight.ExtraBold, color = cs.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Signature Full-Width Purple Gradient Pill CTA ("Generate New / Next")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.horizontalGradient(listOf(accents.ctaStart, accents.ctaEnd)))
                .clickable { idx += 1 }
                .testTag("listening_next_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Generate New / Next · পরবর্তী",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
