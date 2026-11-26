package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

enum class WritingCategory(val label: String) {
    PARAGRAPH("Paragraph · অনুচ্ছেদ"),
    EMAIL("Email · ইমেইল"),
    ESSAY("Essay · রচনা")
}

data class WritingPrompt(
    val id: String,
    val category: WritingCategory,
    val level: String,
    val title: String,
    val titleBn: String,
    val task: String,
    val taskBn: String,
    val minWords: Int,
    val maxWords: Int,
    val outline: List<String>,
    val connectors: List<String>,
    val modelAnswer: String
)

object WritingData {
    val prompts: List<WritingPrompt> = listOf(
        // Paragraphs
        WritingPrompt(
            "wp1", WritingCategory.PARAGRAPH, "A1–A2",
            "My Daily Morning Routine", "আমার প্রতিদিনের সকালের রুটিন",
            "Write a clear paragraph describing what you do from the time you wake up until you start work or study.",
            "ঘুম থেকে ওঠা থেকে শুরু করে কাজ বা পড়াশোনা শুরু করা পর্যন্ত সকালে আপনি কী কী করেন তা নিয়ে একটি স্পষ্ট অনুচ্ছেদ লিখুন।",
            50, 90,
            listOf("Topic sentence: When you wake up · কখন ঘুম থেকে ওঠেন", "2–3 main morning habits (Present Simple) · ২-৩টি সকালের অভ্যাস", "Closing sentence: How you feel · দিনটি কেমনভাবে শুরু হয়"),
            listOf("First,", "After that,", "Usually,", "Before leaving,", "Finally,"),
            "I usually wake up at six thirty every morning to start my day calmly. First, I drink a tall glass of water and stretch for ten minutes. After that, I take a quick shower and enjoy a healthy breakfast with my family. Before leaving home, I review my daily study plan and check my messages. Finally, I walk to the bus stop feeling refreshed and ready to focus."
        ),
        WritingPrompt(
            "wp2", WritingCategory.PARAGRAPH, "A2–B1",
            "The Importance of Learning English", "ইংরেজি শেখার গুরুত্ব",
            "Explain why English is useful for education, career growth, and connecting with people globally.",
            "শিক্ষা, ক্যারিয়ারের উন্নতি এবং বিশ্বজুড়ে মানুষের সাথে যোগাযোগে ইংরেজি কেন গুরুত্বপূর্ণ তা ব্যাখ্যা করুন।",
            60, 110,
            listOf("Main idea sentence · মূল ভাবনা", "Educational & career benefit · শিক্ষা ও চাকরির সুবিধা", "Real-world example · বাস্তব উদাহরণ", "Concluding thought · উপসংহার"),
            listOf("Moreover,", "For example,", "In addition,", "Therefore,", "In conclusion,"),
            "Learning English opens doors to global education and career opportunities. In addition to reading international research and technical documentation, English helps professionals communicate with teams worldwide. For example, many software engineers and students collaborate online using English every day. Moreover, watching lectures and podcasts becomes effortless when your vocabulary grows. Therefore, dedicating fifteen minutes daily to English is one of the best investments anyone can make."
        ),
        WritingPrompt(
            "wp3", WritingCategory.PARAGRAPH, "B1",
            "A Memorable Journey", "একটি স্মরণীয় ভ্রমণ",
            "Describe a memorable trip you took using Past Simple and Past Continuous tenses.",
            "Past Simple ও Past Continuous টেন্স ব্যবহার করে আপনার জীবনের একটি স্মরণীয় ভ্রমণের বর্ণনা দিন।",
            60, 110,
            listOf("Where and when you went · কোথায় ও কখন গিয়েছিলেন", "What happened during the journey · যাত্রাপথে কী ঘটেছিল", "Why it remains unforgettable · কেন তা স্মরণীয়"),
            listOf("Last year,", "While we were", "Suddenly,", "Although", "Overall,"),
            "Last year, I traveled by train to the hills of Sylhet with two close friends. While we were looking out the window, gentle rain began falling over the endless green tea gardens. Suddenly, the morning mist cleared and revealed a golden sunrise behind the hills. Although the journey lasted nearly six hours, we laughed and shared stories the entire way. Overall, it remains the most peaceful trip I have ever experienced."
        ),

        // Emails
        WritingPrompt(
            "we1", WritingCategory.EMAIL, "A2–B1",
            "Formal Leave Application Email", "ছুটির জন্য অফিসিয়াল ইমেইল",
            "Write a polite email to your manager or teacher requesting two days of leave for a family event.",
            "পারিবারিক অনুষ্ঠানের জন্য দুই দিনের ছুটি চেয়ে আপনার ম্যানেজার বা শিক্ষকের কাছে একটি ভদ্র ইমেইল লিখুন।",
            60, 120,
            listOf("Subject line & Formal Greeting · বিষয় ও সম্ভাষণ", "Clear reason and exact dates · ছুটির কারণ ও তারিখ", "Plan to handle pending work · কাজের বিকল্প ব্যবস্থা", "Polite sign-off · বিনীত বিদায়"),
            listOf("Dear Mr./Ms.,", "I am writing to request", "During my absence,", "Thank you for your understanding.", "Best regards,"),
            "Subject: Leave Request — 14–15 November\n\nDear Mr. Rahman,\n\nI am writing to request two days of leave on 14 and 15 November due to an important family wedding. During my absence, I will ensure that all urgent tasks are completed ahead of schedule, and my colleague Sara has kindly agreed to assist with any immediate queries. I will remain reachable by email in case of emergencies.\n\nThank you very much for your understanding.\n\nBest regards,\nTariq Hasan"
        ),
        WritingPrompt(
            "we2", WritingCategory.EMAIL, "B1",
            "Job Application Follow-Up Email", "চাকরির ইন্টারভিউ পরবর্তী ফলো-আপ ইমেইল",
            "Write a concise, professional follow-up email one week after interviewing for a junior position.",
            "ইন্টারভিউয়ের এক সপ্তাহ পর একটি সংক্ষিপ্ত ও পেশাদার ফলো-আপ ইমেইল লিখুন।",
            60, 120,
            listOf("Subject & Greeting · বিষয় ও সম্ভাষণ", "Thank them for the interview · ধন্যবাদ জানানো", "Reiterate enthusiasm & key skill · আগ্রহ ও মূল দক্ষতা", "Professional closing · পেশাদার সমাপ্তি"),
            listOf("Dear Hiring Manager,", "Thank you for the opportunity", "I remain very enthusiastic", "Please let me know if", "Sincerely,"),
            "Subject: Following Up — Junior Developer Interview\n\nDear Hiring Manager,\n\nThank you for the opportunity to interview for the Junior Developer position last Tuesday. I truly enjoyed learning about your upcoming mobile projects and discussing how my Kotlin and problem-solving skills could support the team. I remain very enthusiastic about joining your company. Please let me know if you need any additional information from my side.\n\nSincerely,\nNadia Karim"
        ),
        WritingPrompt(
            "we3", WritingCategory.EMAIL, "A2",
            "Invitation Email to a Friend", "বন্ধুকে আমন্ত্রণ জানিয়ে ইমেইল",
            "Write a friendly informal email inviting a friend to dinner or a weekend hangout.",
            "রাতের খাবার বা সাপ্তাহিক আড্ডায় বন্ধুকে আমন্ত্রণ জানিয়ে একটি আন্তরিক ইমেইল লিখুন।",
            50, 100,
            listOf("Warm greeting · উষ্ণ অভিবাদন", "What the plan is (day, time, place) · দিন, সময় ও স্থান", "Ask them to reply · উত্তর জানাতে বলা"),
            listOf("Hi there,", "I hope you're doing great!", "Would you like to", "Let me know if", "See you soon,"),
            "Subject: Weekend Dinner Catch-Up!\n\nHi Arif,\n\nI hope you're doing great! It has been way too long since we last caught up. Would you like to come over to my place this Saturday evening at seven for homemade biryani and a movie? Let me know if Saturday works for you, and feel free to bring your brother along too!\n\nSee you soon,\nRafi"
        ),

        // Essays
        WritingPrompt(
            "wes1", WritingCategory.ESSAY, "B1–B2",
            "Smartphones: Blessing or Distraction?", "স্মার্টফোন: আশীর্বাদ নাকি মনোযোগ নষ্টের কারণ?",
            "Write a balanced short essay discussing both the benefits and drawbacks of smartphones, ending with your opinion.",
            "স্মার্টফোনের সুবিধা ও অসুবিধা উভয় দিক আলোচনা করে আপনার নিজস্ব মতামতসহ একটি ভারসাম্যপূর্ণ রচনা লিখুন।",
            110, 200,
            listOf("Introduction: Hook + thesis statement · ভূমিকা", "Body 1: Key advantages · মূল সুবিধাসমূহ", "Body 2: Main drawbacks · প্রধান অসুবিধাসমূহ", "Conclusion: Balanced final verdict · উপসংহার"),
            listOf("In today's digital era,", "On the one hand,", "On the other hand,", "However,", "To sum up,"),
            "In today's digital era, smartphones have become indispensable companions for billions of people. While some argue they waste valuable time, others view them as powerful tools for progress.\n\nOn the one hand, smartphones democratize education and communication. Learners can study languages, attend live lectures, and collaborate across continents right from their pockets. Instant navigation and mobile banking also save hours every week.\n\nOn the other hand, constant notifications can fragment concentration and reduce face-to-face conversation. Excessive late-night scrolling frequently harms sleep quality and mental focus.\n\nTo sum up, a smartphone is neither purely good nor purely bad. When managed with healthy screen limits, its educational benefits clearly outweigh its distractions."
        ),
        WritingPrompt(
            "wes2", WritingCategory.ESSAY, "B2",
            "Should Homework Be Replaced by Project Work?", "বাড়ির কাজের বদলে কি প্রজেক্ট ওয়ার্ক হওয়া উচিত?",
            "Present clear arguments for and against practical projects versus traditional daily homework.",
            "প্রচলিত বাড়ির কাজের তুলনায় ব্যবহারিক প্রজেক্টের পক্ষে ও বিপক্ষে যুক্তি উপস্থাপন করে একটি রচনা লিখুন।",
            110, 200,
            listOf("Introduction: State the debate clearly · বিতর্কের সূচনা", "Body 1: Why practical projects help · প্রজেক্টের উপকারিতা", "Body 2: Why regular short practice matters · প্রতিদিনের অনুশীলনের গুরুত্ব", "Conclusion: Recommend balanced approach · ভারসাম্যপূর্ণ সমাধান"),
            listOf("Education systems worldwide", "Firstly,", "Furthermore,", "Nevertheless,", "In conclusion,"),
            "Education systems worldwide continue to debate whether hands-on projects should replace traditional homework. Both approaches offer distinct strengths for student development.\n\nFirstly, project-based learning encourages creativity, teamwork, and real-world problem solving. When students build an app or research a local issue, they retain concepts much longer than through rote memorization. Furthermore, projects boost motivation and presentation skills.\n\nNevertheless, short daily exercises remain essential for foundational subjects like mathematics and grammar. Consistent repetition builds automatic accuracy that occasional group projects cannot guarantee alone.\n\nIn conclusion, schools should not abandon homework entirely; instead, blending brief daily drills with monthly creative projects prepares learners best for modern careers."
        )
    )

    fun analyzeOffline(text: String, minWords: Int, maxWords: Int, connectors: List<String>): List<String> {
        val tips = mutableListOf<String>()
        val trimmed = text.trim()
        val words = if (trimmed.isEmpty()) emptyList() else trimmed.split(Regex("\\s+"))
        val wc = words.size
        when {
            wc < minWords -> tips.add("Word count: $wc — লক্ষ্য ($minWords–$maxWords) ছুঁতে আরও ${minWords - wc}টি শব্দ যোগ করুন।")
            wc > maxWords + 30 -> tips.add("Word count: $wc — লেখাটি আরেকটু সংক্ষিপ্ত করার চেষ্টা করুন।")
            else -> tips.add("Great length! $wc words · দারুণ দৈর্ঘ্য ($minWords–$maxWords শব্দের ভেতর আছে)।")
        }
        if (trimmed.isNotEmpty() && !trimmed.first().isUpperCase()) {
            tips.add("Start with a capital letter · প্রথম অক্ষর বড় হাতের লিখুন।")
        }
        if (trimmed.isNotEmpty() && trimmed.last() !in listOf('.', '!', '?')) {
            tips.add("End with punctuation (. ! ?) · বাক্যের শেষে সঠিক বিরামচিহ্ন দিন।")
        }
        if (Regex("\\bi\\b").containsMatchIn(trimmed)) {
            tips.add("Capitalize 'I' · সর্বনাম 'I' সবসময় বড় হাতের লিখুন।")
        }
        val lower = trimmed.lowercase()
        if (lower.contains("doesn't goes") || lower.contains("didn't went") || lower.contains("can speaks")) {
            tips.add("Check verb base form · didn't/doesn't/can-এর পর মূল ভার্ব (V1) ব্যবহার করুন।")
        }
        val usedConnectors = connectors.count { c -> lower.contains(c.lowercase().trimEnd(',', '.')) }
        if (usedConnectors > 0) {
            tips.add("Nice cohesion! · চমৎকার! আপনি ${usedConnectors}টি সংযোগকারী শব্দ ব্যবহার করেছেন।")
        } else {
            tips.add("Add connectors · বাক্যের প্রবাহ বাড়াতে '${connectors.firstOrNull().orEmpty()}'-এর মতো সংযোগকারী শব্দ যোগ করুন।")
        }
        return tips
    }
}

@Composable
fun WritingPracticeScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    aiVm: AiGrammarViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf(WritingCategory.PARAGRAPH) }
    var openPrompt by remember { mutableStateOf<WritingPrompt?>(null) }

    if (openPrompt != null) {
        WritingEditorView(
            prompt = openPrompt!!,
            store = store,
            speaker = speaker,
            aiVm = aiVm,
            modifier = modifier,
            onClose = {
                aiVm.resetWritingReview()
                openPrompt = null
            }
        )
        return
    }

    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val filtered = remember(selectedCategory) { WritingData.prompts.filter { it.category == selectedCategory } }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("writing_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Writing Studio · লেখার অনুশীলন", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("Paragraph · Email · Essay · সম্পন্ন: ${store.completedWritings.size}/${WritingData.prompts.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WritingCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) },
                    modifier = Modifier.testTag("writing_tab_${cat.name.lowercase()}")
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        filtered.forEach { p ->
            val done = p.id in store.completedWritings
            val hasDraft = !store.savedWritings[p.id].isNullOrBlank()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { openPrompt = p }
                    .testTag("writing_prompt_${p.id}"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(p.level, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${p.minWords}–${p.maxWords} words · শব্দ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (done) {
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            } else if (hasDraft) {
                                Spacer(Modifier.width(6.dp))
                                Text("Draft saved · খসড়া সংরক্ষিত", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(p.titleBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(p.taskBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Open prompt")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WritingEditorView(
    prompt: WritingPrompt,
    store: Store,
    speaker: Speaker,
    aiVm: AiGrammarViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var draft by remember(prompt.id) { mutableStateOf(store.savedWritings[prompt.id] ?: "") }
    var showModel by remember(prompt.id) { mutableStateOf(false) }
    var localTips by remember(prompt.id) { mutableStateOf<List<String>>(emptyList()) }
    var savedBanner by remember(prompt.id) { mutableStateOf(false) }

    val aiState by aiVm.writingState.collectAsStateWithLifecycle()
    val wordCount = remember(draft) {
        val t = draft.trim()
        if (t.isEmpty()) 0 else t.split(Regex("\\s+")).size
    }
    val progress = (wordCount.toFloat() / prompt.minWords.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f)) {
                Text("${prompt.category.label} · ${prompt.level}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(prompt.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(prompt.titleBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(prompt.task, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(prompt.taskBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Structure Outline · লেখার কাঠামো:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    prompt.outline.forEachIndexed { i, step ->
                        Text("${i + 1}. $step", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Tap to insert connectors · সংযোগকারী শব্দ যোগ করুন:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prompt.connectors.forEach { conn ->
                    AssistChip(
                        onClick = {
                            draft = if (draft.isBlank() || draft.endsWith(" ") || draft.endsWith("\n")) "$draft$conn " else "$draft $conn "
                        },
                        label = { Text(conn) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it; savedBanner = false },
                modifier = Modifier.fillMaxWidth().testTag("writing_draft_input"),
                minLines = 7,
                placeholder = { Text("Write here / এখানে ইংরেজিতে লেখা শুরু করুন…") },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$wordCount / ${prompt.minWords}–${prompt.maxWords} words · শব্দ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(if (wordCount >= prompt.minWords) "Target met · লক্ষ্য পূর্ণ ✓" else "${prompt.minWords - wordCount} more · আরও বাকি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { localTips = WritingData.analyzeOffline(draft, prompt.minWords, prompt.maxWords, prompt.connectors) },
                    enabled = draft.isNotBlank(),
                    modifier = Modifier.weight(1f).testTag("writing_local_check"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Spellcheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Check · যাচাই")
                }
                Button(
                    onClick = {
                        store.saveWriting(prompt.id, draft, markDone = wordCount >= prompt.minWords / 2)
                        savedBanner = true
                    },
                    enabled = draft.isNotBlank(),
                    modifier = Modifier.weight(1f).testTag("writing_save_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save · সংরক্ষণ (+30 XP)")
                }
            }

            if (store.aiAssistantEnabled) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { aiVm.reviewWriting(prompt.title, prompt.category.label, draft, store.nativeLang) },
                    enabled = draft.isNotBlank() && aiState !is WritingReviewState.Loading,
                    modifier = Modifier.fillMaxWidth().testTag("writing_ai_review_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AI Grammar & Style Feedback · এআই ফিডব্যাক নিন")
                }
            }

            if (savedBanner) {
                Spacer(Modifier.height(8.dp))
                Text("Saved to Writing Portfolio · পোর্টফোলিওতে সংরক্ষিত হয়েছে ✓", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }

            if (localTips.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Writing Check · অফলাইন মূল্যায়ন", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                        localTips.forEach { t -> Text("• $t", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }

            when (val st = aiState) {
                is WritingReviewState.Loading -> {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("AI Tutor is reviewing… এআই পর্যালোচনা করছে…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is WritingReviewState.Success -> {
                    Spacer(Modifier.height(12.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("AI Tutor Feedback · এআই ফিডব্যাক", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Text(st.feedback, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is WritingReviewState.Error -> {
                    Spacer(Modifier.height(12.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(st.message, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                WritingReviewState.Idle -> {}
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showModel = !showModel },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (showModel) "Hide Model Answer · নমুনা উত্তর লুকান" else "View Model Answer · নমুনা উত্তর দেখুন")
            }

            if (showModel) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Model Answer · নমুনা উত্তর", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            FilledIconButton(
                                onClick = { speakIn(store, speaker, prompt.modelAnswer, "en") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Listen to model answer", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(prompt.modelAnswer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
