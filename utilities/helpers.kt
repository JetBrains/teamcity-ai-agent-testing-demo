package utilities

import data.objectMapper
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import utilities.AgentParameters.agentModeParam
import utilities.AgentParameters.hintTypeParam
import utilities.AgentParameters.ideVersionParam
import utilities.AgentParameters.junieGuidelinesContentParam
import utilities.AgentParameters.speedModeParam
import utilities.builds.InitBuild
import java.io.File

enum class SliceAggregatorType {
    VALIDATION,
    EVALUATION,
    EVALUATION_O1,
    EXECUTION;

    fun isEvaluation(): Boolean {
        return this == EVALUATION || this == EVALUATION_O1
    }
}

fun subProjectBuildTypeId(rowInstanceId: String): String {
    return rowInstanceId.replace(Regex("[^A-Za-z0-9]"), "")
}

fun getRepoDirForInstance(instance: Map<String, String>): String {
    return instance["repo"]!!.split("/").last()
}

fun BuildSteps.prepareInstanceJsonStep(data: Map<String, String>, fileName: String = "swebench_instance.json", workDir: String) {
    script {
        name = "Create $fileName"
        workingDir = workDir
        // Be aware of escaping quotes in the JSON
        scriptContent = """
            cat << '_EOF_' > $fileName
            ${objectMapper.writeValueAsString(data).replace("%", "%%")}
            _EOF_

            echo "##teamcity[blockOpened name='Data' description='$fileName']"
            cat $fileName | jq .
            echo "##teamcity[blockClosed name='Data']"
        """.trimIndent()
    }
}

fun BuildSteps.generateId(paramName: String) {
    python {
        command = script {
            content = """
                import uuid
                print(f"##teamcity[setParameter name='$paramName' value='{uuid.uuid4()}']")
            """.trimIndent()
        }
    }
}

fun BuildSteps.seedPause(seed: Int? = null) {
    val pauseInMinutes = if (seed == null) 0 else if (seed > 5) 10 else seed * 2
    script {
        id = "Seed pause"
        name = "Pause for $pauseInMinutes minute(s)"
        scriptContent = """
        echo "Pausing for $pauseInMinutes minute(s)..."
        sleep ${pauseInMinutes}m
        echo "Pause completed"
    """.trimIndent()
    }
}

fun BuildType.sweBenchGitRepo(workDir: String) {
    vcs {
        root(AbsoluteId("Matterhorn_SweBench"), "+:. => $workDir")
    }
}

fun BuildType.matterhornCoreGitHubRepo() {
    vcs {
        root(AbsoluteId("Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore"))
    }
}

fun BuildType.additionalVcsOptions() {
    vcs {
        checkoutDir = "%teamcity.agent.work.dir%/junie"
        cleanCheckout = true
        showDependenciesChanges = true
    }
}

fun BuildType.agentRequirements() {
    requirements {
        contains("teamcity.agent.jvm.os.arch", "amd64")
        startsWith("cloud.amazon.agent-name-prefix", "MatterhornLinux")
    }
}

fun ParametrizedWithType.runIdParam() {
    param("teamcity.run.id", InitBuild.depParamRefs["tc_run_id"].toString())
}


class ValidationAggregator(buildId: String, buildName: String, slice: List<String>) : BuildType({
    id(buildId)
    name = buildName
    type = Type.COMPOSITE

    dependencies {
        for (instanceId in slice) {
            val depId = instanceId
            snapshot(RelativeId(subProjectBuildTypeId(depId))) {
                onDependencyFailure = FailureAction.IGNORE
                onDependencyCancel = FailureAction.IGNORE
                reuseBuilds = ReuseBuilds.NO
            }
        }
    }
})

class ExecutionAggregator(buildId: String, buildName: String, slice: List<String>) : BuildType({
    id(buildId)
    name = buildName
    type = Type.COMPOSITE

    params {
        runIdParam()
        hintTypeParam(reverse = true)
        junieGuidelinesContentParam(reverse = true)
        ideVersionParam(reverse = true)
        agentModeParam(reverse = true)
        speedModeParam(reverse = true)
    }

    dependencies {
        for (instanceId in slice) {
            val depId = instanceId + "_execution"
            snapshot(RelativeId(subProjectBuildTypeId(depId))) {
                onDependencyFailure = FailureAction.IGNORE
                onDependencyCancel = FailureAction.IGNORE
                reuseBuilds = ReuseBuilds.NO
            }
        }
    }
})

class EvaluationAggregator(buildId: String, buildName: String, slice: List<String>, aggregatorType: SliceAggregatorType, runsPerTask: Int? = null) : BuildType({
    id(buildId)
    name = buildName

    params {
        runIdParam()
        hintTypeParam(reverse = true)
        junieGuidelinesContentParam(reverse = true)
        ideVersionParam(reverse = true)
        agentModeParam(reverse = true)
        speedModeParam(reverse = true)
    }

    val repoDir = "swebench_matterhorn"
    sweBenchGitRepo(repoDir)

    artifactRules = """
        data.parquet
        stats_per_run.json
        stats_per_task.jsonl
        trajectories/**
        aggregated_statistics.json
    """.trimIndent()

    steps {
        python {
            enabled = false
            name = "Statistics calculation"
            command = script {
                content = File("utilities/scripts/calculate_success_rate.py").readText()
            }
        }
        script {
            enabled = true
            name = "Calculate statistics report"
            workingDir = repoDir
            scriptContent = File("utilities/scripts/calculate_statistics_report.sh").readText()
        }
        python {
            enabled = true
            name = "Set statistics on TeamCity"
            command = script {
                content = File("utilities/scripts/set_statistics_on_teamcity.py").readText()
            }
        }
        script {
            enabled = true
            name = "Install DuckDB dependencies"
            workingDir = repoDir
            scriptContent = """
                python3 -m venv .venv
                source .venv/bin/activate
                pip install duckdb pandas pyarrow
            """.trimIndent()
        }
        python {
            enabled = true
            name = "Aggregate statistics with DuckDB"
            workingDir = repoDir
            command = script {
                content = File("utilities/scripts/aggregate_statistics_duckdb.py").readText()
            }
        }
    }

    dependencies {
        val depIds = when (runsPerTask) {
            null -> slice.map { instanceId ->
                when (aggregatorType) {
                    SliceAggregatorType.EVALUATION -> instanceId + "_evaluation"
                    SliceAggregatorType.EVALUATION_O1 -> instanceId + "_evaluation_o1"
                    else -> throw Exception("EvaluationAggregator available only for evaluation")
                }
            }
            else -> slice.flatMap { instanceId ->
                (0 until runsPerTask).map { seed ->
                    when (aggregatorType) {
                        SliceAggregatorType.EVALUATION -> instanceId + "_evaluation_$seed"
                        SliceAggregatorType.EVALUATION_O1 -> instanceId + "_evaluation_${seed}_o1"
                        else -> throw Exception("EvaluationAggregator available only for evaluation")
                    }
                }
            }
        }

        depIds.forEach { depId ->
            snapshot(RelativeId(subProjectBuildTypeId(depId))) {
                onDependencyFailure = FailureAction.IGNORE
                onDependencyCancel = FailureAction.IGNORE
                reuseBuilds = ReuseBuilds.NO
            }
        }
    }

    failureConditions {
        executionTimeoutMin = 30
        errorMessage = true
    }

    agentRequirements()
})


fun Project.sliceAggregator(id: String, name: String, slice: List<String>, aggregatorType: SliceAggregatorType) {
    val buildType = when (aggregatorType) {
        SliceAggregatorType.EVALUATION, SliceAggregatorType.EVALUATION_O1 -> {
            EvaluationAggregator(id, name, slice, aggregatorType)
        }
        SliceAggregatorType.VALIDATION -> ValidationAggregator(id, name, slice)
        SliceAggregatorType.EXECUTION -> ExecutionAggregator(id, name, slice)
    }

    buildType(buildType)
}

fun Project.sliceAggregatorMatrix(id: String, name: String, slice: List<String>, aggregatorType: SliceAggregatorType, runsPerTask: Int) {
    val buildType = when (aggregatorType) {
        SliceAggregatorType.EVALUATION, SliceAggregatorType.EVALUATION_O1 -> {
            EvaluationAggregator(id, name, slice, aggregatorType, runsPerTask = runsPerTask)
        }
        SliceAggregatorType.VALIDATION -> ValidationAggregator(id, name, slice)
        SliceAggregatorType.EXECUTION -> ExecutionAggregator(id, name, slice)
    }

    buildType(buildType)
}
