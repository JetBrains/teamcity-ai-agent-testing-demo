import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.Parameter
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import java.io.File

fun createTaskForAgentBuildType(agentName: String,
                                taskEnv: Task,
                                agentBuildConfiguration: BuildType,
                                agentSpecificParams: List<Parameter>,
                                runAgentScript: File,
                                additionalArtifactRules: String? = null): Task {
    val taskId = taskEnv.taskId
    return Task(taskId) {
        name = "$agentName Task: $taskId"
        id("${agentName}_Task_${taskId}_Runner".replace("-", "_"))

        requirements {
            startsWith("teamcity.agent.name", "DemoAgent4Cpu16Gb")
            moreThan("teamcity.agent.work.dir.freeSpaceMb", "10240")
        }


        params {
            agentSpecificParams.forEach { add(it) }

            param("instance_id", taskId)
            param("solution_file", "${taskId}_solution.jsonl")
            param("report_file", "agent.$taskId.jsonl")
            param("env.HF_HOME", "%teamcity.build.workingDir%/dataset_cache")
            param("env.HF_DATASETS_CACHE", "%teamcity.build.workingDir%/dataset_cache/datasets")
        }

        dependencies {
            // Download Docker image from SWE-Bench task environment
            artifacts(taskEnv) {
                buildRule = lastSuccessful()
                artifactRules = """
                   +:$taskId.tar
                   +:${taskId}_issue.md
                   ?:${taskId}_hints.md
                """.trimIndent()
            }

            // Get AI Agent
            artifacts(agentBuildConfiguration) {
                buildRule = lastSuccessful()
                artifactRules = """
                   agent.zip!/** => .
                """.trimIndent()
            }

            // Dataset cache
            artifacts(SWE_Bench_Lite_Dataset) {
                buildRule = lastSuccessful()
                artifactRules = """
                    dataset_cache.zip!/** => dataset_cache
                """.trimIndent()
            }
        }

        steps {
            dockerCommand {
                name = "Load Docker Image"
                commandType = other {
                    subCommand = "load"
                    commandArgs = "-i $taskId.tar"
                }
            }
            script {
                name = "Run $agentName with Docker Wrapper"
                dockerImage = "sweb.eval.x86_64.$taskId:latest"

                scriptContent = runAgentScript.readText() + "\n" +
                                File("scripts/get_patch.sh").readText()
            }
            python {
                name = "Formating solution for SWEBench evaluation"
                command = script {
                    content = File("scripts/formating_solution.py").readText()
                }
            }
            python {
                name = "Evaluate solution"

                command = module {
                    module = "swebench.harness.run_evaluation"
                    scriptArguments = "--predictions_path %teamcity.build.workingDir%/%solution_file% " +
                            "--report_dir %teamcity.build.workingDir%/report " +
                            "--run_id \"${taskId}\" " +
                            "--dataset_name \"princeton-nlp/SWE-bench_Lite\" " +
                            "--cache_level \"instance\" " +
                            "--namespace \"\" " +
                            "--force_rebuild false " +
                            "--timeout 900 "
                }

                environment = venv {
                    requirementsFile = ""
                    pipArgs = "swebench"
                }
            }
            python {
                name = "Tag run"
                command = script {
                    content = File("scripts/tag_task_execution.py").readText()
                }
            }
        }

        detectHangingBuilds = true

        artifactRules = """
        $additionalArtifactRules
        $taskId.patch
        %solution_file%
        %report_file%
    """.trimIndent()
    }
}
