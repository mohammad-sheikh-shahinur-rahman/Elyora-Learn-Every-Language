package com.itamadersomajinc.elyora

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

data class SpeakingPrompt(
    val id: String,
    val category: String,
    val sentence: String,
    val meaningBn: String,
    val phoneticTip: String,
    val focusSound: String,
    val tipBn: String
)

object SpeakingData {
    val categories = listOf("All", "Daily Conversation", "Minimal Pairs", "Interview & Office", "Travel", "Tongue Twisters")

    fun categoryBn(cat: String): String = when (cat) {
        "All" -> "সব"
        "Daily Conversation" -> "দৈনন্দিন কথোপকথন"
        "Minimal Pairs" -> "শব্দ-ধ্বনি পার্থক্য"
        "Interview & Office" -> "ইন্টারভিউ ও অফিস"
        "Travel" -> "ভ্রমণ"
        "Tongue Twisters" -> "টাং টুইস্টার"
        else -> cat
    }

    val prompts: List<SpeakingPrompt> = listOf(
        SpeakingPrompt("sp1", "Daily Conversation", "Could you please repeat that a little more slowly?", "দয়া করে ওটা আরেকটু ধীরে আবার বলবেন কি?", "/kʊd juː pliːz rɪˈpiːt ðæt/", "Polite rising intonation on requests", "ভদ্র অনুরোধের সময় বাক্যের শেষে সুর একটু ওপরে তুলুন।"),
        SpeakingPrompt("sp2", "Daily Conversation", "I have been practicing English every morning this week.", "এই সপ্তাহে আমি প্রতিদিন সকালে ইংরেজি অনুশীলন করছি।", "/aɪ hæv biːn ˈpræktɪsɪŋ ˈɪŋɡlɪʃ/", "Link 'have been' smoothly", "'have been' অংশটি আটকে না গিয়ে মসৃণভাবে একসাথে বলুন।"),
        SpeakingPrompt("sp3", "Daily Conversation", "What do you usually do on weekends with your family?", "আপনি সাধারণত সপ্তাহান্তে পরিবারের সাথে কী করেন?", "/wɒt duː juː ˈjuːʒuəli duː/", "Stress content words: usually, weekends, family", "মূল শব্দগুলোতে (usually, weekends, family) স্পষ্ট জোর দিন।"),
        SpeakingPrompt("sp4", "Daily Conversation", "Thank you so much for taking the time to help me.", "আমাকে সাহায্য করার জন্য সময় দেওয়ায় অনেক ধন্যবাদ।", "/θæŋk juː soʊ mʌtʃ/", "Place tongue gently between teeth for 'th' in Thank", "'Thank'-এর 'th' উচ্চারণে জিভের ডগা দাঁতের মাঝে হালকাভাবে রাখুন।"),
        SpeakingPrompt("sp5", "Minimal Pairs", "The ship is carrying sheep across the deep sea.", "জাহাজটি গভীর সমুদ্র পার হয়ে ভেড়া নিয়ে যাচ্ছে।", "/ʃɪp/ vs /ʃiːp/", "Short /ɪ/ in 'ship' vs long /iː/ in 'sheep'", "'ship'-এ ছোট ই-কার এবং 'sheep'-এ দীর্ঘ ঈ-কার উচ্চারণ করুন।"),
        SpeakingPrompt("sp6", "Minimal Pairs", "Please leave the live concert before it gets late.", "দয়া করে বেশি রাত হওয়ার আগেই লাইভ কনসার্ট থেকে বেরিয়ে এসো।", "/liːv/ vs /laɪv/ vs /lɪv/", "Contrast long /iː/ in leave with /aɪ/ in live", "'leave' (লিভ - দীর্ঘ) এবং 'live' (লাইভ) ধ্বনির পার্থক্য স্পষ্ট রাখুন।"),
        SpeakingPrompt("sp7", "Minimal Pairs", "He thought three thousand things through carefully.", "সে তিন হাজার বিষয় খুব সাবধানে ভেবে দেখেছে।", "/θ/ voiceless TH", "Clear voiceless /θ/ sound — avoid /t/ or /s/", "'th' উচ্চারণ 'ত' বা 'স'-এর বদলে বাতাস বের করে স্পষ্ট /θ/ বলুন।"),
        SpeakingPrompt("sp8", "Minimal Pairs", "Very fine violins vibrate with vivid volume.", "খুব সুন্দর বেহালাগুলো প্রাণবন্ত শব্দে কম্পিত হয়।", "/v/ vs /f/", "Upper teeth on lower lip: /v/ vibrates, /f/ is airy", "নিচের ঠোঁটে ওপরের দাঁত ছুঁয়ে 'V' কম্পনসহ এবং 'F' বাতাসসহ উচ্চারণ করুন।"),
        SpeakingPrompt("sp9", "Interview & Office", "My greatest strength is solving complex problems calmly under pressure.", "আমার সবচেয়ে বড় শক্তি হলো চাপের মধ্যেও শান্তভাবে জটিল সমস্যার সমাধান করা।", "/ˈstreŋθ/ · /ˈprɒbləmz/", "Pause briefly after 'strength' for executive clarity", "পেশাদার স্পষ্টতার জন্য 'strength'-এর পর সামান্য থামুন।"),
        SpeakingPrompt("sp10", "Interview & Office", "Let us schedule a quick follow-up meeting tomorrow afternoon.", "চলো আগামীকাল বিকেলে একটি সংক্ষিপ্ত ফলো-আপ মিটিং নির্ধারণ করি।", "/ˈskedʒuːl/ · /ˈfɒloʊ ʌp/", "Clear stress on SCHE-dule and AF-ter-noon", "SCHE-dule এবং AF-ter-noon অংশে স্পষ্ট জোর দিয়ে বলুন।"),
        SpeakingPrompt("sp11", "Interview & Office", "I am excited to contribute my skills to your engineering team.", "আপনাদের ইঞ্জিনিয়ারিং দলে আমার দক্ষতা কাজে লাগাতে আমি খুবই আগ্রহী।", "/ɪkˈsaɪtɪd/ · /kənˈtrɪbjuːt/", "Stress second syllable in con-TRI-bute", "'con-TRI-bute' শব্দে দ্বিতীয় অংশে (TRI) জোর দিন।"),
        SpeakingPrompt("sp12", "Travel", "Excuse me, which platform does the express train leave from?", "মাফ করবেন, এক্সপ্রেস ট্রেনটি কোন প্ল্যাটফর্ম থেকে ছাড়ে?", "/ɪkˈskjuːz miː/ · /ˈplætfɔːrm/", "Clear consonant cluster in 'express'", "'express' শব্দটির যুক্তবর্ণ স্পষ্টভাবে উচ্চারণ করুন।"),
        SpeakingPrompt("sp13", "Travel", "I would like to check in for my flight to London.", "আমি লন্ডনের ফ্লাইটের জন্য চেক-ইন করতে চাই।", "/aɪ wʊd laɪk tuː tʃek ɪn/", "Natural rhythm: I'd LIKE to CHECK in", "স্বাভাবিক ছন্দ: I'd LIKE to CHECK in।"),
        SpeakingPrompt("sp14", "Travel", "Could you recommend a good local restaurant nearby?", "কাছাকাছি কোনো ভালো স্থানীয় রেস্তোরাঁর পরামর্শ দিতে পারবেন কি?", "/ˌrekəˈmend/ · /ˈrestərɒnt/", "3 syllables in res-tau-rant (/ˈrestrɒnt/)", "'restaurant' শব্দটি স্পষ্ট ছন্দে বলুন।"),
        SpeakingPrompt("sp15", "Tongue Twisters", "She sells seashells by the seashore.", "সে সমুদ্রতীরে ঝিনুক বিক্রি করে।", "/ʃ/ vs /s/", "Alternate cleanly between 'sh' and 's'", "দ্রুত বলার সময় 'sh' (শ) এবং 's' (স) আলাদাভাবে বজায় রাখুন।"),
        SpeakingPrompt("sp16", "Tongue Twisters", "Peter Piper picked a peck of pickled peppers.", "পিটার পাইপার এক ঝুড়ি আচার করা মরিচ তুলল।", "Aspirated /p/", "Crisp burst of air on every initial 'p'", "প্রতিটি 'P' ধ্বনি হালকা বাতাসের ঝাপটাসহ স্পষ্ট বলুন।"),
        SpeakingPrompt("sp17", "Tongue Twisters", "Red lorry, yellow lorry, red lorry, yellow lorry.", "লাল লরি, হলুদ লরি, লাল লরি, হলুদ লরি।", "/r/ vs /l/", "Keep tongue curled for /r/ and touching ridge for /l/", "'R'-এর সময় জিভ বাঁকিয়ে এবং 'L'-এ তালুতে ছুঁয়ে বলুন।")
    )

    fun scorePronunciation(target: String, spoken: String): Pair<Int, List<Pair<String, Boolean>>> {
        val cleanTarget = target.split(" ").filter { it.isNotBlank() }
        val spokenSet = spoken.lowercase()
            .replace(Regex("[^a-z0-9'\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        val wordResults = cleanTarget.map { original ->
            val norm = original.lowercase().replace(Regex("[^a-z0-9']"), "")
            val matched = norm.isNotEmpty() && (norm in spokenSet || spokenSet.any { it.startsWith(norm.take(4)) && norm.length >= 4 })
            original to matched
        }
        val hits = wordResults.count { it.second }
        val pct = if (wordResults.isEmpty()) 0 else (hits * 100) / wordResults.size
        return pct to wordResults
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakingPracticeScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val ctx = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    val pool = remember(selectedCategory) {
        if (selectedCategory == "All") SpeakingData.prompts
        else SpeakingData.prompts.filter { it.category == selectedCategory }
    }
    var idx by remember(selectedCategory) { mutableStateOf(0) }
    val item = pool[idx % pool.size]

    var isListening by remember { mutableStateOf(false) }
    var spokenText by remember(item.id) { mutableStateOf("") }
    var accuracy by remember(item.id) { mutableStateOf<Int?>(null) }
    var wordChecks by remember(item.id) { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var statusMessage by remember(item.id) { mutableStateOf<String?>(null) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(ctx) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                recognizer?.stopListening()
                recognizer?.destroy()
            } catch (_: Exception) {
            }
            recognizer = null
        }
    }

    fun evaluateSpoken(heard: String) {
        spokenText = heard
        val (pct, checks) = SpeakingData.scorePronunciation(item.sentence, heard)
        accuracy = pct
        wordChecks = checks
        statusMessage = null
        store.recordSpeakingCompleted(pct)
    }

    val fallbackIntentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val matches = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val heard = matches?.firstOrNull().orEmpty()
        if (heard.isNotBlank()) {
            evaluateSpoken(heard)
        } else {
            statusMessage = "No speech detected · কোনো কথা শোনা যায়নি। আবার বলুন অথবা Self-Check চাপুন।"
        }
    }

    fun startSpeechCapture() {
        speaker.stop()
        statusMessage = null
        if (!recognizerAvailable) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Read aloud: ${item.sentence}")
                }
                fallbackIntentLauncher.launch(intent)
            } catch (_: Exception) {
                statusMessage = "Speech recognition unavailable on this device · ডিভাইসে ভয়েস রিকগনিশন নেই। শুনে জোরে বলুন এবং Self-Check চাপুন।"
            }
            return
        }

        try {
            recognizer?.destroy()
            val sr = SpeechRecognizer.createSpeechRecognizer(ctx)
            recognizer = sr
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    statusMessage = "Listening… speak clearly now / শুনছি… স্পষ্টভাবে বলুন।"
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    statusMessage = "Could not capture speech (code $error) · আবার চেষ্টা করুন অথবা Self-Check ব্যবহার করুন।"
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val heard = matches?.firstOrNull().orEmpty()
                    if (heard.isNotBlank()) evaluateSpoken(heard)
                    else statusMessage = "No words recognized · শব্দ বোঝা যায়নি, আবার চেষ্টা করুন।"
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val p = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!p.isNullOrBlank()) spokenText = p
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            sr.startListening(intent)
        } catch (_: Exception) {
            isListening = false
            statusMessage = "Speech recognizer unavailable · অডিও শুনে Self-Check ব্যবহার করুন।"
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startSpeechCapture()
        else statusMessage = "Microphone permission denied · মাইক্রোফোন অনুমতি না থাকায় Self-Check ব্যবহার করুন।"
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("speaking_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Speaking Practice · বলার অনুশীলন", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("Offline STT + Sherpa-ONNX Ready · সম্পন্ন: ${store.speakingCompleted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeakingData.categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("$cat · ${SpeakingData.categoryBn(cat)}") }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary) {
                        Text(
                            "${item.category} · ${SpeakingData.categoryBn(item.category)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("${(idx % pool.size) + 1} / ${pool.size}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "\"${item.sentence}\"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    item.meaningBn,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(item.phoneticTip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Focus · মনোযোগ: ${item.focusSound}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                Text(item.tipBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            speaker.setRate(0.95f)
                            speaker.speak(item.sentence, "en")
                        },
                        modifier = Modifier.weight(1f).testTag("speaking_listen_normal"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Listen · শুনুন")
                    }
                    OutlinedButton(
                        onClick = {
                            speaker.setRate(0.62f)
                            speaker.speak(item.sentence, "en")
                        },
                        modifier = Modifier.weight(1f).testTag("speaking_listen_slow"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Slow · ধীরে")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isListening) {
                    recognizer?.stopListening()
                    isListening = false
                } else {
                    val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (granted) startSpeechCapture()
                    else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("speaking_mic_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(if (isListening) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                if (isListening) "Stop Listening · থামুন" else "Tap to Speak · বলুন ও যাচাই করুন",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val checks = item.sentence.split(" ").filter { it.isNotBlank() }.map { it to true }
                spokenText = item.sentence
                accuracy = 100
                wordChecks = checks
                statusMessage = "Self-check recorded! · নিজে বলার অনুশীলন রেকর্ড হয়েছে!"
                store.recordSpeakingCompleted(100)
            },
            modifier = Modifier.fillMaxWidth().testTag("speaking_self_check_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("I Spoke Aloud · জোরে বলেছি (+12 XP)")
        }

        if (!statusMessage.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(statusMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        if (accuracy != null) {
            val pct = accuracy!!
            Spacer(Modifier.height(14.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pct >= 75) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text("Pronunciation Match · নির্ভুলতা: $pct%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                    if (spokenText.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Heard · শোনা গেছে: \"$spokenText\"", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        wordChecks.forEach { (w, ok) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    w,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { idx = if (idx == 0) pool.lastIndex else idx - 1 },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Previous · পূর্ববর্তী") }
            Button(
                onClick = { idx += 1 },
                modifier = Modifier.weight(1f).testTag("speaking_next_button"),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Next · পরবর্তী") }
        }
        Spacer(Modifier.height(24.dp))
    }
}
