package utilities

import jetbrains.buildServer.configs.kotlin.ParametrizedWithType

private fun revParamName(paramName: String) = "reverse.dep.*.$paramName"


object AgentParameters {
    fun ParametrizedWithType.hintTypeParam(reverse: Boolean = false) {
        val paramName = "env.HINT_TYPE"
        select(
            name = if (reverse) revParamName(paramName) else paramName,
            value = "empty",
            allowMultiple = false,
            options = listOf(
                "Empty" to "empty",
                "Golden files to user specified files" to "golden_files_to_user_specified_files",
                "Random files to user specified files" to "random_files",
                "Golden and random files to user specified files" to "golden_files_and_random_files_to_user_specified_files",
            )
        )
    }

    fun ParametrizedWithType.junieGuidelinesContentParam(reverse: Boolean = false) {
        val paramName = "junie.guidelines.content"
        param(if (reverse) revParamName(paramName) else paramName, "")
    }

    fun ParametrizedWithType.ideVersionParam(reverse: Boolean = false) {
        val paramName = "TARGET"
        select(
            name = if (reverse) revParamName(paramName) else paramName,
            value = "251",
            allowMultiple = false,
            options = listOf("243", "251"),
            label = "Target IDE Version",
            description = "The version of IntelliJ IDEA SDK to build the plugin for."
        )
    }

    fun ParametrizedWithType.agentModeParam(reverse: Boolean = false) {
        val paramName = "env.AGENT_MODE"
        select(
            name = if (reverse) revParamName(paramName) else paramName,
            value = "ElectricJunior",
            allowMultiple = false,
            options = listOf(
                "ElectricJunior",
                "ElectricJuniorChat",
                "ElectricJuniorCloud",
                "ElectricJuniorAIA"
            )
        )
    }

    fun ParametrizedWithType.speedModeParam(reverse: Boolean = false) {
        val paramName = "env.MATTERHORN_SPEED_MODE"
        select(
            name = if (reverse) revParamName(paramName) else paramName,
            value = "Quality",
            allowMultiple = false,
            options = listOf(
                "Quality",
                "Speed"
            )
        )
    }
}