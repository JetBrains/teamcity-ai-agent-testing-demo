import jetbrains.buildServer.configs.kotlin.Parameter
import jetbrains.buildServer.configs.kotlin.ParameterSpecPassword
import jetbrains.buildServer.configs.kotlin.Project
import java.io.File

const val agentCodex = "Codex"

object OpenAI_Codex_AI_Agent : Project({
    id("OpenAI_Codex")
    name = "OpenAI Codex AI Agent"

    val tenTasksSet = mutableListOf<Task>()
    val thirtyTasksSet = mutableListOf<Task>()
    val fiftyTasksSet = mutableListOf<Task>()

    subProject({
        id("SWE_Bench_Lite_${agentCodex}_Tasks")
        name = "SWE-Bench Lite Tasks"
        tasks.forEachIndexed { index, task ->
            val taskConfiguration = createTaskForCodexBuildType(task)
            if (index % 10 == 0 && tenTasksSet.size < 10) tenTasksSet.add(taskConfiguration)
            if (index % 7 == 0 && thirtyTasksSet.size < 30) thirtyTasksSet.add(taskConfiguration)
            if (index % 5 == 0 && fiftyTasksSet.size < 50) fiftyTasksSet.add(taskConfiguration)
            buildType(taskConfiguration)
        }
    })

    buildType(create_SWE_Bench_Lite_XxTaskSlice(tenTasksSet, agentCodex))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(thirtyTasksSet, agentCodex))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(fiftyTasksSet, agentCodex))
})

fun createTaskForCodexBuildType(taskEnv: Task) = createTaskForAgentBuildType(
    agentCodex,
    taskEnv,
    null,
    listOf(
        Parameter("env.CODEX_API_KEY", "credentialsJSON:74ae7a45-0597-4859-9adc-aa393d84138e", ParameterSpecPassword(readOnly = false)),
    ),
    File("scripts/run_codex.sh"),
)
