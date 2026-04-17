import jetbrains.buildServer.configs.kotlin.Parameter
import jetbrains.buildServer.configs.kotlin.Project
import java.io.File

const val agentJunie = "Junie"

object JetBrain_Junie_AI_Agent : Project({
    id("JetBrains_Junie")
    name = "JetBrains Junie AI Agent"

    params {
        password("junie.api.key", "credentialsJSON:5af7a5f1-c8f9-4c7b-9380-2d95312cedad")
    }

    val tenTasksSet = mutableListOf<Task>()
    val thirtyTasksSet = mutableListOf<Task>()
    val fiftyTasksSet = mutableListOf<Task>()

    subProject({
        id("SWE_Bench_Lite_${agentJunie}_Tasks")
        name = "SWE-Bench Lite Tasks"
        tasks.forEachIndexed { index, task ->
            val taskConfiguration = createTaskForJunieBuildType(task)
            if (index % 10 == 0 && tenTasksSet.size < 10) tenTasksSet.add(taskConfiguration)
            if (index % 7 == 0 && thirtyTasksSet.size < 30) thirtyTasksSet.add(taskConfiguration)
            if (index % 5 == 0 && fiftyTasksSet.size < 50) fiftyTasksSet.add(taskConfiguration)
            buildType(taskConfiguration)
        }
    })

    buildType(create_SWE_Bench_Lite_XxTaskSlice(tenTasksSet, agentJunie))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(thirtyTasksSet, agentJunie))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(fiftyTasksSet, agentJunie))
})

fun createTaskForJunieBuildType(taskEnv: Task) = createTaskForAgentBuildType(
    agentJunie,
    taskEnv,
    null,
    listOf(
        Parameter("env.JUNIE_API_KEY", "%junie.api.key%"),
    ),
    File("scripts/run_junie.sh"),
    ".junie/** => junie.zip"
)
