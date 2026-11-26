package com.itamadersomajinc.elyora

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class VerbEntry(
    val base: String,
    val v2: String,
    val v3: String,
    val v4: String,
    val irregular: Boolean,
    val hi: String
)

object Verbs {

    @Volatile
    private var cache: List<VerbEntry>? = null

    @Volatile
    private var cachedNative: String = ""

    @Volatile
    private var meaningCache: Map<String, String> = emptyMap()

    val all: List<VerbEntry> get() = cache ?: emptyList()

    val loaded: Boolean get() = cache != null

    fun init(context: Context) {
        if (cache != null) return
        synchronized(this) {
            if (cache != null) return
            cache = try {
                context.assets.open("verbs.tsv").bufferedReader().use { r ->
                    r.lineSequence().filter { it.isNotBlank() }.mapNotNull { line ->
                        val c = line.split("\t")
                        if (c.size < 4) return@mapNotNull null
                        VerbEntry(
                            base = c[0],
                            v2 = c.getOrElse(1) { "" },
                            v3 = c.getOrElse(2) { "" },
                            v4 = c.getOrElse(3) { "" },
                            irregular = c.getOrElse(4) { "0" } == "1",
                            hi = c.getOrElse(5) { "" }
                        )
                    }.toList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun resolve(e: VerbEntry, native: String): String {
        Gloss.ensureBn()
        val bn = banglaGloss(e.base)
        val extra = when {
            native == "bn" || native == "en" || native.isBlank() -> ""
            native == "hi" -> cleanGloss(e.hi) ?: e.hi
            else -> cleanGloss(Gloss.lookup(e.base)) ?: Gloss.lookup(e.base) ?: ""
        }
        return when {
            bn.isNotEmpty() && extra.isNotEmpty() && !hasBengaliScript(extra) -> "$bn · $extra"
            bn.isNotEmpty() -> bn
            else -> extra
        }
    }

    fun meaning(e: VerbEntry, native: String): String {
        if (native == cachedNative && meaningCache.isNotEmpty()) {
            val c = meaningCache[e.base]
            if (c != null) return c
        }
        return resolve(e, native)
    }

    fun precompute(native: String) {
        Gloss.ensureBn()
        if (native == cachedNative && meaningCache.isNotEmpty()) return
        val m = HashMap<String, String>(5200)
        for (e in all) {
            val r = resolve(e, native)
            if (r.isNotEmpty()) m[e.base] = r
        }
        meaningCache = m
        cachedNative = native
    }

    private fun rank(e: VerbEntry, q: String, native: String): Int = when {
        e.base == q -> 0
        e.base.startsWith(q) -> 1
        e.v2.lowercase() == q || e.v3.lowercase() == q || e.v4.lowercase() == q -> 2
        e.base.contains(q) -> 3
        e.v2.lowercase().startsWith(q) || e.v3.lowercase().startsWith(q) || e.v4.lowercase().startsWith(q) -> 4
        e.v2.lowercase().contains(q) || e.v3.lowercase().contains(q) || e.v4.lowercase().contains(q) -> 5
        meaning(e, native).lowercase().startsWith(q) -> 6
        else -> 7
    }

    fun search(query: String, native: String, limit: Int = 200): List<VerbEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val hits = ArrayList<VerbEntry>(limit)
        for (e in all) {
            val hasEn = e.base.contains(q) || e.v2.lowercase().contains(q) ||
                e.v3.lowercase().contains(q) || e.v4.lowercase().contains(q)
            val hasNative = meaning(e, native).lowercase().contains(q)
            if (hasEn || hasNative) hits.add(e)
        }
        return hits.sortedWith(
            compareBy(
                { rank(it, q, native) },
                { it.base.length }
            )
        ).take(limit)
    }
}

@Composable
fun WordsScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    initialShowVerbs: Boolean = false
) {
    var showVerbs by remember(initialShowVerbs) { mutableStateOf(initialShowVerbs) }
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showVerbs,
                onClick = { showVerbs = false },
                label = { Text("Vocabulary · শব্দভান্ডার") },
                modifier = Modifier.testTag("words_tab_vocab")
            )
            FilterChip(
                selected = showVerbs,
                onClick = { showVerbs = true },
                label = { Text("Verbs · ক্রিয়া") },
                modifier = Modifier.testTag("words_tab_verbs")
            )
        }
        if (showVerbs) VerbsScreen(store, speaker, Modifier.weight(1f))
        else DictionaryScreen(store, speaker, Modifier.weight(1f))
    }
}

private enum class VerbFilter(val label: String) {
    ALL("All · সব"),
    IRREGULAR("Irregular · অনিয়মিত"),
    REGULAR("Regular · নিয়মিত"),
    QUIZ("Quiz · কুইজ")
}

@Composable
fun VerbsScreen(store: Store, speaker: Speaker, modifier: Modifier = Modifier) {
    var q by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(VerbFilter.ALL) }
    var ready by remember { mutableStateOf(Verbs.loaded) }
    val query = q.trim()
    val native = store.nativeLang

    LaunchedEffect(native) {
        while (!Verbs.loaded) delay(120)
        withContext(Dispatchers.Default) { Verbs.precompute(native) }
        ready = true
    }

    val irregularCount = remember(ready) { if (ready) Verbs.all.count { it.irregular } else 0 }
    val regularCount = remember(ready) { if (ready) Verbs.all.size - irregularCount else 0 }

    val displayed = remember(query, filter, native, ready) {
        if (!ready || filter == VerbFilter.QUIZ) emptyList()
        else {
            val baseList = if (query.isNotEmpty()) Verbs.search(query, native, limit = 250)
            else Verbs.all
            when (filter) {
                VerbFilter.IRREGULAR -> baseList.filter { it.irregular }.take(300)
                VerbFilter.REGULAR -> baseList.filter { !it.irregular }.take(300)
                VerbFilter.ALL -> baseList.take(300)
                VerbFilter.QUIZ -> emptyList()
            }
        }
    }

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Verb Master · ক্রিয়া মাস্টার", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(
            if (ready) "${Verbs.all.size} verbs · $irregularCount অনিয়মিত · $regularCount নিয়মিত"
            else "Loading verb library · ক্রিয়া তালিকা লোড হচ্ছে…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VerbFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.label) },
                    modifier = Modifier.testTag("verb_filter_${f.name.lowercase()}")
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        if (!ready) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading 4,400+ verbs · লোড হচ্ছে…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Column
        }

        if (filter == VerbFilter.QUIZ) {
            VerbQuizPane(store, speaker, Modifier.weight(1f))
            return@Column
        }

        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth().testTag("verb_search_input"),
            singleLine = true,
            placeholder = { Text("Search V1–V4 or বাংলা অর্থ: go, যাওয়া…") },
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
        Spacer(Modifier.height(8.dp))

        if (displayed.isEmpty()) {
            Text(
                "No verbs match \"$q\" · কোনো ক্রিয়া পাওয়া যায়নি।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                if (query.isEmpty()) "Showing ${displayed.size} (${filter.label}) · মোট ${Verbs.all.size}"
                else "${displayed.size} match(es) · ফলাফল \"$q\"",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(displayed, key = { it.base }) { e ->
                    VerbCard(e, native, store, speaker)
                }
            }
        }
    }
}

@Composable
private fun VerbQuizPane(store: Store, speaker: Speaker, modifier: Modifier = Modifier) {
    var irregularOnly by remember { mutableStateOf(true) }
    var roundKey by remember { mutableStateOf(0) }
    val pool = remember(irregularOnly, roundKey) {
        val src = if (irregularOnly) Verbs.all.filter { it.irregular && it.v2 != it.base } else Verbs.all
        src.filter { it.v2.isNotBlank() && it.v3.isNotBlank() }.shuffled().take(10)
    }
    var idx by remember(roundKey, irregularOnly) { mutableStateOf(0) }
    var score by remember(roundKey, irregularOnly) { mutableStateOf(0) }
    var picked by remember(roundKey, irregularOnly) { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = irregularOnly, onClick = { irregularOnly = true }, label = { Text("Irregular · অনিয়মিত") })
            FilterChip(selected = !irregularOnly, onClick = { irregularOnly = false }, label = { Text("All Verbs · সব") })
        }
        Spacer(Modifier.height(10.dp))

        if (pool.isEmpty()) {
            Text("Loading verbs… ক্রিয়া লোড হচ্ছে।")
            return@Column
        }

        if (idx >= pool.size) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Verb Quiz Complete! · কুইজ সম্পন্ন!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Score · স্কোর: $score / ${pool.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { roundKey += 1 }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Play New Round · আবার খেলুন")
            }
            return@Column
        }

        val v = pool[idx]
        val askV3 = idx % 2 == 1
        val answer = if (askV3) v.v3 else v.v2
        val options = remember(v.base, askV3) {
            val distractors = listOf(
                v.base + "ed",
                v.base + "d",
                v.v4,
                if (askV3) v.v2 else v.v3,
                v.base + "s",
                v.base + "en"
            ).filter { it != answer && it.isNotBlank() }.distinct().take(3)
            (distractors + answer).shuffled()
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Verb ${idx + 1} / ${pool.size} · Score: $score", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (askV3) "Pick V3 (Past Participle) · ৩য় রূপ বেছে নিন:" else "Pick V2 (Past Simple) · ২য় রূপ বেছে নিন:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(v.base, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                val m = Verbs.meaning(v, store.nativeLang)
                if (m.isNotEmpty()) {
                    Text(m, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val revealed = picked != null
        options.forEach { opt ->
            val isRight = opt == answer
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
                        store.recordAnswer(isRight, xpOnCorrect = 6)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bg)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(opt, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    if (revealed && isRight) Text("✓", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                    else if (revealed && opt == picked) Text("✗", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (revealed) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${v.base} → ${v.v2} → ${v.v3} → ${v.v4}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                FilledIconButton(onClick = { speakIn(store, speaker, "${v.base}, ${v.v2}, ${v.v3}", "en") }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear forms", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { idx += 1; picked = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text(if (idx < pool.lastIndex) "Next Verb · পরবর্তী ক্রিয়া" else "Finish · সম্পন্ন")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun VerbCard(e: VerbEntry, native: String, store: Store, speaker: Speaker) {
    val meaning = Verbs.meaning(e, native)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (e.irregular) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e.base, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (e.irregular) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                if (e.irregular) "Irregular · অনিয়মিত" else "Regular · নিয়মিত",
                                Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (meaning.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
                FilledIconButton(
                    onClick = { speakIn(store, speaker, "${e.base}, ${e.v2}, ${e.v3}", "en") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear verb forms", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FormPill("V1", e.base, Modifier.weight(1f))
                FormPill("V2", e.v2, Modifier.weight(1f))
                FormPill("V3", e.v3, Modifier.weight(1f))
                FormPill("V4", e.v4, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FormPill(tag: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
