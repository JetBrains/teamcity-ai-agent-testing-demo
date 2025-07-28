import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.Parameter
import jetbrains.buildServer.configs.kotlin.ParameterSpecPassword
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import java.io.File

const val agentGemini = "GeminiCLI"

object Google_Gemini_CLI_AI_Agent : Project({
    id("Google_Gemini_CLI")
    name = "Google Gemini CLI AI Agent"

    vcsRoot(GeminiCLI)
    buildType(BuildAndTestGoogleGeminiCLI)

    val tenTasksSet = mutableListOf<Task>()
    val thirtyTasksSet = mutableListOf<Task>()
    val fiftyTasksSet = mutableListOf<Task>()

    subProject({
        id("SWE_Bench_Lite_${agentGemini}_Tasks")
        name = "SWE-Bench Lite Tasks"
        tasks.forEachIndexed { index, task ->
            val taskConfiguration = createTaskForGeminiBuildType(task)
            if (index % 10 == 0 && tenTasksSet.size < 10) tenTasksSet.add(taskConfiguration)
            if (index % 7 == 0 && thirtyTasksSet.size < 30) thirtyTasksSet.add(taskConfiguration)
            if (index % 5 == 0 && fiftyTasksSet.size < 50) fiftyTasksSet.add(taskConfiguration)
            buildType(taskConfiguration)
        }
    })

    buildType(create_SWE_Bench_Lite_XxTaskSlice(tenTasksSet, agentGemini))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(thirtyTasksSet, agentGemini))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(fiftyTasksSet, agentGemini))
})

object GeminiCLI: GitVcsRoot ({
    name = "My project main repository"
    url = "https://github.com/google-gemini/gemini-cli.git"
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
    """.trimIndent()
    checkoutPolicy = AgentCheckoutPolicy.USE_MIRRORS
})

object BuildAndTestGoogleGeminiCLI: BuildType({
    name = "Build and Test Gemini CLI"
    id("BuildAndTestGoogleGeminiCLI")

    vcs {
        root(GeminiCLI)
    }

    steps {
        script {
            dockerImage = "node:21"
            scriptContent = """
                npm install
            """.trimIndent()
        }
        script {
            dockerImage = "node:21"
            scriptContent = """
                npm run build
            """.trimIndent()
        }
        script {
            dockerImage = "node:21"
            scriptContent = """
                mkdir -p node
                cp -r /usr/local/* node/ 
            """.trimIndent()
        }
    }

    artifactRules = """
        +:bundle/gemini.js => agent.zip
        +:node => agent.zip!/node
        """.trimIndent()
})

fun createTaskForGeminiBuildType(taskEnv: Task) = createTaskForAgentBuildType(
   agentGemini,
    taskEnv,
    BuildAndTestGoogleGeminiCLI,
    listOf(
        Parameter("env.TBD", "TBD", ParameterSpecPassword(readOnly = false))
    ),
    File("scripts/run_gemini.sh"),
)
