//package utilities.builds.ci
//
//import jetbrains.buildServer.configs.kotlin.Project
//import jetbrains.buildServer.configs.kotlin.buildSteps.script
//import utilities.agentRequirements
//import utilities.matterhornCoreGitHubRepo
//import utilities.sweBenchGitRepo
//
//
//fun Project.checkAgentExecution(instanceIds: List<String>) {
//    buildType {
//        id("Check_Agent_Execution")
//        name = "Check agent execution"
//
//        params {
//            param("reverse.dep.*.env.MATTERHORN_CAN_READ_FROM_REDIS", "false")
//            param("reverse.dep.*.env.MATTERHORN_CAN_WRITE_TO_REDIS", "false")
//            param("env.TEAMCITY_TOKEN", "%TeamCityToken%")
//            param("env.TEAMCITY_SERVER_URL", "%teamcity.serverUrl%")
//        }
//
//        sweBenchGitRepo(SWE_BENCH_DIR)
//        matterhornCoreGitHubRepo("matterhorn_core_github")
//
//        steps {
//            script {
//                name = "Check execution"
//                workingDir = SWE_BENCH_DIR
//                scriptContent = """
//                    #!/bin/bash
//                    set -e
//                    python3 -m venv .venv
//                    source .venv/bin/activate
//                    pip install -r requirements.txt
//                    python -m scripts.ci.check_agent_execution --build_id="%teamcity.build.id%"
//                """.trimIndent()
//            }
//        }
//
//        executionDeps(instanceIds)
//        agentRequirements()
//
//        commitStatusPublisherFeature()
//        pullRequestsFeature()
//        vcsTrigger()
//    }
//}
