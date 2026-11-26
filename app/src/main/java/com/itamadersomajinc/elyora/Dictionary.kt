package com.itamadersomajinc.elyora

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DictionaryScreen(store: Store, speaker: Speaker, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    var q by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf("A") }
    var selectedPos by remember { mutableStateOf("All") }
    val query = q.trim()
    var ready by remember { mutableStateOf(DictionaryData.loaded) }
    var results by remember { mutableStateOf<List<DictEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (!DictionaryData.loaded) delay(120)
        Gloss.ensureBn()
        ready = true
    }

    LaunchedEffect(query, store.target, ready) {
        if (!ready) return@LaunchedEffect
        if (query.isEmpty()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(120)
        val ftsMatches = container.repository.searchVocabFts(query, limit = 60).map { DictEntry.En(it) }
        val rankedMatches = DictionaryData.searchRanked(query, limit = 120)
        val seenEnHeads = HashSet<String>()
        val merged = ArrayList<DictEntry>(140)
        (ftsMatches + rankedMatches).forEach { entry ->
            val key = when (entry) {
                is DictEntry.En -> "en:${entry.entry.head.lowercase()}|${entry.entry.pos}"
                is DictEntry.Ja -> "ja:${entry.entry.kanji}|${entry.entry.kana}"
                is DictEntry.App -> "app:${entry.word.id}"
            }
            if (seenEnHeads.add(key)) merged.add(entry)
        }
        results = merged
        if (store.target != "ja" && store.target != "en") {
            val t = store.target
            val extra = TargetContent.words(t)
                .filter { key ->
                    key.kana.contains(query, ignoreCase = true) ||
                        key.en.contains(query, ignoreCase = true) ||
                        key.hi.contains(query) ||
                        banglaGloss(key.en).contains(query)
                }
                .take(60)
                .map { DictEntry.App(it) }
            results = extra + results
        }
    }

    val browseList = remember(ready, selectedLetter, selectedPos) {
        if (!ready) emptyList()
        else DictionaryData.enAll.asSequence()
            .filter { e ->
                e.head.startsWith(selectedLetter, ignoreCase = true) &&
                    (selectedPos == "All" || e.pos.equals(selectedPos, ignoreCase = true))
            }
            .take(250)
            .toList()
    }

    val wordOfDay = remember(ready) {
        if (!ready || DictionaryData.enAll.isEmpty()) null
        else {
            val idx = ((store.todayEpochDay() * 97L) % DictionaryData.enAll.size).toInt().coerceAtLeast(0)
            DictionaryData.enAll[idx]
        }
    }

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Vocabulary · শব্দভান্ডার", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(
            if (ready) "${DictionaryData.enAll.size} words · ইংরেজি, বাংলা ও ${languageLabel(store.target)} অর্থ"
            else "Loading 25,000+ words · শব্দভান্ডার লোড হচ্ছে…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth().testTag("vocab_search_input"),
            singleLine = true,
            placeholder = { Text("Search words / বাংলা বা ইংরেজিতে শব্দ খুঁজুন…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = if (q.isNotEmpty()) {
                {
                    IconButton(onClick = { q = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (!ready) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading 25,000+ vocabulary… লোড হচ্ছে…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Column
        }

        if (query.isNotEmpty()) {
            if (results.isEmpty()) {
                Text(
                    "No matches for \"$q\" · কোনো ফলাফল পাওয়া যায়নি।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("${results.size} results · ফলাফল \"$q\"", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(results, key = { keyOf(it) }) { r ->
                        when (r) {
                            is DictEntry.En -> EnResultCard(r.entry, store, speaker)
                            is DictEntry.Ja -> JaResultCard(r.entry, store, speaker)
                            is DictEntry.App -> AppResultCard(r.word, store, speaker)
                        }
                    }
                }
            }
            return@Column
        }

        wordOfDay?.let { wod ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Word of the Day · আজকের শব্দ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(wod.head, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        val meaning = nativeMeaningForEntry(wod, store.nativeLang)
                        if (meaning.isNotEmpty()) {
                            Text(meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    FilledIconButton(onClick = { speakIn(store, speaker, wod.head, "en") }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear word of the day", modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // POS Filter
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "n", "v", "adj", "adv").forEach { pos ->
                val label = when (pos) {
                    "n" -> "Noun · বিশেষ্য"
                    "v" -> "Verb · ক্রিয়া"
                    "adj" -> "Adj · বিশেষণ"
                    "adv" -> "Adv · ক্রিয়াবিশেষণ"
                    else -> "All · সব"
                }
                FilterChip(
                    selected = selectedPos == pos,
                    onClick = { selectedPos = pos },
                    label = { Text(label) }
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // A-Z Letter Strip
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ('A'..'Z').forEach { ch ->
                val s = ch.toString()
                FilterChip(
                    selected = selectedLetter == s,
                    onClick = { selectedLetter = s },
                    label = { Text(s, fontWeight = FontWeight.Bold) }
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        Text(
            "Starting with \"$selectedLetter\" (${browseList.size}) · \"$selectedLetter\" দিয়ে শব্দ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(browseList, key = { "b:" + it.head + "|" + it.pos + "|" + it.hi }) { e ->
                EnResultCard(e, store, speaker)
            }
        }
    }
}

private fun nativeMeaningForEntry(e: EnEntry, native: String): String {
    val bn = banglaGloss(e.head)
    val other = when (native) {
        "bn", "en" -> ""
        "hi" -> e.hi
        else -> cleanGloss(Gloss.lookup(e.head)) ?: Gloss.lookup(e.head) ?: ""
    }
    return when {
        bn.isNotEmpty() && other.isNotEmpty() && !hasBengaliScript(other) -> "$bn · $other"
        bn.isNotEmpty() -> bn
        other.isNotEmpty() -> other
        else -> if (e.pos.isNotEmpty()) "(${e.pos}) English vocabulary" else "English vocabulary"
    }
}

private fun keyOf(r: DictEntry): String = when (r) {
    is DictEntry.En -> "e:" + r.entry.head + r.entry.pos + r.entry.hi
    is DictEntry.Ja -> "j:" + r.entry.kanji + "|" + r.entry.kana + r.entry.gloss
    is DictEntry.App -> "w:" + r.word.id
}

@Composable
private fun EnResultCard(e: EnEntry, store: Store, speaker: Speaker) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.head, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (e.pos.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(e.pos, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    nativeMeaningForEntry(e, store.nativeLang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            FilledIconButton(
                onClick = { speakIn(store, speaker, e.head, "en") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear word", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun JaResultCard(j: JaEntry, store: Store, speaker: Speaker) {
    val meaning = remember(j.gloss, store.nativeLang) {
        val base = if (store.nativeLang == "en") j.gloss else Gloss.lookup(j.gloss) ?: j.gloss
        appendBangla(base, j.gloss)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(j.kanji.ifEmpty { j.kana }, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    if (j.common) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("common · প্রচলিত", Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
                Text(
                    listOfNotNull(j.kana.ifEmpty { null }, j.romaji.ifEmpty { null }).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2
                )
                if (j.pos.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(j.pos, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledIconButton(
                onClick = { speakIn(store, speaker, j.kana.ifEmpty { j.kanji }, "ja") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear Japanese word", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AppResultCard(w: Word, store: Store, speaker: Speaker) {
    val targetJa = w.kana.isNotEmpty()
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(w.emoji, fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (targetJa) w.kana else w.en, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (targetJa) listOfNotNull(w.romaji.ifEmpty { null }, w.kanji.ifEmpty { null }, w.en.ifEmpty { null }).joinToString(" · ")
                    else w.ipa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(w.glossFor(store.nativeLang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            FilledIconButton(
                onClick = {
                    speakIn(
                        store,
                        speaker,
                        if (targetJa) w.kana else w.en,
                        if (w.lang != "ja" && w.lang != "en") w.lang else if (targetJa) "ja" else "en"
                    )
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear word", modifier = Modifier.size(16.dp))
            }
        }
    }
}
