package utilities

enum class AgentQuota(val quotaName: String) {
    DEFAULT("default_model_quota"),
    OPENAI_O1_MODEL("openai_o1_model");
}