package com.itamadersomajinc.elyora

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    store: Store,
    speaker: Speaker,
    prefsData: AppPreferencesData,
    onSelectTheme: (ThemeMode) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onToggleOfflineVoice: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val accents = LocalElyoraAccents.current
    var showReset by remember { mutableStateOf(false) }
    var playUpdateStatus by remember { mutableStateOf<PlayUpdateStatus?>(null) }
    val playUpdateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    val weekXp = store.last7DaysXp()
    val maxDayXp = (weekXp.maxOfOrNull { it.second } ?: 1).coerceAtLeast(20)
    val peakXp = weekXp.maxOfOrNull { it.second } ?: 0
    val academyTotal = GrammarAcademyData.all.size.coerceAtLeast(210)
    val selectedQwen = remember(prefsData.selectedQwenModelId) { QwenModelCatalog.byId(prefsData.selectedQwenModelId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Learning Overview · অগ্রগতি ও প্রোফাইল", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(
            "Theme, XP analytics, Qwen AI & settings · থিম, স্ট্রিক ও সেটিংস",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Rounded-Pill 7-Day XP Activity Chart (inspired by mockup)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Weekly XP · ৭ দিনের অগ্রগতি", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Surface(shape = RoundedCornerShape(10.dp), color = accents.lavender.container) {
                        Text(
                            "${weekXp.sumOf { it.second }} XP",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = accents.lavender.content,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekXp.forEach { (dayLabel, xpVal) ->
                        val frac = (xpVal.toFloat() / maxDayXp).coerceIn(0.12f, 1f)
                        val isPeak = xpVal > 0 && xpVal == peakXp
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            if (isPeak) {
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.onSurface) {
                                    Text(
                                        "$xpVal XP",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.surface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            } else if (xpVal > 0) {
                                Text("$xpVal", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height((82 * frac).dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isPeak -> MaterialTheme.colorScheme.primary
                                            xpVal > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(dayLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Dual Gradient Highlight Cards (inspired by mockup)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(accents.heroStart, accents.heroEnd)))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Level ${store.level()}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${store.xp} XP", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(store.levelTitle(), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(accents.sky.badgeBg, accents.mint.badgeBg)))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Skill Master", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${store.accuracyPercent()}%", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("🔥 ${store.streak}d Streak · ধারা", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Light / Dark / System Theme Selector
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Appearance Theme · লাইট / ডার্ক থিম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Switch freely between Light, Dark, or System mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val icon = when (mode) {
                            ThemeMode.SYSTEM -> Icons.Filled.PhoneAndroid
                            ThemeMode.LIGHT -> Icons.Filled.LightMode
                            ThemeMode.DARK -> Icons.Filled.DarkMode
                        }
                        FilterChip(
                            selected = prefsData.themeMode == mode,
                            onClick = { onSelectTheme(mode) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("${mode.labelEn}\n${mode.labelBn}", fontSize = 11.sp, lineHeight = 13.sp) },
                            modifier = Modifier.weight(1f).testTag("theme_mode_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Detailed Skill Mastery Breakdown
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Learning Statistics · শেখার পরিসংখ্যান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SkillBar("Grammar Academy · গ্রামার একাডেমি", store.completedGrammar.size, academyTotal)
                SkillBar("12 Tenses Mastered · ১২ টেন্স", store.masteredTenses.size, 12)
                SkillBar("Writing Tasks · লেখার অনুশীলন", store.completedWritings.size, WritingData.prompts.size)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("📝", "${store.quizzesCompleted}", "Quiz · কুইজ")
                    StatBox("🧩", "${store.sentencesBuilt}", "Sent · বাক্য")
                    StatBox("🎙️", "${store.speakingCompleted}", "Speak · বলা")
                    StatBox("🎧", "${store.listeningCompleted}", "Listen · শোনা")
                }
                Text(
                    "Answers · উত্তর: ${store.totalAnswered} (${store.totalCorrect} সঠিক) · Words · সংরক্ষিত শব্দ: ${store.wordsLearnedCount()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // Offline Qwen AI, Reminder & Offline Speech Controls
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Offline AI & Background · অফলাইন এআই ও রিমাইন্ডার", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                SettingSwitch(
                    title = "Offline Qwen AI Tutor · অফলাইন এআই",
                    subtitle = "Active model: ${selectedQwen.displayName} (${selectedQwen.quant}) · সম্পূর্ণ অফলাইন",
                    checked = store.aiAssistantEnabled,
                    onCheckedChange = { store.setAiEnabled(it) },
                    modifier = Modifier.testTag("profile_ai_toggle")
                )
                SettingSwitch(
                    title = "Daily Study Reminder · দৈনিক রিমাইন্ডার",
                    subtitle = "WorkManager background reminder every 24h",
                    checked = prefsData.dailyReminderEnabled,
                    onCheckedChange = onToggleReminder,
                    modifier = Modifier.testTag("profile_reminder_toggle")
                )
                SettingSwitch(
                    title = "Prefer Offline TTS Voices · অফলাইন ভয়েস",
                    subtitle = "Prioritize on-device Text-to-Speech without internet",
                    checked = prefsData.offlineVoicePreferred,
                    onCheckedChange = onToggleOfflineVoice,
                    modifier = Modifier.testTag("profile_offline_voice_toggle")
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // Native Language
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Native Language · মাতৃভাষা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Bengali (বাংলা) & English are always shown · বাংলা ও ইংরেজি সবসময় দেখানো হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                NATIVE_LANGUAGES.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (code, name) ->
                            FilterChip(
                                selected = store.nativeLang == code,
                                onClick = { store.setNative(code) },
                                label = { Text(name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Target Language & Speech Settings
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("I'm learning… · যে ভাষা শিখছি (বাংলাসহ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                NATIVE_LANGUAGES.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (code, name) ->
                            FilterChip(
                                selected = store.target == code,
                                onClick = { store.setTargetLang(code) },
                                label = { Text(name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Spacer(Modifier.height(14.dp))
                SettingSwitch(
                    title = "Show romaji · রোমাজি উচ্চারণ দেখান",
                    subtitle = "Helpful for Japanese kana · জাপানি হরফ পড়তে সহায়ক।",
                    checked = store.showRomaji,
                    onCheckedChange = { store.showRomaji = it }
                )
                SettingSwitch(
                    title = "Show translations · অনুবাদ দেখান",
                    subtitle = "Show English + বাংলা hints on exercises.",
                    checked = store.showTranslations,
                    onCheckedChange = { store.showTranslations = it }
                )

                Spacer(Modifier.height(12.dp))
                Text("Speech speed · অডিওর গতি", fontWeight = FontWeight.Medium)
                Slider(
                    value = store.speechRate,
                    onValueChange = {
                        store.speechRate = it
                        speaker.setRate(it)
                    },
                    valueRange = 0.5f..1.2f
                )
                Text(
                    "${String.format("%.2f", store.speechRate)}×",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (playUpdateStatus == PlayUpdateStatus.DOWNLOADED_READY) {
                    Updater.completePlayUpdate(ctx)
                } else if (playUpdateStatus != PlayUpdateStatus.CHECKING) {
                    Updater.checkPlayUpdate(ctx, playUpdateLauncher) { status ->
                        playUpdateStatus = status
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_check_play_update")
        ) {
            Text(
                when (playUpdateStatus) {
                    PlayUpdateStatus.CHECKING -> "Checking Google Play… প্লে স্টোরে আপডেট দেখা হচ্ছে…"
                    PlayUpdateStatus.DOWNLOADED_READY -> "Restart to install Play update · রিস্টার্ট করে আপডেট করুন"
                    else -> "Check for Google Play updates · প্লে স্টোর আপডেট (v${Updater.currentVersion(ctx)})"
                }
            )
        }
        when (playUpdateStatus) {
            PlayUpdateStatus.UP_TO_DATE -> {
                Spacer(Modifier.height(6.dp))
                Text(
                    "You're on the latest Google Play version · আপনি সর্বশেষ সংস্করণ ব্যবহার করছেন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PlayUpdateStatus.UPDATE_PROMPTED -> {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Google Play update sheet opened · গুগল প্লে আপডেট চালু হয়েছে",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PlayUpdateStatus.PLAY_FALLBACK_AVAILABLE -> {
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = { Updater.openPlayStore(ctx) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Elyora on Google Play Store · গুগল প্লে স্টোরে দেখুন")
                }
            }
            else -> {}
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showReset = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset all progress · সব অগ্রগতি রিসেট করুন", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset everything? · সব মুছে ফেলবেন?") },
            text = { Text("This deletes your XP, streak, grammar progress, saved writings and review schedules. আপনার সব অগ্রগতি মুছে যাবে।") },
            confirmButton = {
                TextButton(onClick = {
                    store.resetAll()
                    showReset = false
                }) { Text("Reset · রিসেট", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel · বাতিল") }
            }
        )
    }
}

@Composable
private fun SkillBar(label: String, done: Int, total: Int) {
    val pct = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("$done / $total", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun RowScope.StatBox(emoji: String, value: String, label: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
