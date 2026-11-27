package com.itamadersomajinc.elyora

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itamadersomajinc.elyora.ai.AIInferenceService
import com.itamadersomajinc.elyora.ai.ConversationEntity
import com.itamadersomajinc.elyora.ai.ConversationRepository
import com.itamadersomajinc.elyora.ai.LocalOfflineAIInferenceService
import com.itamadersomajinc.elyora.ai.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Fts4
@Entity(tableName = "vocab_fts")
data class VocabFtsEntity(
    val head: String,
    val pos: String,
    val hi: String,
    val bn: String
)

@Fts4
@Entity(tableName = "grammar_fts")
data class GrammarFtsEntity(
    val lessonId: String,
    val title: String,
    val titleBn: String,
    val category: String,
    val level: String,
    val formula: String,
    val rule: String,
    val ruleBn: String
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val modelId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "qwen_benchmarks")
data class QwenBenchmarkEntity(
    @PrimaryKey val modelId: String,
    val tokensPerSec: Float,
    val bnScore: Int,
    val enScore: Int,
    val jaScore: Int,
    val latencyMs: Long,
    val testedAt: Long = System.currentTimeMillis()
)

@Dao
interface ElyoraDao {

    @Query("SELECT * FROM vocab_fts WHERE vocab_fts MATCH :matchQuery LIMIT :limit")
    suspend fun searchVocabFts(matchQuery: String, limit: Int = 80): List<VocabFtsEntity>

    @Query("SELECT COUNT(*) FROM vocab_fts")
    suspend fun vocabFtsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabFtsBatch(items: List<VocabFtsEntity>)

    @Query("SELECT * FROM grammar_fts WHERE grammar_fts MATCH :matchQuery LIMIT :limit")
    suspend fun searchGrammarFts(matchQuery: String, limit: Int = 25): List<GrammarFtsEntity>

    @Query("SELECT COUNT(*) FROM grammar_fts")
    suspend fun grammarFtsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarFtsBatch(items: List<GrammarFtsEntity>)

    @Query("SELECT * FROM ai_messages ORDER BY id ASC")
    fun observeAiMessages(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity): Long

    @Query("DELETE FROM ai_messages")
    suspend fun clearAiMessages()

    @Query("SELECT * FROM qwen_benchmarks ORDER BY bnScore DESC")
    fun observeBenchmarks(): Flow<List<QwenBenchmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBenchmark(benchmark: QwenBenchmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversation_messages WHERE conversationId = :conversationId ORDER BY id ASC")
    fun observeConversationMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationMessage(message: MessageEntity): Long

    @Query("DELETE FROM conversation_messages WHERE conversationId = :conversationId")
    suspend fun clearConversationMessages(conversationId: String)
}

@Database(
    entities = [
        VocabFtsEntity::class,
        GrammarFtsEntity::class,
        AiMessageEntity::class,
        QwenBenchmarkEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ElyoraDatabase : RoomDatabase() {
    abstract fun elyoraDao(): ElyoraDao

    companion object {
        @Volatile
        private var INSTANCE: ElyoraDatabase? = null

        fun getInstance(context: Context): ElyoraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElyoraDatabase::class.java,
                    "elyora_local.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class ElyoraRepository(
    private val context: Context,
    private val dao: ElyoraDao,
    val prefs: DataPreferencesManager
) {
    val aiMessagesFlow: Flow<List<AiMessageEntity>> = dao.observeAiMessages()
    val benchmarksFlow: Flow<List<QwenBenchmarkEntity>> = dao.observeBenchmarks()

    private fun sanitizeFtsQuery(raw: String): String {
        val tokens = raw.trim()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 }
            .take(5)
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" ") { "$it*" }
    }

    suspend fun searchVocabFts(rawQuery: String, limit: Int = 80): List<EnEntry> {
        val match = sanitizeFtsQuery(rawQuery)
        if (match.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            runCatching {
                dao.searchVocabFts(match, limit).map {
                    EnEntry(head = it.head, hi = it.hi, pos = it.pos)
                }
            }.getOrDefault(emptyList())
        }
    }

    suspend fun searchGrammarFts(rawQuery: String, limit: Int = 12): List<GrammarFtsEntity> {
        val match = sanitizeFtsQuery(rawQuery)
        if (match.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            runCatching { dao.searchGrammarFts(match, limit) }.getOrDefault(emptyList())
        }
    }

    suspend fun addAiMessage(isUser: Boolean, text: String, modelId: String) {
        dao.insertAiMessage(AiMessageEntity(isUser = isUser, text = text, modelId = modelId))
    }

    suspend fun clearAiMessages() {
        dao.clearAiMessages()
    }

    suspend fun saveBenchmark(benchmark: QwenBenchmarkEntity) {
        dao.upsertBenchmark(benchmark)
    }

    suspend fun ensureFtsPopulated() = withContext(Dispatchers.IO) {
        runCatching {
            if (dao.grammarFtsCount() == 0 && GrammarAcademyData.all.isNotEmpty()) {
                val grammarRows = GrammarAcademyData.all.map { g ->
                    GrammarFtsEntity(
                        lessonId = g.id,
                        title = g.title,
                        titleBn = g.titleBn,
                        category = g.category,
                        level = g.level,
                        formula = g.formula,
                        rule = g.rule,
                        ruleBn = g.ruleBn
                    )
                }
                grammarRows.chunked(100).forEach { dao.insertGrammarFtsBatch(it) }
            }

            if (dao.vocabFtsCount() == 0 && DictionaryData.enAll.isNotEmpty()) {
                Gloss.ensureBn()
                val vocabRows = DictionaryData.enAll.map { e ->
                    VocabFtsEntity(
                        head = e.head,
                        pos = e.pos,
                        hi = e.hi,
                        bn = banglaGloss(e.head)
                    )
                }
                vocabRows.chunked(800).forEach { dao.insertVocabFtsBatch(it) }
            }
        }
    }
}

class AppContainer(context: Context) {
    private val appCtx = context.applicationContext
    val prefs = DataPreferencesManager(appCtx)
    val db = ElyoraDatabase.getInstance(appCtx)
    val repository = ElyoraRepository(appCtx, db.elyoraDao(), prefs)
    val conversationRepository = ConversationRepository(db.elyoraDao())
    val aiEngine = OfflineBilingualEngine(appCtx, repository)
    val llamaBridge = LocalLlamaBridge(appCtx, repository)
    val inferenceService: AIInferenceService = LocalOfflineAIInferenceService(appCtx, llamaBridge, aiEngine)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun warmUpIndexes() {
        appScope.launch {
            var attempts = 0
            while (!DictionaryData.loaded && attempts < 40) {
                kotlinx.coroutines.delay(150)
                attempts++
            }
            repository.ensureFtsPopulated()
        }
    }
}

object ElyoraAppProvider {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var instance: AppContainer? = null

    fun get(context: Context): AppContainer {
        return instance ?: synchronized(this) {
            instance ?: AppContainer(context.applicationContext).also { instance = it }
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
