# Elyora — Learn Every Language

**Elyora — ভাষা শিখুন, বিশ্বকে জানুন** · *Learn every language — explained in yours.*

Elyora (`com.itamadersomajinc.elyora`) is a free, ad-free, offline-first Android language-learning app built with **Kotlin & Jetpack Compose (Material 3)**. Pick your native language (12 supported, including **বাংলা** and **English**) and the language you want to learn (also 12) at any time—the entire curriculum, dictionary, grammar explanations, voice tutor, and practice studio work completely offline with zero mandatory backend or sign-in.

---

## Key Highlights

- 🤖 **Offline AI Tutor & Manus-Style Voice Chat** — Built-in **Qwen2.5 & Qwen3 (`.gguf`)** model catalog and in-app GGUF downloader (`Qwen3-0.6B-Q4_K_M`, `Qwen3-1.7B-Q4_K_M`, `Qwen2.5-1.5B-Instruct`, `Qwen2.5-0.5B-Instruct`) with GGUF header verification and on-device benchmarking. Includes a hands-free **Manus-style Offline Voice Chat** console featuring an animated Pearl Orb mascot, real-time mic reactivity, barge-in interruption, live subtitles, and synchronized TTS playback.
- 🎨 **Modern Light & Dark UI + Animated Floating Navbar** — Full **Light** and **Dark** theme support (`Theme.kt`), edge-to-edge Material 3 design, and a custom **Floating Crest Pill Bottom Navigation Bar** with spring icon animations, pulsing review badge, rotating center dotted halo, and quick-launch Practice Studio sheet.
- 🌐 **Anytime Language Switcher (12 × 12 Tracks)** — Switch native or target languages anytime from the top bar or Quick Studio (`English`, `বাংলা`, `Hindi`, `Japanese`, `Spanish`, `Arabic`, `French`, `German`, `Tamil`, `Telugu`, `Urdu`, `Kannada`) with Bengali explanations, English examples, IPA/romaji, and offline gloss tables.
- 🚫 **Zero Mandatory Backend & Ad-Free** — No forced cloud login, no Firebase dependency, no ads, and no paywalls.

---

## Learning Features & Practice Studio (14+ Modules)

- 🌸 **Kana Mastery** — Hiragana & Katakana (46×2 + voiced variants) with picture mnemonics, audio, learned tracker, and listening/reading quizzes.
- 🔤 **Alphabet Starter (A–Z)** — Full letter-by-letter starter course with phonetics, sounds, and example words for every language track.
- 🗾 **Kanji & 5,000 Kanji Words** — Stroke/meaning study plus 5,000 kanji vocabulary words organized into 20 thematic categories.
- 📚 **Bilingual Core Lessons** — High-frequency vocabulary across greetings, numbers, colors, family, food, animals, actions, adjectives, time, places, weather, and survival phrases with emoji prompts and audio.
- 🇬🇧 **Oxford 5000 (A1–C1)** — ~4,960 graded Oxford 5000 words with parts of speech, native glosses, examples, and audio.
- 🏯 **JLPT (N5 → N1) & CEFR (A1 → C2)** — Level-grouped Japanese and English word lists with native meanings.
- 📗 **Genki 1 & Japanese From Zero** — Structured vocabulary and matching grammar sections for Genki Textbook 1 (12 lessons) and JFZ Book 1 (pre-lessons + 13 lessons).
- 📖 **Offline Dictionary & SQLite FTS4 Search** — Instant offline search across **25,656 English** and **217,686 Japanese** entries in any supported language.
- 🧾 **Verb Trainer (4,400+ Verbs)** — Searchable English verb database showing **1st (base), 2nd (past), 3rd (past participle), and 4th (-ing)** forms, irregular badges, audio, and two-way native-language search.
- 🧠 **Unified SM-2 Spaced Repetition (SRS)** — Again / Hard / Good / Easy grading across every word module with XP, levels, daily streaks, and best-streak tracking.
- 🗣️ **Phrase Bank & 🎭 Roleplay Conversations** — Real-world chunked sentences with word-by-word breakdowns plus interactive scenario dialogues (hotel, taxi, shopping, dining).
- 📐 **Grammar Patterns & 210+ Grammar Academy** — Curated grammar patterns (JLPT/CEFR/Genki/JFZ) + 210+ structured Bengali/English grammar lessons, each backed by **10+ example sentences** and interactive multiple-choice drills.
- ⏳ **13 English Tenses Mastery** — Complete coverage of all 13 English tenses (Present/Past/Future Simple, Continuous, Perfect, Perfect Continuous + *Be Going To*), rules, common-mistake tips, and 12 contextual examples per tense.
- 🎯 **Practice Studio Tools** — Daily Challenge, Tense Mastery, Grammar Quiz, Sentence Builder, Speaking Practice, Listening Practice, Writing Studio, and Offline AI Voice Chat.
- 🔊 **Smart Audio & Offline TTS** — Multi-fallback Text-to-Speech (`Speaker.kt`) supporting adjustable speed and offline voice preference.
- 🔄 **Google Play In-App Updates** — Official Play Core flexible in-app update flow (`com.google.android.play:app-update-ktx`) with Play Store fallback (`Updater.kt`).

---

## Navigation & Screens

- **Home** — Daily streak, XP progress, quick language switcher, theme toggle, and 14-tool Practice Studio grid.
- **Learn** (`Courses · Grammar · AI Chat`) — Alphabet, Kana, Kanji, Core Lessons, Oxford 5000, Genki 1, JFZ, JLPT/CEFR, Phrase Bank, Roleplays, 210+ Grammar Academy, and Offline Qwen AI Tutor / Voice Chat.
- **Review** (`Review · Words · Verbs`) — Unified SM-2 Flashcard Review, 25k+/217k+ Offline Dictionary, and 4,400+ Verb Trainer.
- **Center `+` Quick Studio** — Instant launcher for Offline AI Voice Chat (Manus), Daily Challenge, 13 Tenses, Grammar Quiz, Sentence Builder, Speaking, Listening, Writing, and Language Switcher.
- **Profile** — Native/target language selection, Light/Dark theme switch, offline TTS toggle, daily WorkManager reminders, stats, and Play Store update check.

---

## Supported Languages

| Role | Languages (12) |
|---|---|
| **Native (Explanation)** | English · বাংলা (Bengali) · Hindi · Japanese · Spanish · Arabic · French · German · Tamil · Telugu · Urdu · Kannada |
| **Target (Learning)** | English · Japanese · বাংলা (Bengali) · Hindi · Spanish · Arabic · French · German · Tamil · Telugu · Urdu · Kannada |

---

## Technology Stack

| Layer | Implementation |
|---|---|
| **Language & Build** | Kotlin `2.2.21` · Android Gradle Plugin `9.1.1` · Gradle Kotlin DSL · `minSdk 26` / `targetSdk 36` |
| **UI & Theme** | Jetpack Compose (BOM `2025.05.00`) · Material 3 · Custom Light/Dark Theme · Spring & Canvas Animations |
| **Navigation** | Navigation Compose (`2.9.0`) with `@Serializable` type-safe routes |
| **Local Database & Search** | Room (`2.7.1`) + SQLite FTS4 · KSP (`2.3.4`) |
| **Preferences & Background** | Jetpack DataStore (`1.1.4`) · WorkManager (`2.10.0`) |
| **Async & Serialization** | Kotlin Coroutines (`1.9.0`) + StateFlow · `kotlinx.serialization` (`1.8.0`) |
| **On-Device AI** | Qwen2.5 / Qwen3 Quantized `.gguf` Model Manager, Header Inspector & Manus Voice Console |
| **Updates** | Google Play In-App Updates (`play:app-update-ktx:2.1.0`) |

---

## Building Locally

```bash
gradle :app:assembleDebug
```

Output signed debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## CI & Releases

Every push to `main` (or manual dispatch) triggers [`.github/workflows/build.yml`](.github/workflows/build.yml), which builds the signed release APK (`./gradlew assembleRelease`), uploads it as a GitHub Actions artifact, and creates a GitHub Release tagged `v<versionName>` (configured via `versionCode` / `versionName` in `app/build.gradle.kts`).
