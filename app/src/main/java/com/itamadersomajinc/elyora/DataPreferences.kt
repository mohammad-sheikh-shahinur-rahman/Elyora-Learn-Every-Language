package com.itamadersomajinc.elyora

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val labelEn: String, val labelBn: String) {
    SYSTEM("System", "সিস্টেম"),
    LIGHT("Light", "লাইট"),
    DARK("Dark", "ডার্ক")
}

data class AppPreferencesData(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedQwenModelId: String = "qwen2.5-1.5b-q4_k_m",
    val isPremium: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val offlineVoicePreferred: Boolean = true,
    val sherpaAcousticPackReady: Boolean = true
)

private val Context.elyoraPrefsStore: DataStore<Preferences> by preferencesDataStore(name = "elyora_prefs")

class DataPreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_QWEN_MODEL = stringPreferencesKey("selected_qwen_model")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        val OFFLINE_VOICE = booleanPreferencesKey("offline_voice")
        val SHERPA_READY = booleanPreferencesKey("sherpa_ready")
    }

    val preferencesFlow: Flow<AppPreferencesData> = context.elyoraPrefsStore.data.map { prefs ->
        val rawTheme = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val mode = runCatching { ThemeMode.valueOf(rawTheme) }.getOrDefault(ThemeMode.SYSTEM)
        AppPreferencesData(
            themeMode = mode,
            selectedQwenModelId = prefs[Keys.SELECTED_QWEN_MODEL] ?: "qwen2.5-1.5b-q4_k_m",
            isPremium = prefs[Keys.IS_PREMIUM] ?: false,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER] ?: false,
            offlineVoicePreferred = prefs[Keys.OFFLINE_VOICE] ?: true,
            sherpaAcousticPackReady = prefs[Keys.SHERPA_READY] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.elyoraPrefsStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSelectedQwenModel(modelId: String) {
        context.elyoraPrefsStore.edit { it[Keys.SELECTED_QWEN_MODEL] = modelId }
    }

    suspend fun setPremium(premium: Boolean) {
        context.elyoraPrefsStore.edit { it[Keys.IS_PREMIUM] = premium }
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        context.elyoraPrefsStore.edit { it[Keys.DAILY_REMINDER] = enabled }
    }

    suspend fun setOfflineVoicePreferred(preferred: Boolean) {
        context.elyoraPrefsStore.edit { it[Keys.OFFLINE_VOICE] = preferred }
    }

    suspend fun setSherpaReady(ready: Boolean) {
        context.elyoraPrefsStore.edit { it[Keys.SHERPA_READY] = ready }
    }
}
