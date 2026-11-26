package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReviewScreen(store: Store, speaker: Speaker, modifier: Modifier = Modifier) {
    var session by remember { mutableStateOf<List<Word>?>(null) }

    val pool = if (store.target == "ja" || store.target == "en") Data.allWords
    else TargetContent.words(store.target)

    fun loadDue(): List<Word> =
        store.dueCards(25).mapNotNull { (id, _) ->
            pool.firstOrNull { it.id == id } ?: Data.allWords.firstOrNull { it.id == id }
        }

    if (session == null) {
        ReviewHome(
            store = store,
            modifier = modifier,
            onStart = { session = loadDue().ifEmpty { null } },
            onAddNew = {
                val known = store.srs.keys
                val fresh = pool.filter { it.id !in known }.shuffled(java.util.Random()).take(10)
                store.addNewCards(fresh.map { it.id })
                session = loadDue().ifEmpty { null }
            }
        )
    } else {
        ReviewSession(session!!, store, speaker, modifier, onFinish = { session = null })
    }
}

@Composable
private fun ReviewHome(
    store: Store,
    modifier: Modifier,
    onStart: () -> Unit,
    onAddNew: () -> Unit
) {
    val due = store.dueCount()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Review · রিভিশন", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            "Spaced repetition — মনে রাখার বিজ্ঞানসম্মত পদ্ধতি।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧠", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("$due words due · $due টি বাকি", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text(
                    if (due > 0) "Perfect time to review — এখনই রিভিশন করলে সহজেই মনে থাকবে।"
                    else "All caught up! সব শেষ হয়েছে — নতুন শব্দ যোগ করুন বা পাঠ শুরু করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = due > 0
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Start review · রিভিশন শুরু করুন")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add 10 new words · ১০টি নতুন শব্দ")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("How it works · এটি যেভাবে কাজ করে", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Every word you learn gets its own schedule. Get it right and it comes back in a day, " +
                        "then a week, then a month. Get it wrong and it returns in 10 minutes.\n\n" +
                        "প্রতিটি শব্দের জন্য আলাদা রিভিশন সময় নির্ধারিত হয়। সঠিক উত্তর দিলে ১ দিন, তারপর ১ সপ্তাহ ও ১ মাস পর আসবে; ভুল হলে ১০ মিনিট পর আবার আসবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReviewSession(
    queue: List<Word>,
    store: Store,
    speaker: Speaker,
    modifier: Modifier,
    onFinish: () -> Unit
) {
    BackHandler(onBack = onFinish)
    var index by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    val targetJa = store.target != "en"

    fun next() {
        if (index == queue.lastIndex) done = true else {
            index++
            revealed = false
        }
    }

    if (done) {
        Column(
            modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🌟", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text("Review complete! · রিভিশন সম্পন্ন!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "You reviewed ${queue.size} words. They'll return right before you'd forget them.\nআপনি ${queue.size}টি শব্দ রিভিশন করেছেন। ভুলে যাওয়ার আগেই এগুলো আবার আসবে।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Done · সম্পন্ন") }
        }
        return
    }

    val w = queue[index]
    LaunchedEffect(index) {
        speak(store, speaker, if (targetJa) w.kana else w.en, targetJa)
    }

    Column(modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { index / queue.size.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Card ${index + 1}/${queue.size} · কার্ড", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            Text(
                "${languageLabel(store.target)} → ${languageLabel(store.nativeLang)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(w.emoji, fontSize = 92.sp)
                Spacer(Modifier.height(16.dp))
                if (revealed) {
                    if (targetJa) {
                        Text(w.kana, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                        if (store.showRomaji) {
                            Spacer(Modifier.height(4.dp))
                            Text(w.romaji, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Text(w.en, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(w.ipa, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (store.showTranslations || store.nativeLang == "bn") {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            w.glossFor(store.nativeLang),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text("🔊 Listen… · শুনুন…", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "What does it mean? Tap reveal · অর্থ কী? নিচে দেখুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    FilledIconButton(onClick = { speak(store, speaker, if (targetJa) w.kana else w.en, targetJa) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear again · আবার শুনুন")
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))

        if (!revealed) {
            Button(
                onClick = { revealed = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reveal answer · উত্তর দেখুন") }
        } else {
            Text(
                "How well did you remember it? · কতটা মনে আছে?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GradeButton("Again\nআবার", Color(0xFFD64545), Modifier.weight(1f)) {
                    store.grade(w.id, 0)
                    next()
                }
                GradeButton("Hard\nকঠিন", Color(0xFFE8A33D), Modifier.weight(1f)) {
                    store.grade(w.id, 1)
                    next()
                }
                GradeButton("Good\nভালো", Color(0xFF2E9E5B), Modifier.weight(1f)) {
                    store.grade(w.id, 2)
                    next()
                }
                GradeButton("Easy\nসহজ", Color(0xFF2E7DBF), Modifier.weight(1f)) {
                    store.grade(w.id, 3)
                    next()
                }
            }
        }
    }
}

@Composable
private fun GradeButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
