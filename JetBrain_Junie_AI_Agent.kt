import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.Parameter
import jetbrains.buildServer.configs.kotlin.ParameterSpecPassword
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import java.io.File

const val agentJunie = "Junie"

object JetBrain_Junie_AI_Agent : Project({
    id("JetBrains_Junie")
    name = "JetBrains Junie AI Agent"

    buildType(DownloadJunie)

    val tenTasksSet = mutableListOf<Task>()
    val thirtyTasksSet = mutableListOf<Task>()
    val fiftyTasksSet = mutableListOf<Task>()

    subProject({
        id("SWE_Bench_Lite_Tasks")
        name = "SWE-Bench Lite Tasks"
        tasks.forEachIndexed { index, task ->
            val taskConfiguration = createTaskForJunieBuildType(task)
            if (index % 10 == 0 && tenTasksSet.size < 10) tenTasksSet.add(taskConfiguration)
            if (index % 7 == 0 && thirtyTasksSet.size < 30) thirtyTasksSet.add(taskConfiguration)
            if (index % 5 == 0 && fiftyTasksSet.size < 50) fiftyTasksSet.add(taskConfiguration)
            buildType(taskConfiguration)
        }
    })

    buildType(create_SWE_Bench_Lite_XxTaskSlice(tenTasksSet,agentJunie))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(thirtyTasksSet, agentJunie))
    buildType(create_SWE_Bench_Lite_XxTaskSlice(fiftyTasksSet, agentJunie))

})


object DownloadJunie: BuildType({
    name = "Download Junie"
    id("DownloadJetBrainsJunie")

    steps {
        script {
            scriptContent = """
                echo "Downloading Junie ..."
                curl -L -o junie.zip "https://github.com/jetbrains-junie/junie/releases/download/236.1/junie-cloud-eap-251.236.1-linux-amd64.zip"
                echo "Downloading Idea ..."
                curl -L -o idea.tar.gz "https://download.jetbrains.com/idea/ideaIU-2025.1.2.tar.gz"
                echo "Repacking Idea ..."
                tar xzf idea.tar.gz
                rm -f idea.tar.gz
                cd idea*
                tar czf ../idea.tar.gz .
                cd ..
                rm -rf idea
            """.trimIndent()
        }
    }

    artifactRules = """
            +:junie.zip
            +:idea.tar.gz
        """.trimIndent()
})

fun createTaskForJunieBuildType(taskEnv: Task) = createTaskForAgentBuildType(
    agentJunie,
    taskEnv,
    listOf(
        Parameter("env.EJ_FOLDER_WORK", "%teamcity.build.workingDir%/.junie"),
        Parameter("env.EJ_IDE_LOCATION", "%teamcity.build.workingDir%/ide"),
        Parameter("env.EJ_AUTH_INGRAZZIO_TOKEN", "credentialsJSON:0feb5b92-0768-4e92-884d-48fe77b85a46", ParameterSpecPassword(readOnly = false))
    ),
    File("scripts/run_junie.sh"),
    ".junie/** => junie.zip"
)
