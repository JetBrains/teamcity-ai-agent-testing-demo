import dataset.swebench_lite.taskIds
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import java.io.File


// Generate build configurations per task ID
val tasks = taskIds.map { taskId -> createTaskEnvBuildType(taskId) }.toList()

object SWE_Bench_Lite : Project({
    name = "SWE-Bench Lite"

    buildType(SWE_Bench_Lite_Dataset)

    subProject(TaskEnvironments)
})

object SWE_Bench_Lite_Dataset: BuildType ({
    id("SWE_Bench_Lite_Dataset")
    name = "SWE Bench Lite Dataset"
    description = "Download and cache SWE Bench Lite dataset"

    steps {
        python {
            name = "Load Dataset"
            dockerImage = "python:3.11"
            command = script {
                content = File("scripts/download_dataset.py").readText()
            }

            environment = venv {
                requirementsFile = ""
                pipArgs = "swebench datasets huggingface_hub"
            }
        }
    }

    artifactRules = """
            dataset_cache/** => dataset_cache.zip
            .venv => venv.zip
        """.trimIndent()
})

object TaskEnvironments : Project({
    name = "Task Environments"

    tasks.forEach { buildType(it) }

    buildType {
        id("Tasks_RebuildAll")
        name = "Rebuild all"
        type = BuildTypeSettings.Type.COMPOSITE

        dependencies {
            tasks.forEach { task ->
                snapshot(task) {
                    reuseBuilds = ReuseBuilds.NO
                }
            }
        }

//        triggers {
            // URL build trigger on the dataset
//        }
    }
})
