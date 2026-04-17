import dataset.swebench_lite.taskIds
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.sharedResources
import java.io.File

const val baseImagesArchive = "base_images.tar"
const val baseImagesManifest = "base_images.txt"

// Generate build configurations per task ID
val tasks = taskIds.map { taskId -> createTaskEnvBuildType(taskId) }.toList()

object SWE_Bench_Lite : Project({
    name = "SWE-Bench Lite"

    buildType(SWE_Bench_Lite_Dataset)
    buildType(SWE_Bench_Lite_BaseImages)

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

object SWE_Bench_Lite_BaseImages: BuildType ({
    id("SWE_Bench_Lite_BaseImages")
    name = "SWE Bench Lite Base Images"
    description = "Build and cache shared SWE-Bench base Docker images"

    requirements {
        startsWith("teamcity.agent.name", "DemoAgent4Cpu16Gb")
        moreThan("teamcity.agent.work.dir.freeSpaceMb", "10240")
    }

    params {
        param("env.HF_HOME", "%teamcity.build.workingDir%/dataset_cache")
        param("env.HF_DATASETS_CACHE", "%teamcity.build.workingDir%/dataset_cache/datasets")
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
            name = "Build shared base images"
            command = script {
                content = File("scripts/build_base_images.py").readText()
            }

            environment = venv {
                requirementsFile = ""
                pipArgs = "swebench"
            }
        }
        script {
            name = "Save base image archive"
            scriptContent = File("scripts/save_base_images.sh").readText()
        }
    }

    artifactRules = """
        $baseImagesArchive
        $baseImagesManifest
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
