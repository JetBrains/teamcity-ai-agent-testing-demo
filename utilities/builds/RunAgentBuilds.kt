package utilities.builds

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import utilities.*
import utilities.AgentParameters.agentModeParam
import utilities.AgentParameters.hintTypeParam
import utilities.AgentParameters.ideVersionParam
import utilities.AgentParameters.junieGuidelinesContentParam
import utilities.AgentParameters.speedModeParam
import java.io.File


private const val SWE_BENCH_DIR = "swebench_matterhorn"
private const val MATTERHORN_PLUGIN_DIR = "matterhorn_plugin"
private const val JUNIE_ARTIFACTS_DIR = "junie_artifacts"
private const val TRAJECTORY_ID_PARAM_NAME = "teamcity.trajectory.id"


private fun BuildType.runAgentDep() {
    dependencies {
        dependency(InitBuild) {
            snapshot {
                reuseBuilds = ReuseBuilds.NO
            }
        }

        dependency(AbsoluteId("Matterhorn_MatterhornCi_Matterhorn_Starter")) {
            snapshot {
                reuseBuilds = ReuseBuilds.NO
            }

            artifacts {
                artifactRules = "* => $MATTERHORN_PLUGIN_DIR"
            }
        }
    }
}

private fun BuildFeatures.setQuota(isCustom: Boolean) {
    val quota = when (isCustom) {
        true -> AgentQuota.OPENAI_O1_MODEL
        else -> AgentQuota.DEFAULT
    }

    sharedResources {
        readLock(quota.quotaName)
    }
}

private fun BuildType.failureByTimeout(timeoutInMinutes: Int) {
    failureConditions {
        executionTimeoutMin = timeoutInMinutes
        errorMessage = true
    }
}


class RunAgentBuild(data: Map<String, String>, seed: Int? = null, respectQuota: Boolean = false) : BuildType({
    val instanceId: String = data["instance_id"]!!
    val id_ = instanceId + "_execution" + (if (seed != null) "_$seed" else "")  + (if (respectQuota) "_o1" else "")
    id(subProjectBuildTypeId(id_))
    name = "$instanceId Execution" + (if (seed != null) " $seed" else "")

    val repoDir = getRepoDirForInstance(data)

    matterhornCoreGitHubRepo()
    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    params {
        hintTypeParam()
        junieGuidelinesContentParam()
        ideVersionParam()
        agentModeParam()
        speedModeParam()
        param("env.RunIdFromTeamcity", InitBuild.depParamRefs["tc_run_id"].toString())
        if (seed != null) {
            param("env.MATTERHORN_LLM_RANDOM_SEED", seed.toString())
        }
        param(TRAJECTORY_ID_PARAM_NAME, "stub")

        // Environment dependent paths
        param("env.EXECUTION_MODE", "teamcity")
        param("env.MATTERHORN_CACHES", "%teamcity.build.checkoutDir%")
        param("matterhorn.plugin.path", "%teamcity.build.checkoutDir%/$MATTERHORN_PLUGIN_DIR")
        param("swe.bench.matterhorn.path", "%teamcity.build.checkoutDir%/$SWE_BENCH_DIR")
        param("instance.env.name", repoDir)
        param("instance.repo.path", "%teamcity.build.checkoutDir%/$repoDir")
        param("junie.artifacts.path", "%teamcity.build.checkoutDir%/$JUNIE_ARTIFACTS_DIR")
    }

    artifactRules = """
        ${repoDir}/patch.patch
        %env.MATTERHORN_CACHES%/matterhorn.tar.gz
        idea-logs.tar.gz
    """.trimIndent()

    steps {
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
        seedPause(seed)
        generateId(TRAJECTORY_ID_PARAM_NAME)
        script {
            name = "Setup Conda Repository"
            scriptContent = File("utilities/scripts/install_conda.sh").readText().trimIndent()
        }
        script {
            name = "Install System dependencies"
            scriptContent = File("utilities/scripts/install_system_packages.sh").readText().trimIndent()
        }
        script {
            name = "Prepare Conda virtual environment"
            scriptContent = File("utilities/scripts/prepare_conda_venv.sh").readText().trimIndent()
        }
        script {
            name = "Prepare environment"
            scriptContent = File("utilities/scripts/prepare_instance_env.sh").readText().trimIndent()
        }
        script {
            name = "Download Matterhorn plugin"
            workingDir = MATTERHORN_PLUGIN_DIR
            scriptContent = File("utilities/scripts/download_matterhorn_plugin.sh").readText().trimIndent()
        }
        script {
            id = "RunJunie"
            name = "Run Junie"
            scriptContent = File("utilities/scripts/run_junie.sh").readText().trimIndent()
        }
        script {
            name = "Save and log results"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = File("utilities/scripts/save_and_log_results.sh").readText().trimIndent()
            conditions {
                exists("teamcity.build.step.status.RunJunie")
            }
        }
        script {
            name = "Publish logs"
            executionMode = BuildStep.ExecutionMode.ALWAYS
            scriptContent = File("utilities/scripts/publish_idea_logs.sh").readText().trimIndent()
        }
    }

    runAgentDep()
    agentRequirements()
    failureByTimeout(120)

    features {
        pullRequestsFeature()
        setQuota(isCustom = respectQuota)
    }
})


class RunAgentInDockerBuild(data: Map<String, String>, seed: Int? = null, respectQuota: Boolean = false) : BuildType({
    val instanceId: String = data["instance_id"]!!
    val id_ = instanceId + "_execution" + (if (seed != null) "_$seed" else "")  + (if (respectQuota) "_o1" else "")
    id(subProjectBuildTypeId(id_))
    name = "$instanceId Execution (dockerized)" + (if (seed != null) " $seed" else "")

    val repoDir = getRepoDirForInstance(data)

    matterhornCoreGitHubRepo()
    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    params {
        hintTypeParam()
        junieGuidelinesContentParam()
        ideVersionParam()
        agentModeParam()
        speedModeParam()
        param("env.RunIdFromTeamcity", InitBuild.depParamRefs["tc_run_id"].toString())
        if (seed != null) {
            param("env.MATTERHORN_LLM_RANDOM_SEED", seed.toString())
        }
        param(TRAJECTORY_ID_PARAM_NAME, "stub")

        // Environment dependent paths
        param("env.EXECUTION_MODE", "docker")
        param("env.MATTERHORN_CACHES", "/$JUNIE_ARTIFACTS_DIR")
        param("matterhorn.plugin.path", "/$MATTERHORN_PLUGIN_DIR")
        param("swe.bench.matterhorn.path", "/$SWE_BENCH_DIR")
        param("instance.env.name", repoDir)
        param("instance.repo.path", "/$repoDir")
        param("junie.artifacts.path", "/$JUNIE_ARTIFACTS_DIR")
    }

    artifactRules = """
        ${repoDir}/patch.patch
        %env.MATTERHORN_CACHES%/matterhorn.tar.gz
        idea-logs.tar.gz
    """.trimIndent()

    steps {
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
        seedPause(seed)
        generateId(TRAJECTORY_ID_PARAM_NAME)
        script {
            name = "Download plugin"
            workingDir = MATTERHORN_PLUGIN_DIR
            scriptContent = File("utilities/scripts/download_matterhorn_plugin.sh").readText().trimIndent()
        }
        script {
            id = "RunJunie"
            name = "Run Junie"
            scriptContent = File("utilities/scripts/run_junie.sh").readText().trimIndent()
            dockerImage = getDockerFullImageName(instanceId)
            dockerRunParameters = """
                -v %teamcity.build.checkoutDir%/$SWE_BENCH_DIR:/$SWE_BENCH_DIR
                -v %teamcity.build.checkoutDir%/$MATTERHORN_PLUGIN_DIR:/$MATTERHORN_PLUGIN_DIR
                -v %teamcity.build.checkoutDir%/$JUNIE_ARTIFACTS_DIR:/$JUNIE_ARTIFACTS_DIR
            """.trimIndent()
        }
        script {
            name = "Prepare artifacts from Docker"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = File("utilities/scripts/prepare_artifacts_from_docker.sh").readText().trimIndent()
            conditions {
                exists("teamcity.build.step.status.RunJunie")
            }
        }
        script {
            name = "Save results"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = File("utilities/scripts/save_and_log_results.sh").readText().trimIndent()
            conditions {
                exists("teamcity.build.step.status.RunJunie")
            }
        }
    }

    runAgentDep()
    agentRequirements()
    failureByTimeout(120)

    features {
        loginToSpaceRegistryFeature()
        pullRequestsFeature()
        setQuota(isCustom = respectQuota)
    }
})