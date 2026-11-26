package com.itamadersomajinc.elyora

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.widget.Toast
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Speaks text in any of the supported languages, prioritizing on-device offline TTS voices first.
 */
class Speaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var initFailed = false
    private var pending: Pair<String, String>? = null
    private var rate = 0.85f
    private var preferOfflineVoice = true
    private val appCtx = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var warnedNoEngine = false
    private var warnedNoLang = false
    private var warnedPending = false
    private var media: MediaPlayer? = null
    private var speakSeq = 0
    private var lastUtteranceId: String? = null
    private var lastFallback: Pair<String, String>? = null
    private var completionCallback: (() -> Unit)? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(appCtx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ready = true
                    tts?.setSpeechRate(rate)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            if (utteranceId == lastUtteranceId) {
                                notifyDoneOnMain()
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onError(utteranceId: String?) {
                            fallbackIfCurrent(utteranceId)
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            fallbackIfCurrent(utteranceId)
                        }
                    })
                    pending?.let { p ->
                        pending = null
                        speak(p.first, p.second)
                    }
                } else {
                    initFailed = true
                    toastIfOnce("Audio isn't available on this device. Check Text-to-Speech in Android Settings.")
                }
            }
        } catch (_: Exception) {
            initFailed = true
            toastIfOnce("Couldn't start audio on this device.")
        }
    }

    private fun notifyDoneOnMain() {
        _isSpeaking.value = false
        val cb = completionCallback
        completionCallback = null
        if (cb != null) {
            mainHandler.post { cb() }
        }
    }

    private fun toastIfOnce(msg: String) {
        try {
            Toast.makeText(appCtx, msg, Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
        }
    }

    fun setRate(r: Float) {
        rate = r
        tts?.setSpeechRate(r)
    }

    fun setPreferOffline(offline: Boolean) {
        preferOfflineVoice = offline
    }

    fun hasOfflineVoice(lang: String): Boolean {
        val t = tts ?: return false
        if (!ready) return false
        val root = localeFor(lang).substringBefore('-')
        return runCatching {
            t.voices?.any { v ->
                v.locale.language.equals(root, ignoreCase = true) && !v.isNetworkConnectionRequired
            } == true
        }.getOrDefault(false)
    }

    private fun localeCandidates(t: TextToSpeech, lang: String): List<Locale> {
        val out = LinkedHashSet<Locale>()
        val tag = localeFor(lang)
        out.add(Locale.forLanguageTag(tag))
        val root = tag.substringBefore('-')
        out.add(Locale.forLanguageTag(root))
        runCatching {
            for (l in t.availableLanguages) {
                if (l.language.equals(root, true)) out.add(l)
            }
        }
        return out.toList()
    }

    private fun selectBestOfflineVoice(t: TextToSpeech, lang: String) {
        if (!preferOfflineVoice) return
        val root = localeFor(lang).substringBefore('-')
        runCatching {
            val offlineVoice: Voice? = t.voices
                ?.filter { !it.isNetworkConnectionRequired && it.locale.language.equals(root, ignoreCase = true) }
                ?.maxByOrNull { it.quality }
            if (offlineVoice != null) {
                t.voice = offlineVoice
            }
        }
    }

    /** Returns true if the engine accepted a locale for [lang]. */
    private fun applyLanguage(t: TextToSpeech, lang: String): Boolean {
        for (loc in localeCandidates(t, lang)) {
            val res = try {
                t.setLanguage(loc)
            } catch (_: Exception) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            if (res == TextToSpeech.LANG_AVAILABLE ||
                res == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            ) {
                selectBestOfflineVoice(t, lang)
                return true
            }
        }
        return false
    }

    private fun fallbackIfCurrent(utteranceId: String?) {
        if (utteranceId == null || utteranceId != lastUtteranceId) return
        val p = lastFallback
        lastFallback = null
        if (p == null || preferOfflineVoice) {
            notifyDoneOnMain()
            return
        }
        speakOnline(p.first, p.second)
    }

    private fun releaseMedia() {
        val mp = media ?: return
        media = null
        runCatching { mp.stop() }
        runCatching { mp.release() }
    }

    private fun speakOnline(text: String, lang: String) {
        val code = localeFor(lang).substringBefore('-')
        val url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=" +
            code + "&q=" + URLEncoder.encode(text, "UTF-8")
        Thread {
            var file: File? = null
            var conn: HttpURLConnection? = null
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                conn = c
                c.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
                )
                c.setRequestProperty("Referer", "https://translate.google.com/")
                c.connectTimeout = 10000
                c.readTimeout = 15000
                if (c.responseCode == 200) {
                    val f = File(appCtx.cacheDir, "tts_" + code + "_" + (text.hashCode() and 0x7fffffff) + ".mp3")
                    c.inputStream.use { input -> f.outputStream().use { output -> input.copyTo(output) } }
                    if (f.length() > 0L) file = f
                }
            } catch (_: Exception) {
                file = null
            } finally {
                runCatching { conn?.disconnect() }
            }
            val downloaded = file
            mainHandler.post {
                if (downloaded == null) {
                    if (!warnedNoLang) {
                        warnedNoLang = true
                        toastIfOnce("No ${nativeName(lang)} offline voice installed. Install voice data in Settings → Text-to-speech.")
                    }
                    notifyDoneOnMain()
                    return@post
                }
                try {
                    releaseMedia()
                    val mp = MediaPlayer()
                    mp.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    mp.setDataSource(downloaded.absolutePath)
                    mp.setOnPreparedListener { player ->
                        runCatching { player.playbackParams = player.playbackParams.setSpeed(rate) }
                        player.start()
                    }
                    mp.setOnCompletionListener {
                        releaseMedia()
                        notifyDoneOnMain()
                    }
                    mp.setOnErrorListener { _, _, _ ->
                        releaseMedia()
                        notifyDoneOnMain()
                        true
                    }
                    media = mp
                    mp.prepareAsync()
                } catch (_: Exception) {
                    releaseMedia()
                    notifyDoneOnMain()
                }
            }
        }.start()
    }

    fun speak(text: String, lang: String) {
        speakWithCompletion(text, lang, null)
    }

    fun speakWithCompletion(text: String, lang: String, onDone: (() -> Unit)?) {
        if (text.isBlank()) {
            onDone?.invoke()
            return
        }
        val t = tts
        if (t == null || initFailed) {
            if (!warnedNoEngine) {
                warnedNoEngine = true
                toastIfOnce("Audio isn't ready — install/check Text-to-Speech in device settings.")
            }
            onDone?.invoke()
            return
        }
        if (!ready) {
            if (!warnedPending) {
                warnedPending = true
                toastIfOnce("Audio is starting up… tap the sound button again in a second.")
            }
            if (pending == null) pending = text to lang
            onDone?.invoke()
            return
        }
        completionCallback = onDone
        lastFallback = null
        releaseMedia()
        runCatching { t.stop() }
        t.setSpeechRate(rate)
        if (!applyLanguage(t, lang)) {
            speakOnline(text, lang)
            return
        }
        speakSeq += 1
        val id = "k$speakSeq"
        lastUtteranceId = id
        lastFallback = text to lang
        _isSpeaking.value = true
        val result = try {
            t.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        } catch (_: Exception) {
            TextToSpeech.ERROR
        }
        if (result == TextToSpeech.ERROR) {
            lastFallback = null
            if (!warnedNoEngine) {
                warnedNoEngine = true
                toastIfOnce("Using online fallback for ${nativeName(lang)}. Install the offline voice in device settings.")
            }
            speakOnline(text, lang)
        }
    }

    fun stop() {
        _isSpeaking.value = false
        completionCallback = null
        lastFallback = null
        tts?.stop()
        releaseMedia()
    }

    fun shutdown() {
        _isSpeaking.value = false
        completionCallback = null
        lastFallback = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        releaseMedia()
    }
}
