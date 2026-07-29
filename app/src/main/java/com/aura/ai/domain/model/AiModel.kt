package com.aura.ai.domain.model

/** Catalog of NVIDIA NIM-hosted models exposed in the model selector. */
enum class AiModel(
    val id: String,
    val displayName: String,
    val description: String,
    val supportsReasoning: Boolean = false,
    val contextWindow: Int = 8192
) {
    LLAMA_3_3_70B("meta/llama-3.3-70b-instruct", "Llama 3.3 70B", "Fast, balanced general assistant", contextWindow = 128_000),
    LLAMA_3_1_405B("meta/llama-3.1-405b-instruct", "Llama 3.1 405B", "Most capable, best for complex tasks", contextWindow = 128_000),
    DEEPSEEK_R1("deepseek-ai/deepseek-r1", "DeepSeek R1", "Deep step-by-step reasoning", supportsReasoning = true, contextWindow = 64_000),
    NEMOTRON_70B("nvidia/llama-3.1-nemotron-70b-instruct", "Nemotron 70B", "NVIDIA-tuned, great instruction following", contextWindow = 128_000),
    MISTRAL_LARGE("mistralai/mistral-large-2-instruct", "Mistral Large 2", "Strong multilingual & coding", contextWindow = 128_000);

    companion object {
        val default = LLAMA_3_3_70B
        fun fromId(id: String): AiModel = entries.firstOrNull { it.id == id } ?: default
    }
}
