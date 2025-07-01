package utilities.builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import utilities.*
import java.io.File


private const val SWE_BENCH_DIR = "swebench_matterhorn"

private fun BuildType.failureByTimeout(timeoutInMinutes: Int) {
    failureConditions {
        executionTimeoutMin = timeoutInMinutes
        errorMessage = true
    }
}


class ValidateBuild(data: Map<String, String>) : BuildType({
    val instanceId: String = data["instance_id"]!!
    id(subProjectBuildTypeId(instanceId))
    name = instanceId

    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    steps {
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
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
            name = "Prepare instance environment"
            scriptContent = File("utilities/scripts/prepare_instance_env.sh").readText().trimIndent()
        }
        script {
            name = "Run validate"
            scriptContent = File("utilities/scripts/validate_instance.sh").readText().trimIndent()
        }
    }

    failureByTimeout(60)
    agentRequirements()
})


class ValidateInDockerBuild(data: Map<String, String>) : BuildType({
    val instanceId: String = data["instance_id"]!!
    val repo: String = data["repo"]!!
    id(subProjectBuildTypeId(instanceId))
    name = "$instanceId (dockerized)"

    sweBenchGitRepo(SWE_BENCH_DIR)
    additionalVcsOptions()

    steps {
        prepareInstanceJsonStep(data, workDir = SWE_BENCH_DIR)
        script {
            name = "Run validate"
            scriptContent = File("utilities/scripts/validate_instance_in_docker.sh").readText().trimIndent()
            dockerImage = getDockerFullImageName(instanceId)
            dockerRunParameters = """
                -v ./$SWE_BENCH_DIR:/$SWE_BENCH_DIR
            """.trimIndent()
        }
    }

    failureByTimeout(60)
    agentRequirements()

    features {
        loginToSpaceRegistryFeature()
    }
})