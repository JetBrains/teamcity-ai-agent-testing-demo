import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import java.io.File


fun create_SWE_Bench_Lite_XxTaskSlice(tasks: List<Task>, agentName: String): BuildType {
    val size = tasks.size

    return BuildType({
        id("${size}xTasksFor${agentName}")
        name = "$size Tasks of SWE_Bench Lite"

        params {
            param("size", "$size")
        }

        dependencies {
            tasks.forEach {
                snapshot(it) {
                    onDependencyFailure = FailureAction.FAIL_TO_START
                    reuseBuilds = ReuseBuilds.NO
                }
                artifacts(it) {
                    buildRule = sameChainOrLastFinished()
                    artifactRules = """
                    agent.${it.taskId}.json
                """.trimIndent()
                }
            }
        }

        steps {
            python {
                name ="Calculate Statistics"
                command = script {
                    content = File("scripts/calculate_statistics.py").readText()
                }
            }
        }
    })
}
