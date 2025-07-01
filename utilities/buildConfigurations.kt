package utilities

import jetbrains.buildServer.configs.kotlin.Project
import utilities.builds.EvaluateBuild
import utilities.builds.EvaluateInDockerBuild
import utilities.builds.RunAgentBuild
import utilities.builds.RunAgentInDockerBuild
import utilities.builds.ValidateBuild
import utilities.builds.ValidateInDockerBuild

private fun hasDockerImage(instanceData: Map<String, String>) = hasDockerImage(instanceData["instance_id"]!!)

fun createBuildTypeForValidation(
    project: Project,
    instanceData: Map<String, String>,
) {
    val buildType = when (hasDockerImage(instanceData)) {
        false -> ValidateBuild(instanceData)
        true -> ValidateInDockerBuild(instanceData)
    }
    project.buildType(buildType)
}


fun createBuildTypeForExecution(
    project: Project,
    instanceData: Map<String, String>,
    seed: Int? = null,
    respectQuota: Boolean = false,
) {
    val buildType = when (hasDockerImage(instanceData)) {
        false -> RunAgentBuild(instanceData, seed, respectQuota)
        true -> RunAgentInDockerBuild(instanceData, seed, respectQuota)
    }
    project.buildType(buildType)
}


fun createBuildTypeForEvaluation(
    project: Project,
    instanceData: Map<String, String>,
    seed: Int? = null,
    respectQuota: Boolean = false,
) {
    val builds = when (hasDockerImage(instanceData)) {
        false -> {
            val executionBuildType = RunAgentBuild(instanceData, seed, respectQuota)
            val evaluationBuildType = EvaluateBuild(executionBuildType, instanceData, seed, respectQuota)
            listOf(executionBuildType, evaluationBuildType)
        }
        true -> {
            val executionBuildType = RunAgentInDockerBuild(instanceData, seed, respectQuota)
            val evaluationBuildType = EvaluateInDockerBuild(executionBuildType, instanceData, seed, respectQuota)
            listOf(executionBuildType, evaluationBuildType)
        }
    }
    builds.forEach { project.buildType(it) }
}