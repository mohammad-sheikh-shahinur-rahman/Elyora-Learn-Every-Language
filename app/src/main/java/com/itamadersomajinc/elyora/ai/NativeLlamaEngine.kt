package com.itamadersomajinc.elyora.ai

import android.content.Context

/**
 * JNI bridge to native `libelyora-llama.so` (compiled from `llama.cpp`).
 * Gracefully reports [isNativeLibraryAvailable] = false on JVM unit-test hosts
 * or ABIs where the native library is not loaded.
 */
object NativeLlamaEngine {

    val isNativeLibraryAvailable: Boolean by lazy {
        runCatching {
            System.loadLibrary("elyora-llama")
            true
        }.getOrDefault(false)
    }

    @Volatile
    private var initialized = false

    @Volatile
    private var loadedPath: String? = null

    @Volatile
    private var lastSystemPrompt: String? = null

    fun ensureInitialized(context: Context): Boolean {
        if (!isNativeLibraryAvailable) return false
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    nativeInit(context.applicationInfo.nativeLibraryDir)
                    initialized = true
                }
            }
        }
        return true
    }

    fun loadAndPrepare(
        context: Context,
        ggufPath: String,
        systemPrompt: String,
        contextSize: Int = 4096
    ): Boolean {
        if (!ensureInitialized(context)) return false
        if (loadedPath == ggufPath) {
            if (lastSystemPrompt != systemPrompt) {
                if (nativeProcessSystemPrompt(systemPrompt) != 0) return false
                lastSystemPrompt = systemPrompt
            }
            return true
        }
        unload()
        if (nativeLoad(ggufPath) != 0) return false
        if (nativePrepare(contextSize) != 0) {
            unload()
            return false
        }
        if (nativeProcessSystemPrompt(systemPrompt) != 0) {
            unload()
            return false
        }
        loadedPath = ggufPath
        lastSystemPrompt = systemPrompt
        return true
    }

    fun startUserTurn(prompt: String, maxTokens: Int = 512): Boolean {
        if (loadedPath == null) return false
        return nativeProcessUserPrompt(prompt, maxTokens) == 0
    }

    fun nextToken(): String? = if (loadedPath != null) nativeGenerateNextToken() else null

    fun cancelGeneration() {
        if (isNativeLibraryAvailable) {
            runCatching { nativeCancel() }
        }
    }

    fun unload() {
        if (isNativeLibraryAvailable) {
            runCatching { nativeUnload() }
        }
        loadedPath = null
        lastSystemPrompt = null
    }

    private external fun nativeInit(nativeLibDir: String)
    private external fun nativeLoad(modelPath: String): Int
    private external fun nativePrepare(nCtx: Int): Int
    private external fun nativeProcessSystemPrompt(systemPrompt: String): Int
    private external fun nativeProcessUserPrompt(userPrompt: String, nPredict: Int): Int
    private external fun nativeGenerateNextToken(): String?
    private external fun nativeCancel()
    private external fun nativeUnload()
    private external fun nativeShutdown()
}
