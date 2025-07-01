package utilities.builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import utilities.*
import utilities.AgentParameters.agentModeParam
import utilities.AgentParameters.hintTypeParam
import utilities.AgentParameters.ideVersionParam
import utilities.AgentParameters.junieGuidelinesContentParam
import utilities.AgentParameters.speedModeParam
import java.io.File


private const val SWE_BENCH_DIR = "swebench_matterhorn"
private const val EVAL_ID_PARAM_NAME = "teamcity.eval.id"


private fun BuildType.executeDep(executionBuildType: BuildType) {
    dependencies {
        dependency(executionBuildType) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
                reuseBuilds = ReuseBuilds.NO
            }
            artifacts {
                artifactRules = """
                    patch.patch
                    matterhorn.tar.gz
                """.trimIndent()
            }
        }
    }
}

private fun BuildType.failureByTimeout(timeoutInMinutes: Int) {
    failureConditions {
        executionTimeoutMin = timeoutInMinutes
        errorMessage = true
    }
}

class EvaluateBuild(executionBuildType: RunAgentBuild, data: Map<String, String>, seed: Int? = null, respectQuota: Boolean = false) : BuildType({
    val instanceId: String = data["instance_id"]!!
    val id_ = instanceId + "_evaluation" + (if (seed != null) "_$seed" else "") + (if (respectQuota) "_o1" else "")
    id(subProjectBuildTypeId(id_))
    name = "$instanceId Evaluation" + (if (seed != null) " $seed" else "")

    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    params {
        runIdParam()
        hintTypeParam(reverse = true)
        junieGuidelinesContentParam(reverse = true)
        ideVersionParam(reverse = true)
        agentModeParam(reverse = true)
        speedModeParam(reverse = true)
        param(EVAL_ID_PARAM_NAME, "stub")
        param("env.EXECUTION_MODE", "teamcity")
        param("env.MATTERHORN_SWE_AGENT_OUTPUTS_DIR", "%teamcity.build.checkoutDir%/.matterhorn")
        param("swe.bench.matterhorn.path", "%teamcity.build.checkoutDir%/$SWE_BENCH_DIR")
        param("patch.path", "%teamcity.build.checkoutDir%/patch.patch")
        param("matterhorn.artifacts.path", "%teamcity.build.checkoutDir%")
    }

    steps {
        generateId(EVAL_ID_PARAM_NAME)
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
        script {
            name = "Unpack artifacts"
            scriptContent = File("utilities/scripts/unpack_artifacts.sh").readText().trimIndent()
        }
        script {
            name = "Run evaluation"
            scriptContent = File("utilities/scripts/run_eval.sh").readText().trimIndent()
            dockerImage = "continuumio/miniconda3:main"
        }
    }

    executeDep(executionBuildType)
    agentRequirements()
    failureByTimeout(60)
})


class EvaluateInDockerBuild(executionBuildType: RunAgentInDockerBuild, data: Map<String, String>, seed: Int? = null, respectQuota: Boolean = false) : BuildType({
    val instanceId: String = data["instance_id"]!!
    val id_ = instanceId + "_evaluation" + (if (seed != null) "_$seed" else "") + (if (respectQuota) "_o1" else "")
    id(subProjectBuildTypeId(id_))
    name = "$instanceId Evaluation (dockerized)" + (if (seed != null) " $seed" else "")

    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    params {
        runIdParam()
        hintTypeParam(reverse = true)
        junieGuidelinesContentParam(reverse = true)
        ideVersionParam(reverse = true)
        agentModeParam(reverse = true)
        speedModeParam(reverse = true)
        param(EVAL_ID_PARAM_NAME, "stub")
        param("env.EXECUTION_MODE", "docker")
        param("env.MATTERHORN_SWE_AGENT_OUTPUTS_DIR", "/.matterhorn")
        param("swe.bench.matterhorn.path", "/$SWE_BENCH_DIR")
        param("patch.path", "/patch.patch")
        param("matterhorn.artifacts.path", "/")
    }

    steps {
        generateId(EVAL_ID_PARAM_NAME)
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
        script {
            name = "Unpack artifacts"
            scriptContent = File("utilities/scripts/unpack_artifacts.sh").readText().trimIndent()
        }
        script {
            name = "Run evaluation"
            scriptContent = File("utilities/scripts/run_eval.sh").readText().trimIndent()
            dockerImage = getDockerFullImageName(instanceId)
            dockerRunParameters = """
                -v %teamcity.build.checkoutDir%/$SWE_BENCH_DIR:/$SWE_BENCH_DIR
                -v %teamcity.build.checkoutDir%/patch.patch:/patch.patch
                -v %teamcity.build.checkoutDir%/.matterhorn:/.matterhorn
            """.trimIndent()
        }
    }

    executeDep(executionBuildType)
    agentRequirements()
    features {
        loginToSpaceRegistryFeature()
    }
    failureByTimeout(60)
})