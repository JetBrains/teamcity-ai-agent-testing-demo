import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.sharedResources
import java.io.File

fun createTaskEnvBuildType(taskId: String): Task {
    return Task(taskId) {
        id("SWE_Bench_Task_Env_$taskId".replace("-", "_"))
        name = "Task env: $taskId"

        val instanceImageOrigTag = "latest"
        val instanceImageOrigName = "sweb.eval.x86_64.$taskId"
        val instanceImageOrig = "$instanceImageOrigName:$instanceImageOrigTag"

        requirements {
            startsWith("teamcity.agent.name", "DemoAgent4Cpu16Gb")
            moreThan("teamcity.agent.work.dir.freeSpaceMb", "10240")
        }

        params {
            param("instance_id", taskId)
            param("env.HF_HOME", "%teamcity.build.workingDir%/dataset_cache")
            param("env.HF_DATASETS_CACHE", "%teamcity.build.workingDir%/dataset_cache/datasets")
//            param("env.HF_DATASETS_OFFLINE", "1")
            param("env.HF_HUB_CACHE", "%teamcity.build.workingDir%/dataset_cache")
        }

        dependencies {
            artifacts(SWE_Bench_Lite_Dataset) {
                buildRule = lastSuccessful()
                artifactRules = """
                    dataset_cache.zip!/** => dataset_cache
                    venv.zip!/** => .venv
                """.trimIndent()
            }
        }

        features {
            sharedResources {
                readLock("HuggingFaceConnection")
            }
        }

        steps {
            python {
                name = "Extract task data"
                dockerImage = "python:3.11"
                command = script {
                    content = File("scripts/extract_task_data.py").readText()
                }

                environment = venv {
                    requirementsFile = ""
                    pipArgs = "swebench"
                }
            }
            python {
                name = "Build Docker image for $taskId"
                command = module {
                    module = "swebench.harness.prepare_images"
                    scriptArguments = "--instance_ids $taskId --tag $instanceImageOrigTag"
                }
                environment = venv {
                    name = "myVenv"
                    requirementsFile = ""
                    pipArgs = "swebench"
                }
            }
            dockerCommand {
                name = "Save Docker Image for $taskId"
                commandType = other {
                    subCommand = "save"
                    commandArgs = "-o $taskId.tar $instanceImageOrig"
                }
            }
        }

        artifactRules = """
                $taskId.tar
                $taskId.json
                ${taskId}_issue.md
                ${taskId}_hints.md
        """.trimIndent()
    }
}
