#include <android/log.h>
#include <jni.h>
#include <algorithm>
#include <cmath>
#include <iomanip>
#include <sstream>
#include <string>
#include <unistd.h>
#include <vector>

#include "chat.h"
#include "common.h"
#include "llama.h"
#include "sampling.h"

#define TAG "ElyoraLlamaJNI"
#define LOGi(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGw(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static void android_log_cb(ggml_log_level level, const char * text, void * /* user_data */) {
    if (level >= GGML_LOG_LEVEL_ERROR) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "%s", text);
    } else if (level == GGML_LOG_LEVEL_WARN) {
        __android_log_print(ANDROID_LOG_WARN, TAG, "%s", text);
    }
}

constexpr int   N_THREADS_MIN        = 2;
constexpr int   N_THREADS_MAX        = 4;
constexpr int   N_THREADS_HEADROOM   = 2;
constexpr int   DEFAULT_CONTEXT_SIZE = 2048;
constexpr int   OVERFLOW_HEADROOM    = 4;
constexpr int   BATCH_SIZE           = 512;
constexpr float DEFAULT_TEMP         = 0.6f;

static llama_model            * g_model   = nullptr;
static llama_context          * g_context = nullptr;
static common_batch             g_batch;
static common_chat_templates_ptr g_chat_templates;
static common_sampler         * g_sampler = nullptr;

constexpr const char * ROLE_SYSTEM    = "system";
constexpr const char * ROLE_USER      = "user";
constexpr const char * ROLE_ASSISTANT = "assistant";

static std::vector<common_chat_msg> chat_msgs;
static llama_pos system_prompt_position = 0;
static llama_pos current_position = 0;
static llama_pos stop_generation_position = 0;
static std::string cached_token_chars;
static std::ostringstream assistant_ss;

static void reset_long_term_states(bool clear_kv = true) {
    chat_msgs.clear();
    system_prompt_position = 0;
    current_position = 0;
    if (clear_kv && g_context) {
        llama_memory_clear(llama_get_memory(g_context), false);
    }
}

static void reset_short_term_states() {
    stop_generation_position = 0;
    cached_token_chars.clear();
    assistant_ss.str("");
    assistant_ss.clear();
}

static void shift_context() {
    if (!g_context) return;
    const int n_discard = std::max(1, (current_position - system_prompt_position) / 2);
    llama_memory_seq_rm(llama_get_memory(g_context), 0, system_prompt_position, system_prompt_position + n_discard);
    llama_memory_seq_add(llama_get_memory(g_context), 0, system_prompt_position + n_discard, current_position, -n_discard);
    current_position -= n_discard;
}

static std::string chat_add_and_format(const std::string & role, const std::string & content) {
    common_chat_msg new_msg;
    new_msg.role = role;
    new_msg.content = content;
    auto formatted = common_chat_format_single(
            g_chat_templates.get(), chat_msgs, new_msg, role == ROLE_USER, false);
    chat_msgs.push_back(new_msg);
    return formatted;
}

static int decode_tokens_in_batches(
        llama_context * context,
        common_batch & batch,
        const llama_tokens & tokens,
        const llama_pos start_pos,
        const bool compute_last_logit = false) {
    for (int i = 0; i < (int) tokens.size(); i += BATCH_SIZE) {
        const int cur_batch_size = std::min((int) tokens.size() - i, BATCH_SIZE);
        batch.clear();
        if (start_pos + i + cur_batch_size >= DEFAULT_CONTEXT_SIZE - OVERFLOW_HEADROOM) {
            shift_context();
        }
        for (int j = 0; j < cur_batch_size; j++) {
            const llama_token token_id = tokens[i + j];
            const llama_pos position = start_pos + i + j;
            const bool want_logit = compute_last_logit && (i + j == (int) tokens.size() - 1);
            batch.add(token_id, position, 0, want_logit);
        }
        if (llama_process(context, LLAMA_PROCESS_TYPE_DECODE, batch.get()) != 0) {
            LOGe("llama_process failed");
            return 1;
        }
    }
    return 0;
}

static bool is_valid_utf8(const char * string) {
    if (!string) return true;
    const auto * bytes = (const unsigned char *) string;
    while (*bytes != 0x00) {
        int num;
        if ((*bytes & 0x80) == 0x00) num = 1;
        else if ((*bytes & 0xE0) == 0xC0) num = 2;
        else if ((*bytes & 0xF0) == 0xE0) num = 3;
        else if ((*bytes & 0xF8) == 0xF0) num = 4;
        else return false;
        bytes += 1;
        for (int i = 1; i < num; ++i) {
            if ((*bytes & 0xC0) != 0x80) return false;
            bytes += 1;
        }
    }
    return true;
}

extern "C" JNIEXPORT void JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeInit(JNIEnv * env, jobject, jstring nativeLibDir) {
    llama_log_set(android_log_cb, nullptr);
    const auto * path = env->GetStringUTFChars(nativeLibDir, nullptr);
    if (path) {
        LOGi("Loading GGML backends from %s", path);
        ggml_backend_load_all_from_path(path);
        env->ReleaseStringUTFChars(nativeLibDir, path);
    }
    llama_backend_init();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeLoad(JNIEnv * env, jobject, jstring jmodel_path) {
    if (g_model) {
        if (g_sampler) { common_sampler_free(g_sampler); g_sampler = nullptr; }
        g_chat_templates.reset();
        g_batch = common_batch();
        if (g_context) { llama_free(g_context); g_context = nullptr; }
        llama_model_free(g_model);
        g_model = nullptr;
    }
    llama_model_params model_params = llama_model_default_params();
    const auto * model_path = env->GetStringUTFChars(jmodel_path, nullptr);
    if (!model_path) return 1;
    LOGi("Loading GGUF model: %s", model_path);
    g_model = llama_model_load_from_file(model_path, model_params);
    env->ReleaseStringUTFChars(jmodel_path, model_path);
    return g_model ? 0 : 1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativePrepare(JNIEnv *, jobject) {
    if (!g_model) return 1;
    const int n_threads = std::max(N_THREADS_MIN, std::min(N_THREADS_MAX,
            (int) sysconf(_SC_NPROCESSORS_ONLN) - N_THREADS_HEADROOM));
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = DEFAULT_CONTEXT_SIZE;
    ctx_params.n_batch = BATCH_SIZE;
    ctx_params.n_ubatch = BATCH_SIZE;
    ctx_params.n_threads = n_threads;
    ctx_params.n_threads_batch = n_threads;

    g_context = llama_init_from_model(g_model, ctx_params);
    if (!g_context) return 1;

    g_batch = common_batch(g_context);
    g_chat_templates = common_chat_templates_init(g_model, "");
    common_params_sampling sparams;
    sparams.temp = DEFAULT_TEMP;
    g_sampler = common_sampler_init(g_model, sparams);
    reset_long_term_states(false);
    reset_short_term_states();
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeProcessSystemPrompt(JNIEnv * env, jobject, jstring jsys) {
    if (!g_context) return 1;
    reset_long_term_states();
    reset_short_term_states();

    const auto * sys = env->GetStringUTFChars(jsys, nullptr);
    if (!sys) return 1;
    std::string formatted(sys);
    const bool has_tmpl = common_chat_templates_was_explicit(g_chat_templates.get());
    if (has_tmpl) {
        formatted = chat_add_and_format(ROLE_SYSTEM, sys);
    }
    env->ReleaseStringUTFChars(jsys, sys);

    const auto tokens = common_tokenize(g_context, formatted, has_tmpl, has_tmpl);
    if ((int) tokens.size() > DEFAULT_CONTEXT_SIZE - OVERFLOW_HEADROOM) return 1;
    if (decode_tokens_in_batches(g_context, g_batch, tokens, current_position) != 0) return 2;
    system_prompt_position = current_position = (int) tokens.size();
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeProcessUserPrompt(JNIEnv * env, jobject, jstring juser, jint n_predict) {
    if (!g_context) return 1;
    reset_short_term_states();

    const auto * user = env->GetStringUTFChars(juser, nullptr);
    if (!user) return 1;
    std::string formatted(user);
    const bool has_tmpl = common_chat_templates_was_explicit(g_chat_templates.get());
    if (has_tmpl) {
        formatted = chat_add_and_format(ROLE_USER, user);
    }
    env->ReleaseStringUTFChars(juser, user);

    auto tokens = common_tokenize(g_context, formatted, has_tmpl, has_tmpl);
    const int max_batch = DEFAULT_CONTEXT_SIZE - OVERFLOW_HEADROOM;
    if ((int) tokens.size() > max_batch) tokens.resize(max_batch);
    if (decode_tokens_in_batches(g_context, g_batch, tokens, current_position, true) != 0) return 2;
    current_position += (int) tokens.size();
    stop_generation_position = current_position + n_predict;
    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeGenerateNextToken(JNIEnv * env, jobject) {
    if (!g_context || !g_sampler || !g_model) return nullptr;
    if (current_position >= DEFAULT_CONTEXT_SIZE - OVERFLOW_HEADROOM) shift_context();
    if (current_position >= stop_generation_position) return nullptr;

    const auto new_token_id = common_sampler_sample(g_sampler, g_context, -1);
    common_sampler_accept(g_sampler, new_token_id, true);

    g_batch.clear();
    g_batch.add(new_token_id, current_position, 0, true);
    if (llama_process(g_context, LLAMA_PROCESS_TYPE_DECODE, g_batch.get()) != 0) return nullptr;
    current_position++;

    if (llama_vocab_is_eog(llama_model_get_vocab(g_model), new_token_id)) {
        chat_add_and_format(ROLE_ASSISTANT, assistant_ss.str());
        return nullptr;
    }

    cached_token_chars += common_token_to_piece(g_context, new_token_id);
    if (is_valid_utf8(cached_token_chars.c_str())) {
        jstring res = env->NewStringUTF(cached_token_chars.c_str());
        assistant_ss << cached_token_chars;
        cached_token_chars.clear();
        return res;
    }
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT void JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeUnload(JNIEnv *, jobject) {
    reset_long_term_states();
    reset_short_term_states();
    if (g_sampler) { common_sampler_free(g_sampler); g_sampler = nullptr; }
    g_chat_templates.reset();
    g_batch = common_batch();
    if (g_context) { llama_free(g_context); g_context = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
}

extern "C" JNIEXPORT void JNICALL
Java_com_itamadersomajinc_elyora_ai_NativeLlamaEngine_nativeShutdown(JNIEnv *, jobject) {
    llama_backend_free();
}
